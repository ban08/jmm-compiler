package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassKind;
import org.specs.comp.ollir.type.ClassType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2026.optimization.OptUtils;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.classmap.FunctionClassMap;
import pt.up.fe.specs.util.exceptions.NotImplementedException;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates Jasmin code from an OllirResult.
 * <p>
 * One JasminGenerator instance per OllirResult.
 */
public class JasminGenerator {

    private static final String NL = "\n";
    private static final String TAB = "   ";

    private final OllirResult ollirResult;

    private List<Report> reports;

    private String code;

    private Method currentMethod;

    boolean isInsideAssignment;

    private final JasminUtils types;
    private OptUtils utils;
    private MethodEmitter currentEmitter;
    private int generatedLabelCounter;
    private final FunctionClassMap<TreeNode, String> generators;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;
        isInsideAssignment = false;
        currentEmitter = null;
        generatedLabelCounter = 0;

        types = new JasminUtils(ollirResult);
        // Initialize everytime we start a method
        utils = null;
        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(ArrayOperand.class, this::generateArrayOperand);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(UnaryOpInstruction.class, this::generateUnaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
        generators.put(GotoInstruction.class, this::generateGoto);
        generators.put(OpCondInstruction.class, this::generateCondBranch);
        generators.put(SingleOpCondInstruction.class, this::generateCondBranch);
        generators.put(InvokeSpecialInstruction.class, this::generateInvokeSpecial);
        generators.put(InvokeVirtualInstruction.class, this::generateInvokeVirtual);
        generators.put(InvokeStaticInstruction.class, this::generateInvokeStatic);
        generators.put(NewInstruction.class, this::generateNew);
        generators.put(ArrayLengthInstruction.class, this::generateArrayLength);
        generators.put(PutFieldInstruction.class, this::generatePutField);
        generators.put(GetFieldInstruction.class, this::generateGetField);
        generators.put(LdcInstruction.class, this::generateLdc);
    }


    private String apply(TreeNode node) {
        var code = new StringBuilder();

        // Print the corresponding OLLIR code as a comment
        //code.append("; ").append(node).append(NL);

        code.append(generators.apply(node));

        return code.toString();
    }

    public List<Report> getReports() {
        return reports;
    }

    public String build() {

        // This way, build is idempotent
        if (code == null) {
            code = apply(ollirResult.getOllirClass());
        }

        return code;
    }


    private String generateClassUnit(ClassUnit classUnit) {

        var code = new StringBuilder();

        var nameWithPackage = classUnit.getClassFullyQualifiedName().replace('.', '/');
        code.append(".class public ").append(nameWithPackage).append(NL);

        var fullSuperClass = types.getInternalName(classUnit.getSuperClass());
        code.append(".super ").append(fullSuperClass).append(NL).append(NL);

        for (var field : classUnit.getFields()) {
            code.append(generateField(field));
        }

        if (!classUnit.getFields().isEmpty()) {
            code.append(NL);
        }

        var constructors = classUnit.getMethods().stream()
                .filter(Method::isConstructMethod)
                .toList();

        if (constructors.isEmpty()) {
            code.append(generateDefaultConstructor(fullSuperClass));
        } else {
            constructors.forEach(constructor -> code.append(apply(constructor)));
        }

        classUnit.getMethods().stream()
                .filter(method -> !method.isConstructMethod())
                .forEach(method -> code.append(apply(method)));

        return code.toString();
    }

    private String generateField(Field field) {
        var code = new StringBuilder();

        code.append(".field public ");

        if (field.isStaticField()) {
            code.append("static ");
        }

        if (field.isFinalField()) {
            code.append("final ");
        }

        code.append(jasminMemberName(field.getFieldName()))
                .append(" ")
                .append(types.getTypeDescriptor(field.getFieldType()))
                .append(NL);

        return code.toString();
    }

