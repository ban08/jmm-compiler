package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;

import java.util.List;
import java.util.Optional;

/**
 * Shared rules for resolving class references to fully qualified names.
 */
public final class ClassResolution {

    public record ResolvedClass(String fullyQualifiedName, boolean imported) {
        public JmmClassType asType(boolean staticReference) {
            return staticReference
                    ? JmmClassType.ofStaticReference(fullyQualifiedName, imported)
                    : JmmClassType.ofInstance(fullyQualifiedName, imported);
        }
    }

    private ClassResolution() {
    }

    public static Optional<ResolvedClass> resolve(String className,
                                                  List<String> imports,
                                                  String currentClassName,
                                                  String currentClassFullyQualifiedName,
                                                  Importer importer) {
        var importedFqn = findImportedFullyQualifiedName(className, imports);
        if (importedFqn.isPresent()) {
            return Optional.of(new ResolvedClass(importedFqn.get(), true));
        }

        if (className.equals(currentClassName)) {
            return Optional.of(new ResolvedClass(currentClassFullyQualifiedName, false));
        }

        return importer.tryImplicitImport(className)
                .map(symbolTable -> new ResolvedClass(symbolTable.getFullyQualifiedName(), true));
    }

    public static Optional<String> findImportedFullyQualifiedName(String className, List<String> imports) {
        var dotName = "." + className;
        return imports.stream()
                .filter(importName -> importName.equals(className) || importName.endsWith(dotName))
                .findFirst();
    }
}
