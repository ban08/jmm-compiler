package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;

import java.util.*;
import java.util.stream.Collectors;

public class JmmSymbolTable extends AJmmSymbolTable {

    private final List<String> imports;
    private final String classQualifiedName;
    private final String superQualifiedName;
    private final Map<String, Symbol> fields;
    private final Map<Signature, MethodSymbol> methods;


    private final Set<String> importNames;

    public JmmSymbolTable(List<String> imports, String classQualifiedName,
                          String superQualifiedNameName, List<Symbol> fields,
                          List<MethodSymbol> methods,
                          Importer importer) {
//                          Map<String, Type> returnTypes,
//                          Map<String, List<Symbol>> params,
//                          Map<String, List<Symbol>> locals) {
        super(importer);
        this.imports = imports;
        this.classQualifiedName = classQualifiedName;
        this.superQualifiedName = superQualifiedNameName;
        this.fields = new LinkedHashMap<>();
        for (var field : fields) {
            this.fields.putIfAbsent(field.name(), field);
        }

        this.methods = new LinkedHashMap<>();
        for (var method : methods) {
            this.methods.putIfAbsent(method.signature(), method);
        }
//        this.returnTypes = returnTypes;
//        this.params = params;
//        this.locals = locals;
        this.importNames = calcImportNames(imports);
    }

    private Set<String> calcImportNames(List<String> imports) {
        return imports.stream()
                .map(s -> s.split("\\."))
                .map(sarray -> sarray[sarray.length - 1])
                .collect(Collectors.toSet());
    }

    @Override
    public String getFullyQualifiedName() {
        return this.classQualifiedName;
    }

    @Override
    public String getSuperFullyQualifiedName() {
        return this.superQualifiedName;
    }

    /**
     * Represents available class names. Corresponds to the names of the imports + current class
     *
     * @return
     */
    public Set<String> getDeclaredClasses() {
        var declaredClasses = new HashSet<>(importNames);
        declaredClasses.add(getSimpleClassName());
        return declaredClasses;
    }


    /**
     * @return the set of valid import class names (only the last name, not the fully qualified names)
     */
    public Set<String> getImportNames() {
        return importNames;
    }

    @Override
    public List<String> getImports() {
        return imports;
    }


    @Override
    public List<Symbol> getFields() {
        return fields.values().stream().toList();
    }

    @Override
    public List<MethodSymbol> getMethods() {
        return methods.values().stream().toList();
    }

    @Override
    public List<MethodSymbol> getMethods(String name) {
        return methods.values().stream().filter(m -> m.name().equals(name)).toList();
    }

    @Override
    public Optional<MethodSymbol> getMethod(Signature signature) {
        return Optional.ofNullable(methods.getOrDefault(signature, null));
    }

    @Override
    public Optional<Symbol> getField(String name) {
        return Optional.ofNullable(fields.getOrDefault(name, null));
    }

    @Override
    public Optional<String> getImportedFullyQualifiedName(String simpleName) {
        return ClassResolution.findImportedFullyQualifiedName(simpleName, imports);
    }

    @Override
    public String toString() {
        return print();
    }

    public Optional<SymbolTable> getImportedSymbolTable(String className) {
        var importedName = this.getImportedFullyQualifiedName(className);
        if (importedName.isPresent()) {
            return this.importer.getSymbolTableOf(importedName.get());
        }

        var directLookup = this.importer.getSymbolTableOf(className);
        if (directLookup.isPresent()) {
            return directLookup;
        }

        return this.importer.tryImplicitImport(className);
    }

    public boolean isImplicitImport(String className) {
        return importer.isImplicitImport(className);
    }

    public Optional<ClassResolution.ResolvedClass> resolveClass(String className) {
        return ClassResolution.resolve(className, imports, getSimpleClassName(), getFullyQualifiedName(), importer);
    }

    public Optional<SymbolTable> getImplicitImport(String className) {
        return this.importer.tryImplicitImport(className);
    }

    private String getSimpleClassName() {
        var separatorIndex = classQualifiedName.lastIndexOf('.');
        return separatorIndex >= 0
                ? classQualifiedName.substring(separatorIndex + 1)
                : classQualifiedName;
    }

}
