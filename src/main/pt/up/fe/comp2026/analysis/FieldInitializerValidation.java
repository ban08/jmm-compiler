package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
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
        addVisit(JmmKind.FIELD_DECL, this::visitFieldDecl);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitFieldDecl(JmmNode fieldDecl, SymbolTable ignored) {
        var initializerExprs = fieldDecl.getChildren(JmmKind.EXPR);
        if (initializerExprs.isEmpty()) {
            return null;
        }

        var declaredType = types.convertType(fieldDecl.getObject(JmmAttributes.FIELD_DECL.TYPE_NODE.getKey(), JmmNode.class));
        var initializerType = types.getExprType(initializerExprs.getFirst());

        if (initializerType != null && !types.isAssignable(declaredType, initializerType)) {
            addReport(newError(
                    fieldDecl,
                    "Initializer of field '" + fieldDecl.get(JmmAttributes.FIELD_DECL.NAME) + "' has incompatible type '" +
                            initializerType.print() + "' (expected '" + declaredType.print() + "')"));
        }

        return null;
    }
}
