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
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;

import java.util.ArrayList;
import java.util.HashSet;
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

        if (ARRAY_TYPE.check(typeNode)) {
            var elementType = typeNode.getObject(JmmAttributes.ARRAY_TYPE.ELEMENT_TYPE.getKey(), JmmNode.class);
            return wrapArrayType(convertType(elementType), 1);
        }

        var name = typeNode.get(JmmAttributes.SIMPLE_TYPE.NAME);
        var primitive = JmmPrimitiveType.fromString(name);
        if (primitive.isPresent()) {
            return primitive.get();
        }

        return resolveClassType(name, false);
    }

    public boolean isKnownTypeName(String name) {
        return JmmPrimitiveType.fromString(name).isPresent()
                || table.resolveClass(name).isPresent();
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
            case NEW_INT_ARRAY_EXPR -> Optional.of(getNewIntArrayExprType(expr));
            case ARRAY_INITIALIZER_EXPR -> Optional.of(JmmArrayType.of(intType()));
            case NEW_EXPR -> Optional.of(resolveClassType(expr.get(JmmAttributes.NEW_EXPR.NAME), false));
            case BINARY_EXPR -> Optional.of(getBinExprType(expr));
            case FIELD_ACCESS_EXPR -> resolveFieldAccessType(expr);
            case ARRAY_ACCESS_EXPR -> getArrayElementType(expr.getChild(0));
            case METHOD_CALL_EXPR -> resolveMethodCall(expr).map(ResolvedMethodCall::returnType);
            case IMPLICIT_THIS_CALL_EXPR -> resolveImplicitThisMethodCall(expr).map(ResolvedMethodCall::returnType);
            default -> Optional.empty();
        };
    }

    public Signature getMethodDeclSignature(JmmNode methodDecl) {
        METHOD_DECL.check(methodDecl);

        var methodName = methodDecl.get(JmmAttributes.METHOD_DECL.NAME);
        var paramTypes = methodDecl.getChildren(PARAM).stream()
                .map(param -> convertType(param.getObject(JmmAttributes.PARAM.TYPE_NODE.getKey(), JmmNode.class)))
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

        var inheritedField = findFieldInHierarchy(table.getSuperFullyQualifiedName(), name);
        if (inheritedField.isPresent()) {
            return Optional.of(new ResolvedIdentifier(name, inheritedField.get().type(), AccessType.FIELD));
        }

        return table.resolveClass(name)
                .map(resolvedClass -> new ResolvedIdentifier(
                        name,
                        resolvedClass.asType(true),
                        resolvedClass.imported() ? AccessType.IMPORT : AccessType.CLASS
                ));
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

        return resolveMethodCall(receiverType.get().asClass(), callExpr.get(JmmAttributes.METHOD_CALL_EXPR.NAME), argTypes);
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

        return resolveMethodCall(receiverType, callExpr.get(JmmAttributes.IMPLICIT_THIS_CALL_EXPR.NAME), argTypes);
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
        return isClassAssignable(targetClass.fullyQualifiedName(), valueClass.fullyQualifiedName());
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
        var operator = binaryExpr.get(JmmAttributes.BINARY_EXPR.OP);

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

        var resolvedArray = arrayType.get().asArray();
        if (resolvedArray.dimension() == 1) {
            return Optional.of(resolvedArray.itemType());
        }

        return Optional.of(JmmArrayType.of(resolvedArray.itemType(), resolvedArray.dimension() - 1));
    }

    private JmmType getNewIntArrayExprType(JmmNode newArrayExpr) {
        NEW_INT_ARRAY_EXPR.check(newArrayExpr);

        return wrapArrayType(intType(), newArrayExpr.getChildren(ARRAY_CREATION_DIM).size());
    }

    public Optional<JmmType> resolveFieldAccessType(JmmNode fieldAccessExpr) {
        FIELD_ACCESS_EXPR.check(fieldAccessExpr);

        var receiverType = tryGetExprType(fieldAccessExpr.getChild(0));
        if (receiverType.isEmpty()) {
            return Optional.empty();
        }

        var fieldName = fieldAccessExpr.get(JmmAttributes.FIELD_ACCESS_EXPR.NAME);
        if (receiverType.get().isArray()) {
            return "length".equals(fieldName) ? Optional.of(intType()) : Optional.empty();
        }

        if (!receiverType.get().isClass()) {
            return Optional.empty();
        }

        var receiverClass = receiverType.get().asClass();

        if (receiverClass.staticRef()) {
            return Optional.empty();
        }

        if (isCurrentClass(receiverClass)) {
            var localField = table.getField(fieldName);
            if (localField.isPresent()) {
                return Optional.of(localField.get().type());
            }

            return findFieldInHierarchy(table.getSuperFullyQualifiedName(), fieldName)
                    .map(Symbol::type);
        }

        return findFieldInHierarchy(receiverClass.fullyQualifiedName(), fieldName)
                .map(Symbol::type);
    }

    private JmmType getVarExprType(JmmNode varRefExpr) {
        var resolved = resolveIdentifier(varRefExpr, varRefExpr.get(JmmAttributes.VAR_REF_EXPR.NAME));
        if (resolved.isEmpty()) {
            return null;
        }

        var accessType = resolved.get().accessType();
        if ((accessType == AccessType.CLASS || accessType == AccessType.IMPORT)
                && !isClassIdentifierReceiverContext(varRefExpr)) {
            return null;
        }

        return resolved.get().type();
    }

    public boolean isClassIdentifierReceiverContext(JmmNode varRefExpr) {
        if (!VAR_REF_EXPR.check(varRefExpr)) {
            return false;
        }

        var parent = varRefExpr.getParent();
        if (parent == null) {
            return false;
        }

        if ((METHOD_CALL_EXPR.check(parent) || FIELD_ACCESS_EXPR.check(parent))
                && parent.getNumChildren() > 0
                && parent.getChild(0) == varRefExpr) {
            return true;
        }

        return false;
    }

    private Optional<ResolvedMethodCall> resolveMethodCall(JmmClassType receiverType, String methodName, List<JmmType> argTypes) {
        var requireStatic = receiverType.staticRef();

        if (isCurrentClass(receiverType)) {
            var localMethod = findMatchingMethod(table.getMethods(methodName), argTypes, requireStatic);
            if (localMethod.isPresent()) {
                return Optional.of(new ResolvedMethodCall(receiverType, localMethod.get()));
            }

            return findMatchingMethodInHierarchy(table.getSuperFullyQualifiedName(), methodName, argTypes, requireStatic)
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

    /**
     * Walks the imported superclass chain and returns the first overload-compatible method.
     * This keeps expression-type inference aligned with semantic validation, which already
     * searches inherited methods recursively.
     */
    private Optional<MethodSymbol> findMatchingMethodInHierarchy(String classQualifiedName,
                                                                 String methodName,
                                                                 List<JmmType> argTypes,
                                                                 boolean requireStatic) {
        var visitedClasses = new HashSet<String>();
        var currentClass = classQualifiedName;

        while (currentClass != null && visitedClasses.add(currentClass)) {
            var symbolTable = table.getImportedSymbolTable(currentClass);
            if (symbolTable.isEmpty()) {
                return Optional.empty();
            }

            var method = findMatchingMethod(symbolTable.get().getMethods(methodName), argTypes, requireStatic);
            if (method.isPresent()) {
                return method;
            }

            currentClass = symbolTable.get().getSuperFullyQualifiedName();
        }

        return Optional.empty();
    }

    private JmmClassType resolveClassType(String className, boolean staticReference) {
        return table.resolveClass(className)
                .map(resolvedClass -> resolvedClass.asType(staticReference))
                .orElseGet(() -> staticReference
                        ? JmmClassType.ofStaticReference(className, false)
                        : JmmClassType.ofInstance(className, false));
    }

    private boolean isCurrentClass(JmmClassType classType) {
        return classType.fullyQualifiedName().equals(table.getFullyQualifiedName());
    }

    private boolean sameClass(JmmClassType left, JmmClassType right) {
        return sameClass(left.fullyQualifiedName(), right.fullyQualifiedName());
    }

    private boolean sameClass(String left, String right) {
        return left.equals(right);
    }

    private boolean isClassAssignable(String targetClass, String valueClass) {
        var visited = new HashSet<String>();
        var currentClass = valueClass;

        while (currentClass != null && visited.add(currentClass)) {
            if (sameClass(targetClass, currentClass)) {
                return true;
            }

            currentClass = getDirectSuperClass(currentClass);
        }

        return false;
    }

    private String getDirectSuperClass(String classQualifiedName) {
        if (sameClass(classQualifiedName, table.getFullyQualifiedName())) {
            return table.getSuperFullyQualifiedName();
        }

        return table.getImportedSymbolTable(classQualifiedName)
                .map(SymbolTable::getSuperFullyQualifiedName)
                .orElse(null);
    }

    // Imported symbol tables expose only the fields declared in that class, so we
    // need to walk the superclass chain ourselves to resolve inherited fields.
    private Optional<Symbol> findFieldInHierarchy(String classQualifiedName, String fieldName) {
        var visited = new HashSet<String>();
        var currentClass = classQualifiedName;

        while (currentClass != null && visited.add(currentClass)) {
            var symbolTable = table.getImportedSymbolTable(currentClass);
            if (symbolTable.isEmpty()) {
                return Optional.empty();
            }

            var field = symbolTable.get().getField(fieldName);
            if (field.isPresent()) {
                return field;
            }

            currentClass = symbolTable.get().getSuperFullyQualifiedName();
        }

        return Optional.empty();
    }

    private JmmType wrapArrayType(JmmType baseType, int arrayDepth) {
        if (arrayDepth <= 0) {
            return baseType;
        }

        if (baseType.isArray()) {
            var arrayType = baseType.asArray();
            return JmmArrayType.of(arrayType.itemType(), arrayType.dimension() + arrayDepth);
        }

        return JmmArrayType.of(baseType, arrayDepth);
    }
}
