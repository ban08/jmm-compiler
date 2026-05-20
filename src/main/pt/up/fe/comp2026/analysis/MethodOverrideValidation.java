package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class MethodOverrideValidation extends SemanticValidationPass {

    public MethodOverrideValidation(SymbolTable table) {
        super(table);
    }

    @Override
    protected void buildVisitor() {
        addVisit(JmmKind.METHOD_DECL, this::visitMethodDecl);
        setDefaultVisit((node, st) -> null);
    }

    private Void visitMethodDecl(JmmNode methodDecl, SymbolTable ignored) {
        var currentMethod = types.getEnclosingMethod(methodDecl);
        if (currentMethod.isEmpty()) {
            return null;
        }

        var inheritedMethod = findInheritedOverride(currentMethod.get());
        if (inheritedMethod.isEmpty()) {
            return null;
        }

        var localReturn = currentMethod.get().returnType();
        var inheritedReturn = inheritedMethod.get().returnType();
        if (!sameType(localReturn, inheritedReturn)) {
            addReport(newError(methodDecl,
                    "Method '" + methodDecl.get(JmmAttributes.METHOD_DECL.NAME) +
                            "' overrides an inherited method with return type '" +
                            inheritedReturn.print() + "' but declares return type '" +
                            localReturn.print() + "'"));
        }

        return null;
    }

    private Optional<MethodSymbol> findInheritedOverride(MethodSymbol currentMethod) {
        var visitedSupers = new HashSet<String>();
        var superQualifiedName = table.getSuperFullyQualifiedName();

        while (superQualifiedName != null && visitedSupers.add(superQualifiedName)) {
            var superTable = table.getImportedSymbolTable(superQualifiedName);
            if (superTable.isEmpty()) {
                return Optional.empty();
            }

            var matchingMethod = superTable.get().getMethods(currentMethod.name()).stream()
                    .filter(superMethod -> sameParameters(currentMethod.parameters(), superMethod.parameters()))
                    .findFirst();

            if (matchingMethod.isPresent()) {
                return matchingMethod;
            }

            superQualifiedName = superTable.get().getSuperFullyQualifiedName();
        }

        return Optional.empty();
    }

    private boolean sameParameters(List<Symbol> currentParameters, List<Symbol> inheritedParameters) {
        if (currentParameters.size() != inheritedParameters.size()) {
            return false;
        }

        for (int i = 0; i < currentParameters.size(); i++) {
            if (!sameType(currentParameters.get(i).type(), inheritedParameters.get(i).type())) {
                return false;
            }
        }

        return true;
    }

    private boolean sameType(JmmType left, JmmType right) {
        if (left.isArray() || right.isArray()) {
            if (!left.isArray() || !right.isArray()) {
                return false;
            }

            var leftArray = left.asArray();
            var rightArray = right.asArray();
            return leftArray.dimension() == rightArray.dimension()
                    && sameType(leftArray.itemType(), rightArray.itemType());
        }

        if (left.isClass() || right.isClass()) {
            return left.isClass()
                    && right.isClass()
                    && left.asClass().fullyQualifiedName().equals(right.asClass().fullyQualifiedName());
        }

        return left.equals(right);
    }
}
