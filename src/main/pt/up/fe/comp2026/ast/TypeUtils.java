package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Utility methods regarding types.
 */
public class TypeUtils {

    public record ResolvedIdentifier(String name, JmmType type, AccessType accessType) {
    }

    public record ResolvedMethodCall(JmmType receiverType, MethodSymbol method) {
        public JmmType returnType() {
            return method.returnType();
        }
    }

    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static TypeUtils with(SymbolTable table) {
        return new TypeUtils(table);
    }

    public static JmmPrimitiveType intType() {
        return JmmPrimitiveType.INT;
    }

    public static JmmPrimitiveType booleanType() {
        return JmmPrimitiveType.BOOLEAN;
    }

    public static JmmPrimitiveType voidType() {
        return JmmPrimitiveType.VOID;
    }

    public JmmType convertType(JmmNode typeNode) {
        TYPE.check(typeNode);

        var name = typeNode.get("name");
        var isArray = NodeUtils.getBooleanAttribute(typeNode, "isArray", "false");
        var arrayDepth = NodeUtils.getIntegerAttribute(typeNode, "arrayDepth", isArray ? "1" : "0");

        var primitive = JmmPrimitiveType.fromString(name);
        if (primitive.isPresent()) {
            return wrapArrayType(primitive.get(), arrayDepth);
        }

        var classType = resolveClassType(name, false);
        return wrapArrayType(classType, arrayDepth);
    }

    public boolean isKnownTypeName(String name) {
        return JmmPrimitiveType.fromString(name).isPresent()
                || name.equals(table.getClassName())
                || table.getImportedFullyQualifiedName(name)
                .flatMap(table::getImportedSymbolTable)
                .isPresent()
                || table.getImplicitImport(name).isPresent();
    }

    /**
     * Gets the {@link JmmType} of an arbitrary expression.
     */
    public JmmType getExprType(JmmNode expr) {
        return tryGetExprType(expr).orElse(null);
    }

    public Optional<JmmType> tryGetExprType(JmmNode expr) {
        return switch (expr.getKind()) {
            case PAREN_EXPR -> tryGetExprType(expr.getChild(0));
            case INTEGER_LITERAL -> Optional.of(intType());
            case BOOLEAN_LITERAL -> Optional.of(booleanType());
            case VAR_REF_EXPR -> Optional.ofNullable(getVarExprType(expr));
            case THIS_EXPR -> Optional.of(JmmClassType.ofInstance(table.getFullyQualifiedName(), false));
            case NOT_EXPR -> Optional.of(booleanType());
            case UNARY_EXPR -> Optional.of(intType());
            case NEW_INT_ARRAY_EXPR -> Optional.of(wrapArrayType(intType(), expr.getNumChildren()));
            case ARRAY_INITIALIZER_EXPR -> Optional.of(JmmArrayType.of(intType()));
            case NEW_EXPR -> Optional.of(resolveClassType(expr.get("name"), false));
            case BINARY_EXPR -> Optional.of(getBinExprType(expr));
            case LENGTH_EXPR -> Optional.of(intType());
            case FIELD_ACCESS_EXPR -> resolveFieldAccessType(expr);
            case ARRAY_ACCESS_EXPR -> getArrayElementType(expr.getChild(0));
            case METHOD_CALL_EXPR -> resolveMethodCall(expr).map(ResolvedMethodCall::returnType);
            case IMPLICIT_THIS_CALL_EXPR -> resolveImplicitThisMethodCall(expr).map(ResolvedMethodCall::returnType);
            default -> Optional.empty();
        };
    }

    public Signature getMethodDeclSignature(JmmNode methodDecl) {
        METHOD_DECL.check(methodDecl);

        var methodName = methodDecl.get("name");
        var paramTypes = methodDecl.getChildren(PARAM).stream()
                .map(param -> convertType(param.getObject("typeNode", JmmNode.class)))
                .toList();

        return new Signature(methodName, paramTypes);
    }

    public Optional<MethodSymbol> getEnclosingMethod(JmmNode node) {
        var methodNode = METHOD_DECL.check(node) ? Optional.of(node) : node.getAncestor(METHOD_DECL);
        return methodNode.flatMap(method -> table.getMethod(getMethodDeclSignature(method)));
    }

    public boolean isStaticMethodContext(JmmNode node) {
        return getEnclosingMethod(node).map(MethodSymbol::isStatic).orElse(false);
    }

