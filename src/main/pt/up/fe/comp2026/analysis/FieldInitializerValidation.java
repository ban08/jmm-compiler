package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Validates class field initializers against their declared type.
 */
public class FieldInitializerValidation extends AnalysisVisitorWithTable {

    public FieldInitializerValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.VAR_DECL, this::visitVarDecl);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitVarDecl(JmmNode varDecl, SymbolTable ignored) {
        if (!JmmKind.CLASS_DECL.check(varDecl.getParent())) {
            return null;
        }

        var initializerExprs = varDecl.getChildren(JmmKind.EXPR);
        if (initializerExprs.isEmpty()) {
            return null;
        }

        var declaredType = types.convertType(varDecl.getObject("typeNode", JmmNode.class));
        var initializerType = types.getExprType(initializerExprs.getFirst());

        if (initializerType != null && !types.isAssignable(declaredType, initializerType)) {
            addReport(newError(
                    varDecl,
                    "Initializer of field '" + varDecl.get("name") + "' has incompatible type '" +
                            initializerType.print() + "' (expected '" + declaredType.print() + "')"));
        }

        return null;
    }
}