    private String generateDefaultConstructor(String fullSuperClass) {
        return """
                .method public <init>()V
                   .limit stack 1
                   .limit locals 1
                   aload_0
                   invokespecial %s/<init>()V
                   return
                .end method
                """.formatted(fullSuperClass);
    }

    private String generateMethod(Method method) {
        //System.out.println("STARTING METHOD " + method.getMethodName());
        // set method
        currentMethod = method;
        if (method.getVarTable().isEmpty()) {
            method.buildVarTable();
        }

        // Initialize utils, to have fresh labels
        utils = new OptUtils(null);
        generatedLabelCounter = 0;

        try {
            if (method.isConstructMethod()) {
                return generateConstructor(method);
            }

            return generateRegularMethod(method);
        } finally {
            currentMethod = null;
            currentEmitter = null;
        }
    }

    private String generateConstructor(Method method) {
        var code = new StringBuilder();

        currentEmitter = new MethodEmitter(method);
        generateMethodBody(method);

        code.append(NL).append(".method public <init>()V").append(NL);
        code.append(TAB).append(".limit stack ").append(currentEmitter.stackLimit()).append(NL);
        code.append(TAB).append(".limit locals ").append(currentEmitter.localsLimit()).append(NL);
        code.append(currentEmitter.body());

        var hasReturn = method.getInstructions().stream().anyMatch(ReturnInstruction.class::isInstance);
        if (!hasReturn) {
            code.append(TAB).append("return").append(NL);
        }

        code.append(".end method").append(NL);
        return code.toString();
    }

