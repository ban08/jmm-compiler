package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class VoidTypeValidation extends TypeDeclarationValidationPass {

    public VoidTypeValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.TYPE, this::visitType);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        if (!isOutermostTypeNode(typeNode) || !TypeUtils.voidType().equals(types.convertType(typeNode))) {
            return null;
        }

        var parent = typeNode.getParent();
        if (parent == null) {
            return null;
        }

        if (JmmKind.PARAM.check(parent)) {
            addReport(newError(typeNode,
                    "Parameter '" + parent.get(JmmAttributes.PARAM.NAME) + "' cannot have type 'void'"));
            return null;
        }

        if (!JmmKind.VAR_DECL.check(parent) && !JmmKind.FIELD_DECL.check(parent)) {
            return null;
        }

        var declarationKind = JmmKind.FIELD_DECL.check(parent) ? "Field" : "Local variable";
        addReport(newError(typeNode,
                declarationKind + " '" + getDeclarationName(parent) + "' cannot have type 'void'"));

        return null;
    }
}
