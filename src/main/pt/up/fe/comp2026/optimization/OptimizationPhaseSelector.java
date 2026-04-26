package pt.up.fe.comp2026.optimization;

public class OptimizationPhaseSelector {
    public enum Phase {
        AST_TRANSFORMATION,
        OLLIR_TRANSFORMATION,
    }

    public static final Phase REG_ALLOC = Phase.OLLIR_TRANSFORMATION;
    public static final Phase CONST_PROP = Phase.AST_TRANSFORMATION;
    public static final Phase CONST_FOLD = Phase.AST_TRANSFORMATION;
    public static final Phase BRANCH_ELIM = Phase.AST_TRANSFORMATION;
    public static final Phase DEAD_CODE_ELIM = Phase.AST_TRANSFORMATION;
}
