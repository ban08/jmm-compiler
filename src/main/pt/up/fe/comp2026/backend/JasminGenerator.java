package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.*;
import org.specs.comp.ollir.inst.*;
import org.specs.comp.ollir.tree.TreeNode;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
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

    private int currentStack;
    private int maxStack;

    private final JasminUtils types;
    private OptUtils utils;
    private final FunctionClassMap<TreeNode, String> generators;

    public JasminGenerator(OllirResult ollirResult) {
        this.ollirResult = ollirResult;

        reports = new ArrayList<>();
        code = null;
        currentMethod = null;
        isInsideAssignment = false;
        currentStack = 0;
        maxStack = 0;

        types = new JasminUtils(ollirResult);
        // Initialize everytime we start a method
        utils = null;
        this.generators = new FunctionClassMap<>();
        generators.put(ClassUnit.class, this::generateClassUnit);
        generators.put(Method.class, this::generateMethod);
        generators.put(AssignInstruction.class, this::generateAssign);
        generators.put(SingleOpInstruction.class, this::generateSingleOp);
        generators.put(LiteralElement.class, this::generateLiteral);
        generators.put(Operand.class, this::generateOperand);
        generators.put(BinaryOpInstruction.class, this::generateBinaryOp);
        generators.put(ReturnInstruction.class, this::generateReturn);
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

        try {
            if (method.isConstructMethod()) {
                return generateConstructor(method);
            }

            return generateRegularMethod(method);
        } finally {
            currentMethod = null;
        }
    }

    private String generateConstructor(Method method) {
        var code = new StringBuilder();

        resetStackTracker();
        var body = generateMethodBody(method);

        code.append(NL).append(".method public <init>()V").append(NL);
        code.append(TAB).append(".limit stack ").append(getStackLimit()).append(NL);
        code.append(TAB).append(".limit locals ").append(getLocalsLimit(method)).append(NL);
        code.append(body);

        var hasReturn = method.getInstructions().stream().anyMatch(ReturnInstruction.class::isInstance);
        if (!hasReturn) {
            code.append(TAB).append("return").append(NL);
        }

        code.append(".end method").append(NL);
        return code.toString();
    }

    private String generateRegularMethod(Method method) {
        var code = new StringBuilder();

        resetStackTracker();
        var body = generateMethodBody(method);

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

        code.append(TAB).append(".limit stack ").append(getStackLimit()).append(NL);
        code.append(TAB).append(".limit locals ").append(getLocalsLimit(method)).append(NL);

        code.append(body);

        code.append(".end method").append(NL);
        //System.out.println("METHOD:\n" + code);
        //System.out.println("ENDING METHOD " + method.getMethodName());
        return code.toString();
    }

    private String generateMethodBody(Method method) {
        var bodyCode = new StringBuilder();

        for (var inst : method.getInstructions()) {
            method.getLabels(inst).forEach(label -> bodyCode.append(label).append(":").append(NL));

            var instCode = StringLines.getLines(apply(inst)).stream()
                    .collect(Collectors.joining(NL + TAB, TAB, NL));

            bodyCode.append(instCode);
        }

        return bodyCode.toString();
    }

    private String generateAssign(AssignInstruction assign) {
        try {
            isInsideAssignment = true;


            var code = new StringBuilder();

            // store value in the stack in destination
            var lhs = assign.getDest();

            // generate code for loading what's on the right
            code.append(apply(assign.getRhs()));


            // Assume Operand
            var operand = (Operand) lhs;


            // get register
            var reg = currentMethod.getVarTable().get(operand.getName());

            code.append(types.getStore(reg)).append(NL);
            updateStack(-1);

            return code.toString();
        } finally {
            isInsideAssignment = false;
        }
    }

    private String generateSingleOp(SingleOpInstruction singleOp) {
        return apply(singleOp.getSingleOperand());
    }

    private String generateLiteral(LiteralElement literal) {
        updateStack(1);
        return "ldc " + literal.getLiteral() + NL;
    }

    private String generateOperand(Operand operand) {
        // get register
        var reg = currentMethod.getVarTable().get(operand.getName());

        updateStack(1);
        return types.getLoad(reg) + NL;
    }


    private String generateBinaryOp(BinaryOpInstruction binaryOp) {

        var code = new StringBuilder();

        // load values on the left and on the right
        code.append(apply(binaryOp.getLeftOperand()));
        code.append(apply(binaryOp.getRightOperand()));


        var typePrefix = types.getTypePrefix(binaryOp.getOperation().getTypeInfo());

        // apply operation
        var op = switch (binaryOp.getOperation().getOpType()) {
            case ADD -> "add";
            case MUL -> "mul";
            default -> throw new NotImplementedException(binaryOp.getOperation().getOpType());
        };

        code.append(typePrefix + op).append(NL);
        updateStack(-1);

        return code.toString();
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

    private String generateInvokeSpecial(InvokeSpecialInstruction invokeSpecial) {
        var code = new StringBuilder();

        code.append(apply(invokeSpecial.getCaller()));
        invokeSpecial.getArguments().forEach(argument -> code.append(apply(argument)));

        var owner = invokeSpecial.getSuperClass()
                .map(types::getInternalName)
                .orElseGet(() -> currentMethod != null && currentMethod.isConstructMethod()
                        ? types.getInternalName(currentMethod.getOllirClass().getSuperClass())
                        : types.getInternalName(invokeSpecial.getCaller().getType()));

        code.append("invokespecial ")
                .append(owner)
                .append("/")
                .append(getMethodName(invokeSpecial))
                .append(types.getMethodDescriptor(invokeSpecial))
                .append(NL);

        updateStack(callStackDelta(invokeSpecial, 1));

        return code.toString();
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

        return code.toString();
    }

    private String generateNew(NewInstruction newInstruction) {
        if (newInstruction.getReturnType() instanceof ArrayType arrayType) {
            var code = new StringBuilder();
            newInstruction.getArguments().forEach(argument -> code.append(apply(argument)));
            code.append("newarray ")
                    .append(types.getTypeDescriptor(arrayType.getElementType()).equals("I") ? "int" : "boolean")
                    .append(NL);
            updateStack(1 - newInstruction.getArguments().size());
            return code.toString();
        }

        updateStack(1);
        return "new " + types.getInternalName(newInstruction.getReturnType()) + NL;
    }

    private String generateArrayLength(ArrayLengthInstruction arrayLength) {
        return apply(arrayLength.getCaller()) + "arraylength" + NL;
    }

    private String generatePutField(PutFieldInstruction putField) {
        var field = (Operand) putField.getField();
        var code = new StringBuilder();

        code.append(apply(putField.getObject()));
        code.append(apply(putField.getValue()));
        code.append("putfield ")
                .append(types.getInternalName(putField.getObject().getType()))
                .append("/")
                .append(jasminMemberName(field.getName()))
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
                .append(jasminMemberName(field.getName()))
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

    private String jasminMemberName(String name) {
        var normalizedName = name.replace("\"", "");
        if (normalizedName.matches("[A-Za-z_$][A-Za-z0-9_$]*") && !isJasminReservedWord(normalizedName)) {
            return normalizedName;
        }

        return "'" + normalizedName + "'";
    }

    private boolean isJasminReservedWord(String name) {
        return switch (name) {
            case "abstract", "annotation", "bridge", "catch", "class", "deprecated", "end", "enum", "field",
                    "final", "from", "implements", "interface", "limit", "method", "private", "protected",
                    "public", "signature", "static", "super", "synthetic", "throws", "to", "using", "var" -> true;
            default -> false;
        };
    }

    private void resetStackTracker() {
        currentStack = 0;
        maxStack = 0;
    }

    private void updateStack(int delta) {
        currentStack += delta;
        maxStack = Math.max(maxStack, currentStack);
    }

    private int getStackLimit() {
        return maxStack;
    }

    private int getLocalsLimit(Method method) {
        return method.getVarTable().values().stream()
                .mapToInt(Descriptor::getVirtualReg)
                .max()
                .stream()
                .map(maxVirtualReg -> maxVirtualReg + 1)
                .findFirst()
                .orElse(method.isStaticMethod() ? 0 : 1);
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

}
