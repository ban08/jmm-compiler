package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;

import java.util.ArrayList;
import java.util.List;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are expressions.
 *
 * Each visit returns an {@link OllirExprResult} with two parts:
 *   - {@code computation}: instructions that must be emitted before the expression value is used.
 *   - {@code code}: a valid OLLIR operand or sub-expression usable in the surrounding instruction.
 */
public class OllirExprGeneratorVisitor extends AJmmVisitor<Void, OllirExprResult> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private static final String END_STMT = ";\n";
    private static final String BOOL_SUFFIX = ".bool";

    private final SymbolTable table;
    private final TypeUtils types;
    private final OptUtils ollirTypes;

    @FunctionalInterface
    private interface WriteBack {
        void append(StringBuilder computation, String valueCode);
    }

    private record AssignableValue(String readCode, JmmType type, StringBuilder computation, WriteBack writeBack) {
    }

    public OllirExprGeneratorVisitor(SymbolTable table, OptUtils ollirTypes) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = ollirTypes;
    }

    @Override
    protected void buildVisitor() {
        addVisit(PAREN_EXPR, this::visitParenExpr);
        addVisit(INTEGER_LITERAL, this::visitInteger);
        addVisit(BOOLEAN_LITERAL, this::visitBoolean);
        addVisit(THIS_EXPR, this::visitThis);
        addVisit(VAR_REF_EXPR, this::visitVarRef);
        addVisit(NOT_EXPR, this::visitNot);
        addVisit(UNARY_EXPR, this::visitUnary);
        addVisit(BINARY_EXPR, this::visitBinExpr);
        addVisit(ARRAY_ACCESS_EXPR, this::visitArrayAccessExpr);
        addVisit(NEW_INT_ARRAY_EXPR, this::visitNewIntArrayExpr);
        addVisit(ARRAY_INITIALIZER_EXPR, this::visitArrayInitializerExpr);
        addVisit(FIELD_ACCESS_EXPR, this::visitFieldAccessExpr);
        addVisit(METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(IMPLICIT_THIS_CALL_EXPR, this::visitImplicitThisCallExpr);
        addVisit(NEW_EXPR, this::visitNewExpr);
    }

    private OllirExprResult visitParenExpr(JmmNode node, Void unused) {
        return visit(node.getChild(0));
    }

    private OllirExprResult visitInteger(JmmNode node, Void unused) {
        String code = node.get("value") + ollirTypes.toOllirType(TypeUtils.intType());
        return new OllirExprResult(code);
    }

    private OllirExprResult visitBoolean(JmmNode node, Void unused) {
        // The local OLLIR ANTLR grammar only has integer/string literals; true/false would
        // be parsed as identifiers. Encode booleans as 1.bool / 0.bool.
        String value = "true".equalsIgnoreCase(node.get("value")) ? "1" : "0";
        return new OllirExprResult(value + BOOL_SUFFIX);
    }

    private OllirExprResult visitThis(JmmNode node, Void unused) {
        String code = "this." + ollirTypes.toOllirClassName(table.getFullyQualifiedName());
        return new OllirExprResult(code);
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        String name = node.get("name");
        var resolved = types.resolveIdentifier(node, name);

        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            JmmType fieldType = resolved.get().type();
            String typeSuffix = ollirTypes.toOllirType(fieldType);

            String tmp = ollirTypes.nextTemp() + typeSuffix;

            StringBuilder computation = new StringBuilder();
            computation.append(tmp)
                    .append(SPACE)
                    .append(ASSIGN)
                    .append(typeSuffix)
                    .append(SPACE)
                    .append("getfield(this, ")
                    .append(ollirTypes.sanitizeId(name))
                    .append(typeSuffix)
                    .append(")")
                    .append(typeSuffix)
                    .append(END_STMT);

            return new OllirExprResult(tmp, computation);
        }

        // Local, param, class identifier, or import. For class/import receivers used as call
        // targets (e.g. `Foo.bar()`), the surrounding call lowering decides how to emit them;
        // returning the bare sanitized id keeps the existing contract.
        JmmType varType = types.getExprType(node);
        String typeSuffix = varType != null ? ollirTypes.toOllirType(varType) : "";
        return new OllirExprResult(ollirTypes.sanitizeId(name) + typeSuffix);
    }

    private OllirExprResult visitNewExpr(JmmNode node, Void unused) {
        String className = ollirClassName(types.getExprType(node), node.get("name"));
        String typeSuffix = "." + className;

        String tmp = ollirTypes.nextTemp() + typeSuffix;

        StringBuilder computation = new StringBuilder();
        StringBuilder argsCode = new StringBuilder();

        for (int i = 0; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());

            if (!argsCode.isEmpty()) {
                argsCode.append(", ");
            }
            argsCode.append(arg.getCode());
        }

        computation.append(tmp)
                .append(SPACE)
                .append(ASSIGN)
                .append(typeSuffix)
                .append(SPACE)
                .append("new(")
                .append(className)
                .append(")")
                .append(typeSuffix)
                .append(END_STMT);

        computation.append("invokespecial(")
                .append(tmp)
                .append(", \"<init>\"");
        if (!argsCode.isEmpty()) {
            computation.append(", ").append(argsCode);
        }
        computation
                .append(").V")
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitImplicitThisCallExpr(JmmNode node, Void unused) {
        String methodName = node.get("name");

        JmmType returnType = types.getExprType(node);
        String returnSuffix = returnType != null ? ollirTypes.toOllirType(returnType) : ".V";

        StringBuilder computation = new StringBuilder();

        StringBuilder argsCode = new StringBuilder();
        for (int i = 0; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());

            argsCode.append(", ").append(arg.getCode());
        }

        var resolvedCall = types.resolveImplicitThisMethodCall(node);
        boolean staticCall = resolvedCall.map(call -> call.method().isStatic())
                .orElseGet(() -> types.isStaticMethodContext(node));
        String invocationKind = staticCall ? "invokestatic" : "invokevirtual";
        String receiverCode = staticCall
                ? ollirTypes.toOllirClassName(table.getFullyQualifiedName())
                : "this";

        if (TypeUtils.voidType().equals(returnType)) {
            computation.append(invocationKind).append("(")
                    .append(receiverCode)
                    .append(", \"")
                    .append(methodName)
                    .append("\"")
                    .append(argsCode)
                    .append(")")
                    .append(returnSuffix)
                    .append(END_STMT);

            return new OllirExprResult("", computation);
        }

        String tmp = ollirTypes.nextTemp() + returnSuffix;

        computation.append(tmp)
                .append(SPACE)
                .append(ASSIGN)
                .append(returnSuffix)
                .append(SPACE)
                .append(invocationKind)
                .append("(")
                .append(receiverCode)
                .append(", \"")
                .append(methodName)
                .append("\"")
                .append(argsCode)
                .append(")")
                .append(returnSuffix)
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {
        String methodName = node.get("name");

        var receiverNode = node.getChild(0);
        var receiver = visit(receiverNode);

        JmmType returnType = types.getExprType(node);
        String returnSuffix = returnType != null ? ollirTypes.toOllirType(returnType) : ".V";

        StringBuilder computation = new StringBuilder();
        computation.append(receiver.getComputation());

        StringBuilder argsCode = new StringBuilder();
        for (int i = 1; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());

            argsCode.append(", ").append(arg.getCode());
        }

        var resolvedCall = types.resolveMethodCall(node);
        boolean staticCall = resolvedCall.map(call -> call.method().isStatic())
                .orElseGet(() -> types.tryGetExprType(receiverNode)
                        .filter(JmmType::isClass)
                        .map(type -> type.asClass().staticRef())
                        .orElse(false));
        String invocationKind = staticCall ? "invokestatic" : "invokevirtual";
        String receiverCode = staticCall
                ? staticCallReceiver(receiverNode, resolvedCall)
                : receiver.getCode();

        if (TypeUtils.voidType().equals(returnType)) {
            computation.append(invocationKind)
                    .append("(")
                    .append(receiverCode)
                    .append(", \"")
                    .append(methodName)
                    .append("\"")
                    .append(argsCode)
                    .append(")")
                    .append(returnSuffix)
                    .append(END_STMT);

            return new OllirExprResult("", computation);
        }

        String tmp = ollirTypes.nextTemp() + returnSuffix;

        computation.append(tmp)
                .append(SPACE)
                .append(ASSIGN)
                .append(returnSuffix)
                .append(SPACE)
                .append(invocationKind)
                .append("(")
                .append(receiverCode)
                .append(", \"")
                .append(methodName)
                .append("\"")
                .append(argsCode)
                .append(")")
                .append(returnSuffix)
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitNot(JmmNode node, Void unused) {
        var operand = visit(node.getChild(0));

        StringBuilder computation = new StringBuilder();
        computation.append(operand.getComputation());

        String tmp = ollirTypes.nextTemp() + BOOL_SUFFIX;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(BOOL_SUFFIX).append(SPACE)
                .append("!").append(BOOL_SUFFIX).append(SPACE)
                .append(operand.getCode()).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitUnary(JmmNode node, Void unused) {
        String op = node.get("op");
        String intSuffix = ollirTypes.toOllirType(TypeUtils.intType());

        if ("++".equals(op) || "--".equals(op)) {
            var assignable = resolveAssignableValue(node.getChild(0));
            StringBuilder computation = new StringBuilder();
            computation.append(assignable.computation());

            String tmp = ollirTypes.nextTemp() + intSuffix;
            String arithmeticOp = "++".equals(op) ? "+" : "-";
            computation.append(tmp).append(SPACE).append(ASSIGN).append(intSuffix).append(SPACE)
                    .append(assignable.readCode()).append(SPACE)
                    .append(arithmeticOp).append(intSuffix).append(SPACE)
                    .append("1").append(intSuffix).append(END_STMT);
            assignable.writeBack().append(computation, tmp);
            return new OllirExprResult(tmp, computation);
        }

        var operand = visit(node.getChild(0));
        StringBuilder computation = new StringBuilder();
        computation.append(operand.getComputation());
        String tmp = ollirTypes.nextTemp() + intSuffix;
        switch (op) {
            case "+" -> {
                // Identity; still bind to a temp so the surrounding expression has an operand.
                computation.append(tmp).append(SPACE).append(ASSIGN).append(intSuffix).append(SPACE)
                        .append(operand.getCode()).append(END_STMT);
            }
            case "-" -> {
                computation.append(tmp).append(SPACE).append(ASSIGN).append(intSuffix).append(SPACE)
                        .append("0").append(intSuffix).append(SPACE)
                        .append("-").append(intSuffix).append(SPACE)
                        .append(operand.getCode()).append(END_STMT);
            }
            default -> throw new RuntimeException("Unknown unary operator: " + op);
        }

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitBinExpr(JmmNode node, Void unused) {
        String op = node.get("op");

        if ("&&".equals(op) || "||".equals(op)) {
            return visitShortCircuit(node, op);
        }

        var lhs = visit(node.getChild(0));
        var rhs = visit(node.getChild(1));

        // Result type drives the assignment suffix; the operator suffix uses the operand type.
        // For arithmetic ops, both are the same. For comparisons, operands are .i32 but the
        // result is .bool, so the two suffixes differ.
        JmmType resultType = types.getExprType(node);
        JmmType operandType = types.getExprType(node.getChild(0));
        String resultSuffix = ollirTypes.toOllirType(resultType);
        String operandSuffix = operandType != null ? ollirTypes.toOllirType(operandType) : resultSuffix;

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());
        computation.append(rhs.getComputation());

        String tmp = ollirTypes.nextTemp() + resultSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(resultSuffix).append(SPACE)
                .append(lhs.getCode()).append(SPACE)
                .append(op).append(operandSuffix).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitArrayAccessExpr(JmmNode node, Void unused) {
        var arrayExpr = visit(node.getChild(0));
        var indexExpr = visit(node.getChild(1));

        JmmType resultType = types.getExprType(node);
        String resultSuffix = ollirTypes.toOllirType(resultType != null ? resultType : TypeUtils.intType());

        StringBuilder computation = new StringBuilder();
        computation.append(arrayExpr.getComputation());
        computation.append(indexExpr.getComputation());

        String tmp = ollirTypes.nextTemp() + resultSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(resultSuffix).append(SPACE)
                .append(arrayExpr.getCode()).append("[").append(indexExpr.getCode()).append("]")
                .append(resultSuffix)
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitNewIntArrayExpr(JmmNode node, Void unused) {
        JmmType arrayType = types.getExprType(node);
        if (arrayType == null) {
            arrayType = JmmArrayType.of(TypeUtils.intType());
        }

        var dimensions = node.getChildren(ARRAY_CREATION_DIM);
        List<OllirExprResult> explicitSizes = new ArrayList<>();

        StringBuilder computation = new StringBuilder();
        for (var dimension : dimensions) {
            if (dimension.getNumChildren() == 0) {
                break;
            }

            var sizeExpr = visit(dimension.getChild(0));
            computation.append(sizeExpr.getComputation());
            explicitSizes.add(sizeExpr);
        }

        if (explicitSizes.isEmpty()) {
            var zero = "0" + ollirTypes.toOllirType(TypeUtils.intType());
            explicitSizes.add(new OllirExprResult(zero));
        }

        String arraySuffix = ollirTypes.toOllirType(arrayType);

        String arrayTemp = ollirTypes.nextTemp("arr") + arraySuffix;
        StringBuilder newArgs = new StringBuilder();
        for (int i = 0; i < explicitSizes.size(); i++) {
            if (i > 0) {
                newArgs.append(", ");
            }
            newArgs.append(explicitSizes.get(i).getCode());
        }

        computation.append(arrayTemp).append(SPACE).append(ASSIGN).append(arraySuffix).append(SPACE)
                .append("new(array, ").append(newArgs).append(")").append(arraySuffix)
                .append(END_STMT);

        computation.append(emitNestedArrayInitialization(arrayTemp, arrayType, explicitSizes, 0));
        return new OllirExprResult(arrayTemp, computation);
    }

    private OllirExprResult visitArrayInitializerExpr(JmmNode node, Void unused) {
        JmmType arrayType = types.getExprType(node);
        if (arrayType == null) {
            arrayType = JmmArrayType.of(TypeUtils.intType());
        }

        String arraySuffix = ollirTypes.toOllirType(arrayType);
        String intSuffix = ollirTypes.toOllirType(TypeUtils.intType());
        String arrayTemp = ollirTypes.nextTemp("arr") + arraySuffix;

        StringBuilder computation = new StringBuilder();
        computation.append(arrayTemp).append(SPACE).append(ASSIGN).append(arraySuffix).append(SPACE)
                .append("new(array, ").append(node.getNumChildren()).append(intSuffix).append(")").append(arraySuffix)
                .append(END_STMT);

        for (int i = 0; i < node.getNumChildren(); i++) {
            var element = visit(node.getChild(i));
            computation.append(element.getComputation());

            String indexLiteral = i + intSuffix;
            computation.append(arrayTemp).append("[").append(indexLiteral).append("]").append(intSuffix).append(SPACE)
                    .append(ASSIGN).append(intSuffix).append(SPACE)
                    .append(element.getCode()).append(END_STMT);
        }

        return new OllirExprResult(arrayTemp, computation);
    }

    private OllirExprResult visitFieldAccessExpr(JmmNode node, Void unused) {
        var receiver = visit(node.getChild(0));
        var fieldName = node.get(JmmAttributes.FIELD_ACCESS_EXPR.NAME);

        StringBuilder computation = new StringBuilder();
        computation.append(receiver.getComputation());

        // Core array support: lower arr.length to arraylength(arr).
        var receiverType = types.tryGetExprType(node.getChild(0));
        if ("length".equals(fieldName) && receiverType.isPresent() && receiverType.get().isArray()) {
            String intSuffix = ollirTypes.toOllirType(TypeUtils.intType());
            String tmp = ollirTypes.nextTemp() + intSuffix;
            computation.append(tmp).append(SPACE).append(ASSIGN).append(intSuffix).append(SPACE)
                    .append("arraylength(").append(receiver.getCode()).append(")").append(intSuffix)
                    .append(END_STMT);
            return new OllirExprResult(tmp, computation);
        }

        JmmType fieldType = types.resolveFieldAccessType(node)
                .orElseThrow(() -> new IllegalArgumentException("Unknown field access: " + fieldName));
        String fieldSuffix = ollirTypes.toOllirType(fieldType);
        String tmp = ollirTypes.nextTemp() + fieldSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(fieldSuffix).append(SPACE)
                .append("getfield(").append(receiver.getCode()).append(", ")
                .append(ollirTypes.sanitizeId(fieldName)).append(fieldSuffix).append(")")
                .append(fieldSuffix)
                .append(END_STMT);
        return new OllirExprResult(tmp, computation);
    }

    private AssignableValue resolveAssignableValue(JmmNode rawNode) {
        JmmNode node = unwrapParen(rawNode);

        if (VAR_REF_EXPR.check(node)) {
            return resolveIdentifierAssignable(node);
        }

        if (ARRAY_ACCESS_EXPR.check(node)) {
            return resolveArrayAssignable(node);
        }

        if (FIELD_ACCESS_EXPR.check(node)) {
            return resolveFieldAssignable(node);
        }

        throw new IllegalArgumentException("Expression is not assignable: " + node);
    }

    private AssignableValue resolveIdentifierAssignable(JmmNode node) {
        String name = node.get("name");
        var resolved = types.resolveIdentifier(node, name)
                .orElseThrow(() -> new IllegalArgumentException("Unknown assignable identifier: " + name));

        JmmType valueType = resolved.type();
        String typeSuffix = ollirTypes.toOllirType(valueType);
        String safeName = ollirTypes.sanitizeId(name);

        if (resolved.accessType() == AccessType.FIELD) {
            String tmp = ollirTypes.nextTemp() + typeSuffix;
            StringBuilder computation = new StringBuilder();
            computation.append(tmp).append(SPACE).append(ASSIGN).append(typeSuffix).append(SPACE)
                    .append("getfield(this, ").append(safeName).append(typeSuffix).append(")")
                    .append(typeSuffix).append(END_STMT);

            return new AssignableValue(tmp, valueType, computation, (out, valueCode) ->
                    out.append("putfield(this, ")
                            .append(safeName)
                            .append(typeSuffix)
                            .append(", ")
                            .append(valueCode)
                            .append(").V")
                            .append(END_STMT));
        }

        String operand = safeName + typeSuffix;
        return new AssignableValue(operand, valueType, new StringBuilder(), (out, valueCode) ->
                out.append(operand).append(SPACE)
                        .append(ASSIGN).append(typeSuffix).append(SPACE)
                        .append(valueCode).append(END_STMT));
    }

    private AssignableValue resolveArrayAssignable(JmmNode node) {
        var arrayExpr = visit(node.getChild(0));
        var indexExpr = visit(node.getChild(1));

        JmmType valueType = types.getExprType(node);
        if (valueType == null) {
            valueType = TypeUtils.intType();
        }
        String valueSuffix = ollirTypes.toOllirType(valueType);
        String elementOperand = arrayExpr.getCode() + "[" + indexExpr.getCode() + "]" + valueSuffix;

        StringBuilder computation = new StringBuilder();
        computation.append(arrayExpr.getComputation());
        computation.append(indexExpr.getComputation());

        String tmp = ollirTypes.nextTemp() + valueSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(valueSuffix).append(SPACE)
                .append(elementOperand).append(END_STMT);

        return new AssignableValue(tmp, valueType, computation, (out, valueCode) ->
                out.append(elementOperand).append(SPACE)
                        .append(ASSIGN).append(valueSuffix).append(SPACE)
                        .append(valueCode).append(END_STMT));
    }

    private AssignableValue resolveFieldAssignable(JmmNode node) {
        var receiver = visit(node.getChild(0));
        String fieldName = node.get(JmmAttributes.FIELD_ACCESS_EXPR.NAME);

        JmmType valueType = types.resolveFieldAccessType(node)
                .orElseThrow(() -> new IllegalArgumentException("Unknown assignable field: " + fieldName));
        String valueSuffix = ollirTypes.toOllirType(valueType);
        String fieldOperand = ollirTypes.sanitizeId(fieldName) + valueSuffix;

        StringBuilder computation = new StringBuilder();
        computation.append(receiver.getComputation());

        String tmp = ollirTypes.nextTemp() + valueSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(valueSuffix).append(SPACE)
                .append("getfield(").append(receiver.getCode()).append(", ").append(fieldOperand).append(")")
                .append(valueSuffix).append(END_STMT);

        return new AssignableValue(tmp, valueType, computation, (out, valueCode) ->
                out.append("putfield(")
                        .append(receiver.getCode())
                        .append(", ")
                        .append(fieldOperand)
                        .append(", ")
                        .append(valueCode)
                        .append(").V")
                        .append(END_STMT));
    }

    private static JmmNode unwrapParen(JmmNode node) {
        var current = node;
        while (PAREN_EXPR.check(current)) {
            current = current.getChild(0);
        }

        return current;
    }

    private String staticCallReceiver(JmmNode receiverNode, java.util.Optional<TypeUtils.ResolvedMethodCall> resolvedCall) {
        return resolvedCall
                .map(TypeUtils.ResolvedMethodCall::receiverType)
                .filter(JmmType::isClass)
                .map(type -> type.asClass().fullyQualifiedName())
                .or(() -> types.tryGetExprType(receiverNode)
                        .filter(JmmType::isClass)
                        .map(type -> type.asClass().fullyQualifiedName()))
                .map(ollirTypes::toOllirClassName)
                .orElseGet(() -> receiverNode.hasAttribute("name")
                        ? ollirTypes.sanitizeClassName(receiverNode.get("name"))
                        : ollirTypes.toOllirClassName(table.getFullyQualifiedName()));
    }

    private String ollirClassName(JmmType type, String fallbackName) {
        if (type != null && type.isClass()) {
            return ollirTypes.toOllirClassName(type.asClass().fullyQualifiedName());
        }

        return ollirTypes.toOllirClassName(fallbackName);
    }

    private String emitNestedArrayInitialization(String arrayOperand,
                                                 JmmType arrayType,
                                                 List<OllirExprResult> explicitSizes,
                                                 int depth) {
        if (!arrayType.isArray() || depth + 1 >= explicitSizes.size()) {
            return "";
        }

        JmmType childType = arrayElementType(arrayType);
        String childSuffix = ollirTypes.toOllirType(childType);
        String intSuffix = ollirTypes.toOllirType(TypeUtils.intType());

        String indexTemp = ollirTypes.nextTemp("idx") + intSuffix;
        String condTemp = ollirTypes.nextTemp() + BOOL_SUFFIX;

        String condLabel = ollirTypes.nextLabel("multi_arr_cond_");
        String bodyLabel = ollirTypes.nextLabel("multi_arr_body_");
        String endLabel = ollirTypes.nextLabel("multi_arr_end_");

        StringBuilder computation = new StringBuilder();
        computation.append(indexTemp).append(SPACE).append(ASSIGN).append(intSuffix).append(SPACE)
                .append("0").append(intSuffix).append(END_STMT);
        computation.append(condLabel).append(":\n");
        computation.append(condTemp).append(SPACE).append(ASSIGN).append(BOOL_SUFFIX).append(SPACE)
                .append(indexTemp).append(SPACE)
                .append("<").append(intSuffix).append(SPACE)
                .append(explicitSizes.get(depth).getCode()).append(END_STMT);
        computation.append("if (").append(condTemp).append(") goto ").append(bodyLabel).append(END_STMT);
        computation.append("goto ").append(endLabel).append(END_STMT);
        computation.append(bodyLabel).append(":\n");

        String childTemp = ollirTypes.nextTemp("arr") + childSuffix;
        computation.append(childTemp).append(SPACE).append(ASSIGN).append(childSuffix).append(SPACE)
                .append("new(array, ").append(explicitSizes.get(depth + 1).getCode()).append(")").append(childSuffix)
                .append(END_STMT);

        if (childType.isArray()) {
            computation.append(emitNestedArrayInitialization(childTemp, childType, explicitSizes, depth + 1));
        }

        computation.append(arrayOperand).append("[").append(indexTemp).append("]").append(childSuffix).append(SPACE)
                .append(ASSIGN).append(childSuffix).append(SPACE)
                .append(childTemp).append(END_STMT);
        computation.append(indexTemp).append(SPACE).append(ASSIGN).append(intSuffix).append(SPACE)
                .append(indexTemp).append(SPACE)
                .append("+").append(intSuffix).append(SPACE)
                .append("1").append(intSuffix).append(END_STMT);
        computation.append("goto ").append(condLabel).append(END_STMT);
        computation.append(endLabel).append(":\n");

        return computation.toString();
    }

    private static JmmType arrayElementType(JmmType type) {
        if (!type.isArray()) {
            return type;
        }

        var array = type.asArray();
        if (array.dimension() == 1) {
            return array.itemType();
        }

        return JmmArrayType.of(array.itemType(), array.dimension() - 1);
    }

    private OllirExprResult visitShortCircuit(JmmNode node, String op) {
        // Branch-form lowering so side effects in the right operand are skipped on short-circuit.
        var lhs = visit(node.getChild(0));

        String tmp = ollirTypes.nextTemp() + BOOL_SUFFIX;
        String rhsLabel = ollirTypes.nextLabel(("&&".equals(op) ? "and_rhs_" : "or_rhs_"));
        String endLabel = ollirTypes.nextLabel(("&&".equals(op) ? "and_end_" : "or_end_"));

        StringBuilder computation = new StringBuilder();
        computation.append(lhs.getComputation());

        if ("&&".equals(op)) {
            // tmp := 0; if (lhs) goto rhs; goto end; rhs: tmp := <rhs>; end:
            computation.append(tmp).append(SPACE).append(ASSIGN).append(BOOL_SUFFIX).append(SPACE)
                    .append("0").append(BOOL_SUFFIX).append(END_STMT);
            computation.append("if (").append(lhs.getCode()).append(") goto ").append(rhsLabel).append(END_STMT);
            computation.append("goto ").append(endLabel).append(END_STMT);
            computation.append(rhsLabel).append(":\n");
            var rhs = visit(node.getChild(1));
            computation.append(rhs.getComputation());
            computation.append(tmp).append(SPACE).append(ASSIGN).append(BOOL_SUFFIX).append(SPACE)
                    .append(rhs.getCode()).append(END_STMT);
            computation.append(endLabel).append(":\n");
        } else {
            // tmp := 1; if (lhs) goto end; <rhs computation>; tmp := <rhs>; end:
            computation.append(tmp).append(SPACE).append(ASSIGN).append(BOOL_SUFFIX).append(SPACE)
                    .append("1").append(BOOL_SUFFIX).append(END_STMT);
            computation.append("if (").append(lhs.getCode()).append(") goto ").append(endLabel).append(END_STMT);
            var rhs = visit(node.getChild(1));
            computation.append(rhs.getComputation());
            computation.append(tmp).append(SPACE).append(ASSIGN).append(BOOL_SUFFIX).append(SPACE)
                    .append(rhs.getCode()).append(END_STMT);
            computation.append(endLabel).append(":\n");
        }

        return new OllirExprResult(tmp, computation);
    }
}
