package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AJmmSymbolTable implements SymbolTable {

    private final Map<String, Object> attrs;

    protected Importer importer;

    public AJmmSymbolTable(Importer importer) {

        this.attrs = new HashMap<>();
        this.importer = importer;
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
}