    public Optional<ResolvedIdentifier> resolveIdentifier(JmmNode context, String name) {
        var method = getEnclosingMethod(context);
        if (method.isPresent()) {
            var local = method.get().getLocalVariable(name);
            if (local.isPresent()) {
                return Optional.of(new ResolvedIdentifier(name, local.get().type(), AccessType.LOCAL));
            }

            var parameter = method.get().getParameter(name);
            if (parameter.isPresent()) {
                return Optional.of(new ResolvedIdentifier(name, parameter.get().type(), AccessType.PARAM));
            }
        }

        var field = table.getField(name);
        if (field.isPresent()) {
            return Optional.of(new ResolvedIdentifier(name, field.get().type(), AccessType.FIELD));
        }

        var importedClass = table.getImportedFullyQualifiedName(name);
        if (importedClass.isPresent() && table.getImportedSymbolTable(importedClass.get()).isPresent()) {
            return Optional.of(new ResolvedIdentifier(name,
                    JmmClassType.ofStaticReference(importedClass.get(), true),
                    AccessType.IMPORT));
        }

        var implicitClass = table.getImplicitImport(name);
        if (implicitClass.isPresent()) {
            return Optional.of(new ResolvedIdentifier(name,
                    JmmClassType.ofStaticReference(implicitClass.get().getFullyQualifiedName(), true),
                    AccessType.IMPORT));
        }

        return Optional.empty();
    }

    public Optional<ResolvedMethodCall> resolveMethodCall(JmmNode callExpr) {
        METHOD_CALL_EXPR.check(callExpr);

        var receiverType = tryGetExprType(callExpr.getChild(0));
        if (receiverType.isEmpty() || !receiverType.get().isClass()) {
            return Optional.empty();
        }

        var argTypes = new ArrayList<JmmType>();
        for (int i = 1; i < callExpr.getNumChildren(); i++) {
            var argType = tryGetExprType(callExpr.getChild(i));
            if (argType.isEmpty()) {
                return Optional.empty();
            }
            argTypes.add(argType.get());
        }

        return resolveMethodCall(receiverType.get().asClass(), callExpr.get("name"), argTypes);
    }

    public Optional<ResolvedMethodCall> resolveImplicitThisMethodCall(JmmNode callExpr) {
        IMPLICIT_THIS_CALL_EXPR.check(callExpr);

        var argTypes = new ArrayList<JmmType>();
        for (int i = 0; i < callExpr.getNumChildren(); i++) {
            var argType = tryGetExprType(callExpr.getChild(i));
            if (argType.isEmpty()) {
                return Optional.empty();
            }

            argTypes.add(argType.get());
        }

        var receiverType = isStaticMethodContext(callExpr)
                ? JmmClassType.ofStaticReference(table.getFullyQualifiedName(), false)
                : JmmClassType.ofInstance(table.getFullyQualifiedName(), false);

        return resolveMethodCall(receiverType, callExpr.get("name"), argTypes);
    }

    public boolean isAssignable(JmmType targetType, JmmType valueType) {
        if (targetType == null || valueType == null) {
            return false;
        }

        if (targetType.equals(valueType)) {
            return true;
        }

        if (targetType.isArray() || valueType.isArray()) {
            if (!targetType.isArray() || !valueType.isArray()) {
                return false;
            }

            var targetArray = targetType.asArray();
            var valueArray = valueType.asArray();
            return targetArray.dimension() == valueArray.dimension()
                    && isAssignable(targetArray.itemType(), valueArray.itemType());
        }

        if (targetType.isPrimitive() || valueType.isPrimitive()) {
            return targetType.equals(valueType);
        }

        var targetClass = targetType.asClass();
        var valueClass = valueType.asClass();
        if (sameClass(targetClass, valueClass)) {
            return true;
        }

        if (isCurrentClass(targetClass)) {
            return false;
        }

        if (isCurrentClass(valueClass)) {
            var superQualifiedName = table.getSuperFullyQualifiedName();
            return superQualifiedName != null
                    && (sameClass(targetClass.fullyQualifiedName(), superQualifiedName)
                    || isJavaAssignable(targetClass.fullyQualifiedName(), superQualifiedName));
        }

        return isJavaAssignable(targetClass.fullyQualifiedName(), valueClass.fullyQualifiedName());
    }

    public boolean areComparable(JmmType leftType, JmmType rightType) {
        if (leftType == null || rightType == null) {
            return false;
        }

        if (leftType.isPrimitive() || rightType.isPrimitive()) {
            return leftType.equals(rightType);
        }

        return isAssignable(leftType, rightType) || isAssignable(rightType, leftType);
    }

    private JmmType getBinExprType(JmmNode binaryExpr) {
        var operator = binaryExpr.get("op");

        return switch (operator) {
            case "+", "-", "*", "/", "%" -> intType();
            case "<", ">", "<=", ">=", "==", "!=", "&&", "||" -> booleanType();
            default -> throw new RuntimeException("Unknown operator '" + operator + "' of expression '" + binaryExpr + "'");
        };
    }

    private Optional<JmmType> getArrayElementType(JmmNode arrayExpr) {
        var arrayType = tryGetExprType(arrayExpr);
        if (arrayType.isEmpty() || !arrayType.get().isArray()) {
            return Optional.empty();
        }

        return Optional.of(arrayType.get().asArray().itemType());
    }

