package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class TypeExistenceValidation extends TypeDeclarationValidationPass {

    public TypeExistenceValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.TYPE, this::visitType);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        if (!isOutermostTypeNode(typeNode)) {
            return null;
        }

        var typeName = getBaseTypeName(typeNode);
        if (!types.isKnownTypeName(typeName)) {
            addReport(newError(typeNode,
                    "Type '" + typeName + "' is not available in the current compilation unit"));
        }

        return null;
    }
}
