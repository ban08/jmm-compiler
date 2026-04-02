package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
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
        var className = newExpr.get(JmmAttributes.NEW_EXPR.NAME);
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

        var newTypeOpt = types.tryGetExprType(newExpr);
        if (newTypeOpt.isEmpty() || !newTypeOpt.get().isClass()) {
            return null;
        }

        var classType = newTypeOpt.get().asClass();
        if (isCurrentClass(classType)) {
            if (!argTypes.isEmpty()) {
                addReport(newError(newExpr,
                        "Class '" + className + "' does not support constructors with arguments"));
            }
            return null;
        }

        var importedTableOpt = table.getImportedSymbolTable(classType.fullyQualifiedName());
        if (importedTableOpt.isEmpty()) {
            addReport(newError(newExpr,
                    "Imported class '" + classType.fullyQualifiedName() + "' not found or has no symbol table"));
            return null;
        }

        var constructors = importedTableOpt.get().getMethods("<init>");
        if (constructors.isEmpty()) {
            addReport(newError(newExpr,
                    "Class '" + className + "' does not define a public constructor"));
            return null;
        }

        var constructorMatch = types.analyzeMethodCandidates(constructors, argTypes, false);
        if (constructorMatch.matchingMethod().isPresent()) {
            return null;
        }

        if (!constructorMatch.hasSameArgumentCount()) {
            addReport(newError(newExpr,
                    "Wrong number of arguments for constructor of class '" + className + "'"));
            return null;
        }

        addReport(newError(newExpr,
                "Wrong argument types for constructor of class '" + className + "'"));
        return null;
    }

    private Void visitVarRefExpr(JmmNode varRefExpr, SymbolTable ignored) {
        var identifier = varRefExpr.get(JmmAttributes.VAR_REF_EXPR.NAME);
        var resolved = types.resolveIdentifier(varRefExpr, identifier);

        if (resolved.isEmpty()) {
            addReport(newError(varRefExpr, "Identifier '" + identifier + "' is not declared"));
            return null;
        }

        if (resolved.get().accessType() == AccessType.FIELD && types.isStaticMethodContext(varRefExpr)) {
            addReport(newError(varRefExpr, "Field '" + identifier + "' cannot be accessed from a static method"));
        }

        if ((resolved.get().accessType() == AccessType.CLASS || resolved.get().accessType() == AccessType.IMPORT)
                && !types.isClassIdentifierReceiverContext(varRefExpr)) {
            addReport(newError(varRefExpr,
                    "Class identifier '" + identifier + "' cannot be used as a value expression"));
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
        var methodName = methodCallExpr.get(JmmAttributes.METHOD_CALL_EXPR.NAME);
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
        var matchAnalysis = types.analyzeMethodCandidates(methods, argTypes, requireStatic);

        if (matchAnalysis.matchingMethod().isPresent()) {
            return;
        }

        if (matchAnalysis.hasStaticContextMismatch()) {
            addReport(newError(methodCallExpr,
                    "Method '" + methodName + "' is not static and cannot be called from a static context"));
            return;
        }

        if (!matchAnalysis.hasVisibleMethods()) {
            addReport(newError(methodCallExpr,
                    "Method '" + methodName + "' is not static and cannot be called from a static context"));
            return;
        }

        if (!matchAnalysis.hasSameArgumentCount()) {
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
        var fieldResolution = types.resolveFieldAccess(fieldAccessExpr);
        if (fieldResolution.status() == TypeUtils.FieldAccessStatus.UNRESOLVED_RECEIVER) {
            return null;
        }

        if (fieldResolution.status() == TypeUtils.FieldAccessStatus.RESOLVED) {
            return null;
        }

        var fieldName = fieldResolution.fieldName();
        var receiverType = fieldResolution.receiverType().orElseThrow();

        switch (fieldResolution.status()) {
            case INVALID_RECEIVER -> addReport(newError(fieldAccessExpr, "Field access requires a class receiver"));
            case REQUIRES_INSTANCE -> addReport(newError(fieldAccessExpr,
                    "Field '" + fieldName + "' requires an instance receiver"));
            case MISSING_FIELD -> addReport(newError(fieldAccessExpr,
                    "Field '" + fieldName + "' is not available on receiver '" + receiverType.print() + "'"));
            default -> {
            }
        }

        return null;
    }
}
