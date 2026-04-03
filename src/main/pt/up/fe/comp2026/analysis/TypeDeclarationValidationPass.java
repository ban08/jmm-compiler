package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.analysis.attributes.MethodDeclAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public abstract class TypeDeclarationValidationPass extends SemanticValidationPass {

    protected TypeDeclarationValidationPass(SymbolTable table) {
        super(table);
    }

    protected boolean isOutermostTypeNode(JmmNode typeNode) {
        var parent = typeNode.getParent();
        return parent == null || !JmmKind.TYPE.check(parent);
    }

    protected int getArrayDepth(JmmNode typeNode) {
        if (!JmmKind.ARRAY_TYPE.check(typeNode)) {
            return 0;
        }

        return 1 + getArrayDepth(typeNode.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), JmmNode.class));
    }

    protected String getBaseTypeName(JmmNode typeNode) {
        if (JmmKind.SIMPLE_TYPE.check(typeNode)) {
            return typeNode.get(JmmAttributes.SIMPLE_TYPE.NAME);
        }

        if (JmmKind.ARRAY_TYPE.check(typeNode)) {
            return getBaseTypeName(typeNode.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), JmmNode.class));
        }

        throw new IllegalArgumentException("Unexpected type node kind: " + typeNode.getKind());
    }

    protected boolean isMainMethodParameter(JmmNode typeNode) {
        var parent = typeNode.getParent();
        if (parent == null || !JmmKind.PARAM.check(parent)) {
            return false;
        }

        var methodDeclOpt = typeNode.getAncestor(JmmKind.METHOD_DECL);
        if (methodDeclOpt.isEmpty()) {
            return false;
        }

        var methodDecl = methodDeclOpt.get();
        var methodOpt = types.getEnclosingMethod(typeNode);
        if (methodOpt.isEmpty() || !isMainMethod(methodDecl, methodOpt.get())) {
            return false;
        }

        var params = methodDecl.getChildren(JmmKind.PARAM);
        return params.size() == 1 && params.getFirst() == parent;
    }

    protected boolean isMainMethod(JmmNode methodDecl, MethodSymbol method) {
        var cachedValue = MethodDeclAttributes.isMainMethod.getOptional(methodDecl);
        if (cachedValue.isPresent()) {
            return cachedValue.get();
        }

        var isMainMethod = "main".equals(methodDecl.get(JmmAttributes.METHOD_DECL.NAME))
                && method.isStatic()
                && pt.up.fe.comp2026.ast.TypeUtils.voidType().equals(method.returnType());
        MethodDeclAttributes.isMainMethod.set(methodDecl, isMainMethod);

        return isMainMethod;
    }

    protected String getDeclarationName(JmmNode declarationNode) {
        if (JmmKind.FIELD_DECL.check(declarationNode)) {
            return declarationNode.get(JmmAttributes.FIELD_DECL.NAME);
        }

        return declarationNode.get(JmmAttributes.VAR_DECL.NAME);
    }
}