    private String generateRegularMethod(Method method) {
        var code = new StringBuilder();

        currentEmitter = new MethodEmitter(method);
        generateMethodBody(method);

        code.append(NL).append(".method ")
                .append(types.getModifier(method.getMethodAccessModifier()));

        if (method.isStaticMethod()) {
            code.append("static ");
        }

        if (method.isFinalMethod()) {
            code.append("final ");
        }

        var params = method.getParams().stream()
                .map(p -> types.getTypeDescriptor(p.getType()))
                .collect(Collectors.joining());

        var returnType = types.getTypeDescriptor(method.getReturnType());

        code.append(method.getMethodName())
                .append("(")
                .append(params)
                .append(")")
                .append(returnType)
                .append(NL);

        code.append(TAB).append(".limit stack ").append(currentEmitter.stackLimit()).append(NL);
        code.append(TAB).append(".limit locals ").append(currentEmitter.localsLimit()).append(NL);

        code.append(currentEmitter.body());

        code.append(".end method").append(NL);
        //System.out.println("METHOD:\n" + code);
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private void generateMethodBody(Method method) {
        for (var inst : method.getInstructions()) {
            method.getLabels(inst).forEach(currentEmitter::emitOllirLabel);

            currentEmitter.emitCode(apply(inst));
            popTopLevelCallResult(inst);
            currentEmitter.requireEmptyStack("after " + inst.getInstType());
        }
    }

    private String generateAssign(AssignInstruction assign) {
        try {
            isInsideAssignment = true;

            var iinc = generateIinc(assign);
            if (iinc.isPresent()) {
                return iinc.get();
            }

            var code = new StringBuilder();

            var lhs = assign.getDest();
            if (lhs instanceof ArrayOperand arrayOperand) {
                code.append(generateArrayAddress(arrayOperand));
                code.append(apply(assign.getRhs()));
                code.append(getArrayStoreInstruction(arrayOperand.getType())).append(NL);
                updateStack(-3);
                return code.toString();
            }

            code.append(apply(assign.getRhs()));

            var operand = (Operand) lhs;
            var reg = currentMethod.getVarTable().get(operand.getName());

            code.append(types.getStore(reg)).append(NL);
            updateStack(-1);

            return code.toString();
        } finally {
            isInsideAssignment = false;
        }
    }

    private Optional<String> generateIinc(AssignInstruction assign) {
        if (!(assign.getDest() instanceof Operand dest) || dest instanceof ArrayOperand) {
            return Optional.empty();
        }

        var reg = currentMethod.getVarTable().get(dest.getName());
        if (reg == null || !isIntType(reg.getVarType())) {
            return Optional.empty();
        }

        return getIincDelta(dest, assign.getRhs())
                .map(delta -> "iinc " + reg.getVirtualReg() + " " + delta + NL);
    }

    private Optional<Integer> getIincDelta(Operand dest, Instruction rhs) {
        if (!(rhs instanceof BinaryOpInstruction binaryOp)) {
            return Optional.empty();
        }

        var opType = binaryOp.getOperation().getOpType();
        var left = binaryOp.getLeftOperand();
        var right = binaryOp.getRightOperand();

        if (opType == OperationType.ADD) {
            if (isSameLocalOperand(dest, left)) {
                return getIincDelta(right, 1);
            }

            if (isSameLocalOperand(dest, right)) {
                return getIincDelta(left, 1);
            }
        }

        if (opType == OperationType.SUB && isSameLocalOperand(dest, left)) {
            return getIincDelta(right, -1);
        }

        return Optional.empty();
    }

    private Optional<Integer> getIincDelta(Element amountElement, int sign) {
        return getIntegerLiteralValue(amountElement)
                .flatMap(value -> normalizeIincDelta((long) value * sign));
    }

    private Optional<Integer> normalizeIincDelta(long delta) {
        if (delta == 0 || delta < Byte.MIN_VALUE || delta > Byte.MAX_VALUE) {
            return Optional.empty();
        }

        return Optional.of((int) delta);
    }

    private boolean isSameLocalOperand(Operand dest, Element element) {
        if (!(element instanceof Operand operand) || element instanceof ArrayOperand) {
            return false;
        }

        var destReg = currentMethod.getVarTable().get(dest.getName());
        var operandReg = currentMethod.getVarTable().get(operand.getName());
        return destReg != null
                && operandReg != null
                && destReg.getVirtualReg() == operandReg.getVirtualReg()
                && isIntType(operandReg.getVarType());
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        return generateConstant(literal);
    }

    private String generateArrayOperand(ArrayOperand arrayOperand) {
        var code = new StringBuilder();

        code.append(generateArrayAddress(arrayOperand));
        code.append(getArrayLoadInstruction(arrayOperand.getType())).append(NL);
        updateStack(-1);

        return code.toString();
    }

    private String generateOperand(Operand operand) {
        var reg = currentMethod.getVarTable().get(operand.getName());

        SpecsCheck.checkNotNull(reg, () -> "No virtual register for operand '" + operand.getName() + "'");

        updateStack(1);
        return types.getLoad(reg) + NL;
    }


    private String generateBinaryOp(BinaryOpInstruction binaryOp) {

        var opType = binaryOp.getOperation().getOpType();
        if (opType.isConditional()) {
            return generateComparisonValue(binaryOp);
        }

        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));

        var op = switch (opType) {
            case ADD -> "iadd";
            case SUB -> "isub";
            case MUL -> "imul";
            case DIV -> "idiv";
            case REM -> "irem";
            case LOGICAL_AND, BITWISE_AND -> "iand";
            case LOGICAL_OR, BITWISE_OR -> "ior";
            case XOR -> "ixor";
            default -> throw new NotImplementedException(opType);
        };

        code.append(op).append(NL);
        updateStack(-1);