    public Optional<JmmType> resolveFieldAccessType(JmmNode fieldAccessExpr) {
        FIELD_ACCESS_EXPR.check(fieldAccessExpr);

        var receiverType = tryGetExprType(fieldAccessExpr.getChild(0));
        if (receiverType.isEmpty() || !receiverType.get().isClass()) {
            return Optional.empty();
        }

        var fieldName = fieldAccessExpr.get("name");
        var receiverClass = receiverType.get().asClass();

        if (isCurrentClass(receiverClass)) {
            var localField = table.getField(fieldName);
            if (localField.isPresent()) {
                return Optional.of(localField.get().type());
            }

            var superQualifiedName = table.getSuperFullyQualifiedName();
            if (superQualifiedName == null) {
                return Optional.empty();
            }

            return table.getImportedSymbolTable(superQualifiedName)
                    .flatMap(symbolTable -> symbolTable.getField(fieldName))
                    .map(Symbol::type);
        }

        return table.getImportedSymbolTable(receiverClass.fullyQualifiedName())
                .flatMap(symbolTable -> symbolTable.getField(fieldName))
                .map(Symbol::type);
    }

    private JmmType getVarExprType(JmmNode varRefExpr) {
        return resolveIdentifier(varRefExpr, varRefExpr.get("name"))
                .map(ResolvedIdentifier::type)
                .orElse(null);
    }

    private Optional<ResolvedMethodCall> resolveMethodCall(JmmClassType receiverType, String methodName, List<JmmType> argTypes) {
        var requireStatic = receiverType.staticRef();

        if (isCurrentClass(receiverType)) {
            var localMethod = findMatchingMethod(table.getMethods(methodName), argTypes, requireStatic);
            if (localMethod.isPresent()) {
                return Optional.of(new ResolvedMethodCall(receiverType, localMethod.get()));
            }

            var superQualifiedName = table.getSuperFullyQualifiedName();
            if (superQualifiedName == null) {
                return Optional.empty();
            }

            var superTable = table.getImportedSymbolTable(superQualifiedName);
            if (superTable.isEmpty()) {
                return Optional.empty();
            }

            return findMatchingMethod(superTable.get().getMethods(methodName), argTypes, requireStatic)
                    .map(method -> new ResolvedMethodCall(receiverType, method));
        }

        var importedTable = table.getImportedSymbolTable(receiverType.fullyQualifiedName());
        if (importedTable.isEmpty()) {
            return Optional.empty();
        }

        return findMatchingMethod(importedTable.get().getMethods(methodName), argTypes, requireStatic)
                .map(method -> new ResolvedMethodCall(receiverType, method));
    }

    private Optional<MethodSymbol> findMatchingMethod(List<MethodSymbol> methods, List<JmmType> argTypes, boolean requireStatic) {
        for (var method : methods) {
            if (requireStatic && !method.isStatic()) {
                continue;
            }

            if (method.parameters().size() != argTypes.size()) {
                continue;
            }

            var matches = true;
            for (int i = 0; i < argTypes.size(); i++) {
                if (!isAssignable(method.parameters().get(i).type(), argTypes.get(i))) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return Optional.of(method);
            }
        }

        return Optional.empty();
    }

    private JmmClassType resolveClassType(String className, boolean staticReference) {
        var importedFqn = table.getImportedFullyQualifiedName(className);
        if (importedFqn.isPresent() && table.getImportedSymbolTable(importedFqn.get()).isPresent()) {
            return staticReference
                    ? JmmClassType.ofStaticReference(importedFqn.get(), true)
                    : JmmClassType.ofInstance(importedFqn.get(), true);
        }

        if (className.equals(table.getClassName())) {
            return staticReference
                    ? JmmClassType.ofStaticReference(table.getFullyQualifiedName(), false)
                    : JmmClassType.ofInstance(table.getFullyQualifiedName(), false);
        }

        var implicitImport = table.getImplicitImport(className);
        if (implicitImport.isPresent()) {
            var fullyQualifiedName = implicitImport.get().getFullyQualifiedName();
            return staticReference
                    ? JmmClassType.ofStaticReference(fullyQualifiedName, true)
                    : JmmClassType.ofInstance(fullyQualifiedName, true);
        }

        return staticReference
                ? JmmClassType.ofStaticReference(className, false)
                : JmmClassType.ofInstance(className, false);
    }

    private boolean isCurrentClass(JmmClassType classType) {
        return sameClass(classType.fullyQualifiedName(), table.getFullyQualifiedName())
                || classType.name().equals(table.getClassName());
    }

    private boolean sameClass(JmmClassType left, JmmClassType right) {
        return sameClass(left.fullyQualifiedName(), right.fullyQualifiedName());
    }

    private boolean sameClass(String left, String right) {
        if (left.equals(right)) {
            return true;
        }

        return simpleName(left).equals(simpleName(right));
    }

    private String simpleName(String className) {
        var lastDot = className.lastIndexOf('.');
        return lastDot == -1 ? className : className.substring(lastDot + 1);
    }

    private boolean isJavaAssignable(String targetClass, String valueClass) {
        try {
            var target = Class.forName(targetClass);
            var value = Class.forName(valueClass);
            return target.isAssignableFrom(value);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private JmmType wrapArrayType(JmmType baseType, int arrayDepth) {
        var currentType = baseType;
        for (int i = 0; i < arrayDepth; i++) {
            currentType = JmmArrayType.of(currentType);
        }

        return currentType;
    }
}
