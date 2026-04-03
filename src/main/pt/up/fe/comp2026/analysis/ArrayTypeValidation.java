package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

public class ArrayTypeValidation extends TypeDeclarationValidationPass {

    private static final String JAVA_LANG_STRING = "java.lang.String";

    public ArrayTypeValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.TYPE, this::visitType);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitType(JmmNode typeNode, SymbolTable ignored) {
        if (!isOutermostTypeNode(typeNode) || !JmmKind.ARRAY_TYPE.check(typeNode)) {
            return null;
        }

        var resolvedType = types.convertType(typeNode);
        if (isSupportedArrayType(typeNode, resolvedType)) {
            return null;
        }

        addReport(newError(typeNode, unsupportedArrayTypeMessage(typeNode, resolvedType)));
        return null;
    }

    private boolean isSupportedArrayType(JmmNode typeNode, JmmType resolvedType) {
        if (!resolvedType.isArray()) {
            return false;
        }

        var arrayType = resolvedType.asArray();
        if (TypeUtils.intType().equals(arrayType.itemType())) {
            return true;
        }

        return arrayType.dimension() == 1
                && isJavaLangString(arrayType.itemType())
                && isMainMethodParameter(typeNode);
    }

    private boolean isJavaLangString(JmmType type) {
        return type.isClass()
                && JAVA_LANG_STRING.equals(type.asClass().fullyQualifiedName());
    }

    private String unsupportedArrayTypeMessage(JmmNode typeNode, JmmType resolvedType) {
        if (resolvedType.isArray()) {
            var arrayType = resolvedType.asArray();
            if (arrayType.dimension() == 1 && isJavaLangString(arrayType.itemType())) {
                return "Type 'String[]' is only supported as the parameter of the static void main method";
            }
        }

        var renderedType = getBaseTypeName(typeNode) + "[]".repeat(getArrayDepth(typeNode));
        return "Type '" + renderedType + "' is not supported; J-- only allows arrays of int";
    }
}