        return code.toString();
    }

    private String generateUnaryOp(UnaryOpInstruction unaryOp) {
        var opType = unaryOp.getOperation().getOpType();

        if (opType == OperationType.LOGICAL_NOT) {
            return generateLogicalNotValue(unaryOp.getOperand());
        }

        if (opType == OperationType.BITWISE_NOT) {
            var code = new StringBuilder();
            code.append(apply(unaryOp.getOperand()));
            code.append("iconst_m1").append(NL);
            updateStack(1);
            code.append("ixor").append(NL);
            updateStack(-1);
            return code.toString();
        }

        throw new NotImplementedException(opType);
    }

    private String generateReturn(ReturnInstruction returnInst) {
        var code = new StringBuilder();

        var returnType = returnInst.getReturnType();

        var typePrefix = types.getTypePrefix(returnType);

        // Load operand into the stack, if present
        returnInst.getOperand().ifPresent(op -> code.append(apply(op)));

        code.append(typePrefix).append("return").append(NL);
        if (returnInst.getOperand().isPresent()) {
            updateStack(-1);
        }

        return code.toString();
    }

    private String generateGoto(GotoInstruction gotoInstruction) {
        currentEmitter.requireEmptyStack("before goto " + gotoInstruction.getLabel());
        return "goto " + gotoInstruction.getLabel() + NL;
    }

    private String generateCondBranch(CondBranchInstruction branch) {
        var condition = branch.getCondition();

        if (condition instanceof BinaryOpInstruction binaryOp
                && binaryOp.getOperation().getOpType().isConditional()) {

            return generateComparisonBranch(binaryOp, branch.getLabel());
        }

        if (condition instanceof UnaryOpInstruction unaryOp
                && unaryOp.getOperation().getOpType() == OperationType.LOGICAL_NOT) {

            var code = new StringBuilder();
            code.append(apply(unaryOp.getOperand()));
            code.append("ifeq ").append(branch.getLabel()).append(NL);
            updateStack(-1);
            return code.toString();
        }

        var code = new StringBuilder();
        code.append(apply(condition));
        code.append("ifne ").append(branch.getLabel()).append(NL);
        updateStack(-1);
        return code.toString();
    }

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        code.append(apply(invokeSpecial.getCaller()));
        invokeSpecial.getArguments().forEach(argument -> code.append(apply(argument)));

        var owner = invokeSpecial.getSuperClass()
                .map(types::getInternalName)
                .orElseGet(() -> getInvokeSpecialOwner(invokeSpecial));

        code.append("invokespecial ")
                .append(owner)
                .append("/")
                .append(getMethodName(invokeSpecial))
                .append(types.getMethodDescriptor(invokeSpecial))
                .append(NL);

        updateStack(callStackDelta(invokeSpecial, 1));
        code.append(popIfIsolated(invokeSpecial));

        return code.toString();
    }

    private String getInvokeSpecialOwner(InvokeSpecialInstruction invokeSpecial) {
        var callerType = invokeSpecial.getCaller().getType();
        if (currentMethod != null
                && currentMethod.isConstructMethod()
                && "<init>".equals(getMethodName(invokeSpecial))
                && callerType instanceof ClassType classType
                && classType.getKind() == ClassKind.THIS) {
            return types.getInternalName(currentMethod.getOllirClass().getSuperClass());
        }

        return types.getInternalName(callerType);
    }

    private String generateInvokeVirtual(InvokeVirtualInstruction invokeVirtual) {
        var code = new StringBuilder();

        code.append(apply(invokeVirtual.getCaller()));
        invokeVirtual.getArguments().forEach(argument -> code.append(apply(argument)));

        code.append("invokevirtual ")
                .append(types.getInternalName(invokeVirtual.getCaller().getType()))
                .append("/")
                .append(getMethodName(invokeVirtual))
                .append(types.getMethodDescriptor(invokeVirtual))
                .append(NL);

        updateStack(callStackDelta(invokeVirtual, 1));
        code.append(popIfIsolated(invokeVirtual));

        return code.toString();
    }

    private String generateInvokeStatic(InvokeStaticInstruction invokeStatic) {
        var code = new StringBuilder();

        invokeStatic.getArguments().forEach(argument -> code.append(apply(argument)));

        var caller = (Operand) invokeStatic.getCaller();
        code.append("invokestatic ")
                .append(types.getInternalName(caller.getName()))
                .append("/")
                .append(getMethodName(invokeStatic))
                .append(types.getMethodDescriptor(invokeStatic))
                .append(NL);

        updateStack(callStackDelta(invokeStatic, 0));
        code.append(popIfIsolated(invokeStatic));

        return code.toString();
    }

    private String generateNew(NewInstruction newInstruction) {
        if (newInstruction.getReturnType() instanceof ArrayType arrayType) {
            var code = new StringBuilder();
            var arguments = newInstruction.getArguments();

            if (arguments.isEmpty()) {
                throw new NotImplementedException("array allocation without size");
            }

            arguments.forEach(argument -> code.append(apply(argument)));

            if (arrayType.getNumDimensions() > 1 || arguments.size() > 1) {
                code.append("multianewarray ")
                        .append(types.getTypeDescriptor(arrayType))
                        .append(" ")
                        .append(arguments.size())
                        .append(NL);
            } else {
                code.append(getOneDimensionalNewArrayInstruction(arrayType)).append(NL);
            }

            updateStack(1 - arguments.size());
            code.append(popIfIsolated(newInstruction));
            return code.toString();
        }

        if (!newInstruction.getArguments().isEmpty()) {
            throw new NotImplementedException("constructor arguments in new");
        }

        updateStack(1);
        return "new " + types.getInternalName(newInstruction.getReturnType()) + NL + popIfIsolated(newInstruction);
    }

    private String getOneDimensionalNewArrayInstruction(ArrayType arrayType) {
        var elementType = arrayType.getElementType();
        if (elementType instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "newarray int";
                case BOOLEAN -> "newarray boolean";
                case STRING -> "anewarray " + types.getInternalName(elementType);
                case VOID -> throw new NotImplementedException("array element type in newarray: " + elementType);
            };
        }

        if (elementType instanceof ClassType) {
            return "anewarray " + types.getInternalName(elementType);
        }

        throw new NotImplementedException("array element type in newarray: " + elementType);
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLength) {
        var code = new StringBuilder();
        code.append(apply(arrayLength.getCaller()));
        code.append("arraylength").append(NL);
        code.append(popIfIsolated(arrayLength));
        return code.toString();
    }

    private String generatePutField(PutFieldInstruction putField) {
        var field = (Operand) putField.getField();
        var code = new StringBuilder();

        code.append(apply(putField.getObject()));
        code.append(apply(putField.getValue()));
        code.append("putfield ")
                .append(types.getInternalName(putField.getObject().getType()))
                .append("/")
                .append(jasminQualifiedMemberName(field.getName()))
                .append(" ")
                .append(types.getTypeDescriptor(field.getType()))
                .append(NL);

        updateStack(-2);

        return code.toString();
    }

    private String generateGetField(GetFieldInstruction getField) {
        var field = (Operand) getField.getField();
        var code = new StringBuilder();

        code.append(apply(getField.getObject()));
        code.append("getfield ")
                .append(types.getInternalName(getField.getObject().getType()))
                .append("/")
                .append(jasminQualifiedMemberName(field.getName()))
                .append(" ")
                .append(types.getTypeDescriptor(field.getType()))
                .append(NL);

        return code.toString();
    }

    private String generateLdc(LdcInstruction ldc) {
        return generateLiteral(ldc.getElement());
    }

    private String getMethodName(CallInstruction call) {
        var methodName = call.getMethodName();

        if (methodName instanceof LiteralElement literal) {
            return literal.getLiteral().replace("\"", "");
        }

        if (methodName instanceof Operand operand) {
            return operand.getName();
        }

        throw new NotImplementedException(methodName.getClass());
    }

    private String generateConstant(LiteralElement literal) {
        var rawLiteral = literal.getLiteral();

        if (literal.getType() instanceof BuiltinType builtinType
                && builtinType.getKind() != BuiltinKind.STRING
                && rawLiteral != null) {

            try {
                var value = Integer.parseInt(rawLiteral);
                updateStack(1);
                return getIntegerConstantInstruction(value) + NL;
            } catch (NumberFormatException ignored) {
                // Fall through to ldc for uncommon literal spellings.
            }
        }

        updateStack(1);
        return "ldc " + formatLdcLiteral(literal) + NL;
    }

    private String getIntegerConstantInstruction(int value) {
        return switch (value) {
            case -1 -> "iconst_m1";
            case 0 -> "iconst_0";
            case 1 -> "iconst_1";
            case 2 -> "iconst_2";
            case 3 -> "iconst_3";
            case 4 -> "iconst_4";
            case 5 -> "iconst_5";
            default -> {
                if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
                    yield "bipush " + value;
                }

                if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
                    yield "sipush " + value;
                }

                yield "ldc " + value;
            }
        };
    }

    private String formatLdcLiteral(LiteralElement literal) {
        var rawLiteral = literal.getLiteral();

        if (literal.getType() instanceof BuiltinType builtinType && builtinType.getKind() == BuiltinKind.STRING) {
            return rawLiteral.startsWith("\"") ? rawLiteral : "\"" + rawLiteral + "\"";
        }

        return rawLiteral;
    }

    private String generateComparisonValue(BinaryOpInstruction binaryOp) {
        var trueLabel = nextGeneratedLabel("cmp_true");
        var endLabel = nextGeneratedLabel("cmp_end");
        var code = new StringBuilder();

        code.append(generateComparisonBranch(binaryOp, trueLabel));

        code.append(getIntegerConstantInstruction(0)).append(NL);
        updateStack(1);
        code.append("goto ").append(endLabel).append(NL);

        setStack(0);
        code.append(trueLabel).append(":").append(NL);
        code.append(getIntegerConstantInstruction(1)).append(NL);
        updateStack(1);
        code.append(endLabel).append(":").append(NL);

        return code.toString();
    }

    private String generateComparisonBranch(BinaryOpInstruction binaryOp, String label) {
        var zeroBranch = generateComparisonBranchAgainstZero(binaryOp, label);
        if (zeroBranch.isPresent()) {
            return zeroBranch.get();
        }

        var code = new StringBuilder();
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));
        code.append("if_icmp")
                .append(getComparisonSuffix(binaryOp.getOperation().getOpType()))
                .append(" ")
                .append(label)
                .append(NL);
        updateStack(-2);
        return code.toString();
    }

    private Optional<String> generateComparisonBranchAgainstZero(BinaryOpInstruction binaryOp, String label) {
        var opType = binaryOp.getOperation().getOpType();
        var left = binaryOp.getLeftOperand();
        var right = binaryOp.getRightOperand();

        if (isLiteralZero(right)) {
            return Optional.of(generateZeroComparison(left, opType, label));
        }

        if (isLiteralZero(left)) {
            return Optional.of(generateZeroComparison(right, swapComparison(opType), label));
        }

        return Optional.empty();
    }

    private String generateZeroComparison(Element value, OperationType opType, String label) {
        var code = new StringBuilder();
        code.append(apply(value));
        code.append("if")
                .append(getComparisonSuffix(opType))
                .append(" ")
                .append(label)
                .append(NL);
        updateStack(-1);
        return code.toString();
    }

    private String generateLogicalNotValue(Element operand) {
        var trueLabel = nextGeneratedLabel("not_true");
        var endLabel = nextGeneratedLabel("not_end");
        var code = new StringBuilder();

        code.append(apply(operand));
        code.append("ifeq ").append(trueLabel).append(NL);
        updateStack(-1);

        code.append(getIntegerConstantInstruction(0)).append(NL);
        updateStack(1);
        code.append("goto ").append(endLabel).append(NL);

        setStack(0);
        code.append(trueLabel).append(":").append(NL);
        code.append(getIntegerConstantInstruction(1)).append(NL);
        updateStack(1);
        code.append(endLabel).append(":").append(NL);

        return code.toString();
    }

    private String getComparisonSuffix(OperationType opType) {
        return switch (opType) {
            case EQ -> "eq";
            case NEQ -> "ne";
            case LTH -> "lt";
            case LTE -> "le";
            case GTH -> "gt";
            case GTE -> "ge";
            default -> throw new NotImplementedException(opType);
        };
    }

    private OperationType swapComparison(OperationType opType) {
        return switch (opType) {
            case EQ -> OperationType.EQ;
            case NEQ -> OperationType.NEQ;
            case LTH -> OperationType.GTH;
            case LTE -> OperationType.GTE;
            case GTH -> OperationType.LTH;
            case GTE -> OperationType.LTE;
            default -> throw new NotImplementedException(opType);
        };
    }

    private boolean isLiteralZero(Element element) {
        return getIntegerLiteralValue(element)
                .map(value -> value == 0)
                .orElse(false);
    }

    private Optional<Integer> getIntegerLiteralValue(Element element) {
        if (!(element instanceof LiteralElement literal) || rawLiteralIsString(literal)) {
            return Optional.empty();
        }

        try {
            return Optional.of(Integer.parseInt(literal.getLiteral()));
        } catch (NumberFormatException ignored) {
            return Optional.empty();
        }
    }

    private boolean rawLiteralIsString(LiteralElement literal) {
        return literal.getType() instanceof BuiltinType builtinType && builtinType.getKind() == BuiltinKind.STRING;
    }

    private boolean isIntType(org.specs.comp.ollir.type.Type type) {
        return type instanceof BuiltinType builtinType && builtinType.getKind() == BuiltinKind.INT32;
    }

    private String generateArrayAddress(ArrayOperand arrayOperand) {
        var indexes = arrayOperand.getIndexOperands();
        SpecsCheck.checkArgument(!indexes.isEmpty(), () -> "Array operand has no indexes: " + arrayOperand);

        var code = new StringBuilder();
        var reg = SpecsCheck.checkNotNull(currentMethod.getVarTable().get(arrayOperand.getName()),
                () -> "No virtual register for array operand '" + arrayOperand.getName() + "'");

        code.append(types.getLoad(reg)).append(NL);
        updateStack(1);

        for (int index = 0; index < indexes.size() - 1; index++) {
            code.append(apply(indexes.get(index)));
            code.append("aaload").append(NL);
            updateStack(-1);
        }

        code.append(apply(indexes.getLast()));
        return code.toString();
    }

    private String getArrayLoadInstruction(org.specs.comp.ollir.type.Type elementType) {
        return getTypeLoadPrefix(elementType) + "aload";
    }

    private String getArrayStoreInstruction(org.specs.comp.ollir.type.Type elementType) {
        return getTypeLoadPrefix(elementType) + "astore";
    }

    private String getTypeLoadPrefix(org.specs.comp.ollir.type.Type type) {
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "i";
                case BOOLEAN -> "b";
                case STRING -> "a";
                case VOID -> throw new RuntimeException("Void cannot be loaded from an array");
            };
        }

        return "a";
    }

    private String popIfIsolated(CallInstruction call) {
        if (isInsideAssignment || !call.isIsolated() || stackSlots(call.getReturnType()) == 0) {
            return "";
        }

        updateStack(-1);
        return "pop" + NL;
    }

    private void popTopLevelCallResult(Instruction inst) {
        if (!(inst instanceof CallInstruction call) || stackSlots(call.getReturnType()) == 0) {
            return;
        }

        if (currentEmitter.stackHeight() == stackSlots(call.getReturnType())) {
            updateStack(-stackSlots(call.getReturnType()));
            currentEmitter.emitCode("pop" + NL);
        }
    }

    private String nextGeneratedLabel(String prefix) {
        return "__jasmin_" + prefix + "_" + generatedLabelCounter++;
    }

    private String jasminMemberName(String name) {
        var normalizedName = normalizeMemberName(name);
        if (normalizedName.matches("[A-Za-z_$][A-Za-z0-9_$]*") && !isJasminReservedWord(normalizedName)) {
            return normalizedName;
        }

        return "'" + normalizedName + "'";
    }

    private String jasminQualifiedMemberName(String name) {
        return normalizeMemberName(name);
    }

    private String normalizeMemberName(String name) {
        var normalizedName = name.replace("\"", "");

        if (normalizedName.length() >= 2
                && normalizedName.startsWith("'")
                && normalizedName.endsWith("'")) {
            return normalizedName.substring(1, normalizedName.length() - 1);
        }

        return normalizedName;
    }

    private boolean isJasminReservedWord(String name) {
        return switch (name) {
            case "abstract", "annotation", "bridge", "catch", "class", "deprecated", "end", "enum", "field",
                    "final", "from", "implements", "interface", "limit", "method", "private", "protected",
                    "public", "signature", "static", "super", "synthetic", "throws", "to", "using", "var" -> true;
            default -> false;
        };
    }

    private void updateStack(int delta) {
        currentEmitter.stack.update(delta);
    }

    private void setStack(int stackHeight) {
        currentEmitter.stack.set(stackHeight);
    }

    private int callStackDelta(CallInstruction call, int receiverSlots) {
        return stackSlots(call.getReturnType()) - receiverSlots - call.getArguments().size();
    }

    private int stackSlots(org.specs.comp.ollir.type.Type type) {
        if (type instanceof BuiltinType builtinType && builtinType.getKind() == BuiltinKind.VOID) {
            return 0;
        }

        return 1;
    }

    private final class MethodEmitter {
        private final Method method;
        private final StringBuilder body;
        private final StackTracker stack;

        private MethodEmitter(Method method) {
            this.method = method;
            this.body = new StringBuilder();
            this.stack = new StackTracker(method);
        }

        private void emitCode(String code) {
            StringLines.getLines(code).forEach(line -> {
                if (line.isBlank()) {
                    return;
                }

                if (line.endsWith(":")) {
                    body.append(line).append(NL);
                    return;
                }

                body.append(TAB).append(line).append(NL);
            });
        }

        private void emitOllirLabel(String label) {
            requireEmptyStack("at label " + label);
            body.append(label).append(":").append(NL);
        }

        private void requireEmptyStack(String context) {
            stack.requireEmpty(context);
        }

        private String body() {
            return body.toString();
        }

        private int stackLimit() {
            return stack.max();
        }

        private int localsLimit() {
            return method.getVarTable().values().stream()
                    .mapToInt(Descriptor::getVirtualReg)
                    .max()
                    .stream()
                    .map(maxVirtualReg -> maxVirtualReg + 1)
                    .findFirst()
                    .orElse(method.isStaticMethod() ? 0 : 1);
        }

        private int stackHeight() {
            return stack.current();
        }
    }

    private final class StackTracker {
        private final Method method;
        private int currentStack;
        private int maxStack;

        private StackTracker(Method method) {
            this.method = method;
            this.currentStack = 0;
            this.maxStack = 0;
        }

        private void update(int delta) {
            currentStack += delta;
            if (currentStack < 0) {
                var exception = new IllegalStateException("Jasmin stack underflow in method '"
                        + method.getMethodName() + "'");
                reports.add(Report.newError(Stage.BACKEND_GENERATION, -1, -1, exception.getMessage(), exception));
                throw exception;
            }

            maxStack = Math.max(maxStack, currentStack);
        }

        private void set(int stackHeight) {
            currentStack = stackHeight;
            maxStack = Math.max(maxStack, currentStack);
        }

        private void requireEmpty(String context) {
            if (currentStack == 0) {
                return;
            }

            var message = "Expected empty Jasmin stack " + context + " in method '"
                    + method.getMethodName() + "', found height " + currentStack;
            var exception = new IllegalStateException(message);
            reports.add(Report.newError(Stage.BACKEND_GENERATION, -1, -1, message, exception));
            throw exception;
        }

        private int max() {
            return maxStack;
        }

        private int current() {
            return currentStack;
        }
    }

}
