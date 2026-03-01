package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;
import java.util.stream.Collectors;

public class JmmSymbolTable extends AJmmSymbolTable {

    private final List<String> imports;
    private final String classQualifiedName;
    private final String superQualifiedName;
    private final Map<String, Symbol> fields;
    private final Map<Signature, MethodSymbol> methods;


    // TODO: Check if some uses of importNames can be replaced with getDeclaredClasses()
    private final Set<String> importNames;

    private final Map<String, Object> attrs;

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
        this.fields = fields.stream().collect(Collectors.toMap(Symbol::name, s -> s));
        this.methods = methods.stream().collect(Collectors.toMap(MethodSymbol::signature, m -> m));
//        this.returnTypes = returnTypes;
//        this.params = params;
//        this.locals = locals;
        this.importNames = calcImportNames(imports);
        this.attrs = new HashMap<>();
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
        declaredClasses.add(this.classQualifiedName);
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
        var dotName = "." + simpleName;
        return imports.stream().filter(i -> i.equals(simpleName) || i.endsWith(dotName)).findFirst();
    }

    @Override
    public String toString() {
        return print();
    }

    @Override
    public Collection<String> getAttributes() {
        return attrs.keySet();
    }

    @Override
    public Object getObject(String attribute) {
        var value = attrs.get(attribute);

        SpecsCheck.checkNotNull(value, () -> "SymbolTable does not contain attribute '" + attribute + "'");

        return value;
    }

    @Override
    public Object putObject(String attribute, Object value) {
        return attrs.put(attribute, value);
    }

    public Optional<SymbolTable> getImportedSymbolTable(String className) {

        if (!this.imports.contains(className)) {
            //then it is already fully qualified name and it is imported
            return this.importer.getSymbolTableOf(className);
        }
        //else, try to get fully qualified name
        var fullyQualifiedName = this.getImportedFullyQualifiedName(className);
        if (fullyQualifiedName.isPresent()) {
            return this.importer.getSymbolTableOf(fullyQualifiedName.get());
        }
        //else, see if implicit import works
        return this.importer.tryImplicitImport(className);
    }

    public boolean isImplicitImport(String className) {
        return importer.isImplicitImport(className);
    }

    public Optional<SymbolTable> getImplicitImport(String className) {
        return this.importer.tryImplicitImport(className);
    }

}
