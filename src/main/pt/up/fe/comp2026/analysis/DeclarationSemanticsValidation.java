package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class DeclarationSemanticsValidation extends SemanticValidationPass {

    public DeclarationSemanticsValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        addVisit(JmmKind.TYPE, this::visitType);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable ignored) {
        var methodOpt = types.getEnclosingMethod(methodDecl);
        if (methodOpt.isEmpty()) {
            return null;
        }

        var method = methodOpt.get();
        if (!TypeUtils.voidType().equals(method.returnType())
                && methodDecl.getDescendants(JmmKind.RETURN_STMT).isEmpty()) {
            addReport(newError(methodDecl,
                    "Method '" + method.name() + "' must contain a return statement"));
        }

        return null;
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        var typeName = typeNode.get("name");

        if (!types.isKnownTypeName(typeName)) {
            addReport(newError(typeNode, "Type '" + typeName + "' is not available in the current compilation unit"));
            return null;
        }

        if (!TypeUtils.voidType().equals(types.convertType(typeNode))) {
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
                declarationKind + " '" + parent.get("name") + "' cannot have type 'void'"));

        return null;
    }
}
