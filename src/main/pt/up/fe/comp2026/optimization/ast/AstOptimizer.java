package pt.up.fe.comp2026.optimization.ast;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;

/**
 * Fixed-point AST optimizer enabled by the compiler's -o flag.
 */
public class AstOptimizer {

    private final ConstantPropagationOptimizer constantPropagation;
    private final ConstantFoldingOptimizer constantFolding;
    private final BranchEliminationOptimizer branchElimination;
    private final DeadCodeEliminationOptimizer deadCodeElimination;

    public AstOptimizer(SymbolTable table) {
        this.constantPropagation = new ConstantPropagationOptimizer(table);
        this.constantFolding = new ConstantFoldingOptimizer();
        this.branchElimination = new BranchEliminationOptimizer();
        this.deadCodeElimination = new DeadCodeEliminationOptimizer();
    }

    public boolean optimize(JmmNode root) {
        boolean changedAny = false;
        boolean changed;

        do {
            changed = false;
            changed |= constantPropagation.optimize(root);
            changed |= constantFolding.optimize(root);
            changed |= branchElimination.optimize(root);
            changed |= deadCodeElimination.optimize(root);
            changedAny |= changed;
        } while (changed);

        return changedAny;
    }
}
