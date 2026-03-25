package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;

public class EntityAccessValidation extends SemanticValidationPass {

    public EntityAccessValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.NEW_EXPR, this::visitNewExpr);
        addVisit(JmmKind.VAR_REF_EXPR, this::visitVarRefExpr);
        addVisit(JmmKind.THIS_EXPR, this::visitThisExpr);
        addVisit(JmmKind.METHOD_CALL_EXPR, this::visitMethodCallExpr);
        addVisit(JmmKind.IMPLICIT_THIS_CALL_EXPR, this::visitImplicitThisCallExpr);
        addVisit(JmmKind.FIELD_ACCESS_EXPR, this::visitFieldAccessExpr);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitNewExpr(JmmNode newExpr, SymbolTable ignored) {
        var className = newExpr.get("name");
        if (!types.isKnownTypeName(className)) {
            addReport(newError(newExpr, "Class '" + className + "' is not imported"));
            return null;
        }

        var argTypes = new ArrayList<JmmType>();
        for (var argExpr : newExpr.getChildren(JmmKind.EXPR)) {
            var argType = types.tryGetExprType(argExpr);
            if (argType.isEmpty()) {
                return null;
            }

            argTypes.add(argType.get());
        }

        if (argTypes.isEmpty()) {
            return null;
        }

        var newTypeOpt = types.tryGetExprType(newExpr);
        if (newTypeOpt.isEmpty() || !newTypeOpt.get().isClass()) {
            return null;
        }

        var classType = newTypeOpt.get().asClass();
        if (isCurrentClass(classType)) {
            addReport(newError(newExpr,
                    "Class '" + className + "' does not support constructors with arguments"));
            return null;
        }

        if (!hasLoadableConstructorOwner(classType)) {
            return null;
        }

        var constructors = getPublicConstructors(classType.fullyQualifiedName());
        if (constructors.isEmpty()) {
            addReport(newError(newExpr,
                    "Class '" + className + "' does not define a public constructor"));
            return null;
        }

        if (hasMatchingConstructor(constructors, argTypes)) {
            return null;
        }

        var anyWithSameCount = constructors.stream()
                .anyMatch(constructor -> !constructor.isVarArgs()
                        && constructor.getParameterCount() == argTypes.size());

        if (!anyWithSameCount) {
            addReport(newError(newExpr,
                    "Wrong number of arguments for constructor of class '" + className + "'"));
            return null;
        }

        addReport(newError(newExpr,
                "Wrong argument types for constructor of class '" + className + "'"));
        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable ignored) {
        var identifier = varRefExpr.get("name");
        var resolved = types.resolveIdentifier(varRefExpr, identifier);

        if (resolved.isEmpty()) {
            addReport(newError(varRefExpr, "Identifier '" + identifier + "' is not declared"));
            return null;
        }

        if (resolved.get().accessType() == AccessType.FIELD && types.isStaticMethodContext(varRefExpr)) {
            addReport(newError(varRefExpr, "Field '" + identifier + "' cannot be accessed from a static method"));
        }

        return null;
    }

    private Void visitThisExpr(JmmNode thisExpr, SymbolTable ignored) {
        if (types.isStaticMethodContext(thisExpr)) {
            addReport(newError(thisExpr, "Keyword 'this' cannot be used inside a static method"));
        }

        return null;
    }

    private Void visitMethodCallExpr(JmmNode methodCallExpr, SymbolTable ignored) {
        var receiverType = types.getExprType(methodCallExpr.getChild(0));
        if (receiverType == null) {
            return null;
        }

        if (!receiverType.isClass()) {
            addReport(newError(methodCallExpr, "Method calls require a class receiver"));
            return null;
        }

        if (isCurrentClass(receiverType)) {
            validateCurrentClassMethodCall(methodCallExpr, 1, receiverType.asClass().staticRef());
            return null;
        }

        var receiverClassType = receiverType.asClass();
        var resolvedOpt = types.resolveMethodCall(methodCallExpr);
        if (resolvedOpt.isEmpty()) {
            var importedTableOpt = table.getImportedSymbolTable(receiverClassType.fullyQualifiedName());
            if (importedTableOpt.isPresent()) {
                validateImportedMethodCall(methodCallExpr, receiverClassType);
            } else {
                addReport(newError(methodCallExpr, "Imported class '" + receiverClassType.fullyQualifiedName() + "' not found or has no symbol table"));
            }
        }

        return null;
    }

    private void validateImportedMethodCall(JmmNode methodCallExpr, pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType receiverClassType) {
        var methodName = methodCallExpr.get("name");
        var methods = table.getImportedSymbolTable(receiverClassType.fullyQualifiedName())
                .orElseThrow()
                .getMethods(methodName);

        if (methods.isEmpty()) {
            addReport(newError(methodCallExpr,
                    "Method '" + methodName + "' does not exist in imported class '" +
                            receiverClassType.fullyQualifiedName() + "'"));
            return;
        }

        var argTypesOpt = getArgumentTypes(methodCallExpr, 1);
        if (argTypesOpt.isEmpty()) {
            return;
        }

        var argTypes = argTypesOpt.get();
        var requireStatic = receiverClassType.staticRef();

        if (requireStatic && findMatchingMethod(methods, argTypes, false) != null) {
            addReport(newError(methodCallExpr,
                    "Method '" + methodName + "' is not static and cannot be called from a static context"));
            return;
        }

        var visibleMethods = requireStatic
                ? methods.stream().filter(method -> method.isStatic()).toList()
                : methods;

        if (visibleMethods.isEmpty()) {
            addReport(newError(methodCallExpr,
                    "Method '" + methodName + "' is not static and cannot be called from a static context"));
            return;
        }

        var anyWithSameCount = visibleMethods.stream()
                .anyMatch(method -> method.parameters().size() == argTypes.size());

        if (!anyWithSameCount) {
            addReport(newError(methodCallExpr,
                    "Wrong number of arguments for method '" + methodName + "' in imported class '" +
                            receiverClassType.fullyQualifiedName() + "'"));
            return;
        }

        addReport(newError(methodCallExpr,
                "Wrong argument types for method '" + methodName + "' in imported class '" +
                        receiverClassType.fullyQualifiedName() + "'"));
    }

    private Void visitImplicitThisCallExpr(JmmNode implicitThisCallExpr, SymbolTable ignored) {
        validateCurrentClassMethodCall(
                implicitThisCallExpr,
                0,
                types.isStaticMethodContext(implicitThisCallExpr)
        );

        return null;
    }

    private Void visitFieldAccessExpr(JmmNode fieldAccessExpr, SymbolTable ignored) {
        var receiverType = types.getExprType(fieldAccessExpr.getChild(0));
        if (receiverType == null) {
            return null;
        }

        if (!receiverType.isClass()) {
            addReport(newError(fieldAccessExpr, "Field access requires a class receiver"));
            return null;
        }

        if (receiverType.asClass().staticRef()) {
            addReport(newError(fieldAccessExpr,
                    "Field '" + fieldAccessExpr.get("name") + "' requires an instance receiver"));
            return null;
        }

        if (types.resolveFieldAccessType(fieldAccessExpr).isEmpty()) {
            addReport(newError(fieldAccessExpr,
                    "Field '" + fieldAccessExpr.get("name") + "' is not available on receiver '" +
                            receiverType.print() + "'"));
        }

        return null;
    }
}
