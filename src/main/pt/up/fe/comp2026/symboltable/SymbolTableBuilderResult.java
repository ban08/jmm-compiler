package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public record SymbolTableBuilderResult(SymbolTable table, List<Report> reports) {
}
