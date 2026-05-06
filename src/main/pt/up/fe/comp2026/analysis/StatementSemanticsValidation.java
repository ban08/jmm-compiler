package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.ForStmtUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class StatementSemanticsValidation extends SemanticValidationPass {

    public StatementSemanticsValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.IF_STMT, this::visitIfStmt);
        addVisit(JmmKind.WHILE_STMT, this::visitWhileStmt);
        addVisit(JmmKind.DO_WHILE_STMT, this::visitDoWhileStmt);
        addVisit(JmmKind.FOR_STMT, this::visitForStmt);
        addVisit(JmmKind.FOR_HEADER_ASSIGN, this::visitForHeaderAssign);
        addVisit(JmmKind.ASSIGN_STMT, this::visitAssignStmt);
        addVisit(JmmKind.ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
        addVisit(JmmKind.RETURN_STMT, this::visitReturnStmt);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitIfStmt(JmmNode ifStmt, SymbolTable ignored) {
        validateBooleanCondition(ifStmt.getChild(0), "IF");
        return null;
    }

    private Void visitWhileStmt(JmmNode whileStmt, SymbolTable ignored) {
        validateBooleanCondition(whileStmt.getChild(0), "WHILE");
        return null;
    }

    private Void visitDoWhileStmt(JmmNode doWhileStmt, SymbolTable ignored) {
        var conditions = doWhileStmt.getChildren(JmmKind.EXPR);
        if (!conditions.isEmpty()) {
            validateBooleanCondition(conditions.getFirst(), "DO-WHILE");
        }

        return null;
    }

    private Void visitForStmt(JmmNode forStmt, SymbolTable ignored) {
        var parts = ForStmtUtils.split(forStmt);
        if (parts.condition() != null) {
            validateBooleanCondition(parts.condition(), "FOR");
        }

        return null;
    }

    private Void visitForHeaderAssign(JmmNode forHeaderAssign, SymbolTable ignored) {
        var targetName = forHeaderAssign.get(JmmAttributes.FOR_HEADER_ASSIGN.VAR);

        if (forHeaderAssign.getNumChildren() == 1) {
            validateSimpleAssignment(forHeaderAssign, targetName, forHeaderAssign.getChild(0));
            return null;
        }

        validateIndexedAssignment(forHeaderAssign, targetName);
        return null;
    }

    private Void visitAssignStmt(JmmNode assignStmt, SymbolTable ignored) {
        validateSimpleAssignment(assignStmt, assignStmt.get(JmmAttributes.ASSIGN_STMT.VAR), assignStmt.getChild(0));
        return null;
    }

    private Void visitArrayAssignStmt(JmmNode arrayAssignStmt, SymbolTable ignored) {
        validateIndexedAssignment(arrayAssignStmt, arrayAssignStmt.get(JmmAttributes.ARRAY_ASSIGN_STMT.VAR));
        return null;
    }

    private Void visitReturnStmt(JmmNode returnStmt, SymbolTable ignored) {
        var methodOpt = types.getEnclosingMethod(returnStmt);
        if (methodOpt.isEmpty()) {
            return null;
        }

        var method = methodOpt.get();
        var hasExpression = returnStmt.getNumChildren() > 0;

        if (TypeUtils.voidType().equals(method.returnType())) {
            if (hasExpression) {
                addReport(newError(returnStmt,
                        "Void method '" + method.name() + "' cannot return a value"));
            }

            return null;
        }

        if (!hasExpression) {
            addReport(newError(returnStmt,
                    "Method '" + method.name() + "' must return an expression of type '" +
                            method.returnType().print() + "'"));
            return null;
        }

        var exprType = types.getExprType(returnStmt.getChild(0));
        if (exprType != null && !types.isAssignable(method.returnType(), exprType)) {
            addReport(newError(returnStmt,
                    "Method '" + method.name() + "' returns '" + exprType.print() +
                            "' but expected '" + method.returnType().print() + "'"));
        }

        return null;
    }
}
