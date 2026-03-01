package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;

/**
 *
 */
public abstract class AnalysisVisitorWithTable extends AnalysisVisitor {

    protected final JmmSymbolTable table;
    protected final TypeUtils types;

    public AnalysisVisitorWithTable(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
        this.types = new TypeUtils(table);
    }

    public SymbolTable getTable() {
        return table;
    }

    public TypeUtils getTypes() {
        return types;
    }

    @Override
    public Void visit(JmmNode jmmNode) {
        return visit(jmmNode, table);
    }
}
