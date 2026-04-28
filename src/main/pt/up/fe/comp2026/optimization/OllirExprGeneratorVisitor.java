package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;

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
        String code = "this." + OptUtils.simpleClassName(table.getFullyQualifiedName());
        return new OllirExprResult(code);
    }

    private OllirExprResult visitVarRef(JmmNode node, Void unused) {
        String name = node.get("name");
        var resolved = types.resolveIdentifier(node, name);

        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            throw new UnsupportedOperationException("Field reads are owned by the field OLLIR task");
        }

        // Local, param, class identifier, or import. For class/import receivers used as call
        // targets (e.g. `Foo.bar()`), the surrounding call lowering decides how to emit them;
        // returning the bare sanitized id keeps the existing contract.
        JmmType varType = types.getExprType(node);
        String typeSuffix = varType != null ? ollirTypes.toOllirType(varType) : "";
        return new OllirExprResult(ollirTypes.sanitizeId(name) + typeSuffix);
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
        var operand = visit(node.getChild(0));
        String intSuffix = ollirTypes.toOllirType(TypeUtils.intType());

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
            case "++", "--" -> throw new UnsupportedOperationException(
                    "Prefix increment/decrement needs writeback support and is not part of the core OLLIR slice");
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

        return new OllirExprResult(arrayExpr.getCode() + "[" + indexExpr.getCode() + "]" + resultSuffix, computation);
    }

    private OllirExprResult visitNewIntArrayExpr(JmmNode node, Void unused) {
        var dimensions = node.getChildren(ARRAY_CREATION_DIM);
        OllirExprResult sizeExpr;
        if (dimensions.isEmpty() || dimensions.getFirst().getNumChildren() == 0) {
            sizeExpr = new OllirExprResult("0" + ollirTypes.toOllirType(TypeUtils.intType()));
        } else {
            sizeExpr = visit(dimensions.getFirst().getChild(0));
        }

        JmmType arrayType = types.getExprType(node);
        if (arrayType == null) {
            arrayType = pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType.of(TypeUtils.intType());
        }
        String arraySuffix = ollirTypes.toOllirType(arrayType != null ? arrayType : types.getExprType(node));

        StringBuilder computation = new StringBuilder();
        computation.append(sizeExpr.getComputation());

        String tmp = ollirTypes.nextTemp() + arraySuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(arraySuffix).append(SPACE)
                .append("new(array, ").append(sizeExpr.getCode()).append(")").append(arraySuffix)
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
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

        throw new UnsupportedOperationException("Field reads are owned by the field OLLIR task");
    }

    private OllirExprResult visitMethodCallExpr(JmmNode node, Void unused) {
        var receiver = visit(node.getChild(0));

        StringBuilder computation = new StringBuilder();
        computation.append(receiver.getComputation());

        StringBuilder args = new StringBuilder();
        for (int i = 1; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            if (args.length() > 0) args.append(", ");
            args.append(arg.getCode());
        }

        String methodName = node.get(JmmAttributes.METHOD_CALL_EXPR.NAME);
        JmmType returnType = types.getExprType(node);
        if (returnType == null) {
            returnType = TypeUtils.voidType();
        }
        String returnSuffix = ollirTypes.toOllirType(returnType);

        String invokeKind = "invokevirtual";
        var receiverRaw = node.getChild(0);
        if (VAR_REF_EXPR.check(receiverRaw)) {
            var resolved = types.resolveIdentifier(receiverRaw, receiverRaw.get("name"));
            if (resolved.isPresent() && (resolved.get().accessType() == AccessType.CLASS || resolved.get().accessType() == AccessType.IMPORT)) {
                invokeKind = "invokestatic";
            }
        }

        String callCode = invokeKind + "(" + receiver.getCode() + ", \"" + methodName + "\""
                + (args.length() == 0 ? "" : ", " + args) + ")" + returnSuffix;

        if (TypeUtils.voidType().equals(returnType)) {
            computation.append(callCode).append(END_STMT);
            return new OllirExprResult("", computation);
        }

        String tmp = ollirTypes.nextTemp() + returnSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(returnSuffix).append(SPACE)
                .append(callCode).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitImplicitThisCallExpr(JmmNode node, Void unused) {
        String thisCode = "this." + OptUtils.simpleClassName(table.getFullyQualifiedName());

        StringBuilder computation = new StringBuilder();
        StringBuilder args = new StringBuilder();
        for (int i = 0; i < node.getNumChildren(); i++) {
            var arg = visit(node.getChild(i));
            computation.append(arg.getComputation());
            if (args.length() > 0) args.append(", ");
            args.append(arg.getCode());
        }

        String methodName = node.get(JmmAttributes.IMPLICIT_THIS_CALL_EXPR.NAME);
        JmmType returnType = types.getExprType(node);
        if (returnType == null) {
            returnType = TypeUtils.voidType();
        }
        String returnSuffix = ollirTypes.toOllirType(returnType);

        String invokeKind = types.isStaticMethodContext(node) ? "invokestatic" : "invokevirtual";
        String callCode = invokeKind + "(" + thisCode + ", \"" + methodName + "\""
                + (args.length() == 0 ? "" : ", " + args) + ")" + returnSuffix;

        if (TypeUtils.voidType().equals(returnType)) {
            computation.append(callCode).append(END_STMT);
            return new OllirExprResult("", computation);
        }

        String tmp = ollirTypes.nextTemp() + returnSuffix;
        computation.append(tmp).append(SPACE).append(ASSIGN).append(returnSuffix).append(SPACE)
                .append(callCode).append(END_STMT);

        return new OllirExprResult(tmp, computation);
    }

    private OllirExprResult visitNewExpr(JmmNode node, Void unused) {
        String className = OptUtils.simpleClassName(node.get(JmmAttributes.NEW_EXPR.NAME));

        var classType = JmmClassType.ofInstance(className, false);
        String classSuffix = ollirTypes.toOllirType(classType);

        String tmp = ollirTypes.nextTemp() + classSuffix;

        StringBuilder computation = new StringBuilder();
        computation.append(tmp).append(SPACE).append(ASSIGN).append(classSuffix).append(SPACE)
                .append("new(").append(className).append(")").append(classSuffix)
                .append(END_STMT);
        computation.append("invokespecial(").append(tmp).append(", \"<init>\"").append(")")
                .append(ollirTypes.toOllirType(TypeUtils.voidType()))
                .append(END_STMT);

        return new OllirExprResult(tmp, computation);
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
