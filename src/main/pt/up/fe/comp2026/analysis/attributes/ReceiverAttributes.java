package pt.up.fe.comp2026.analysis.attributes;

public final class ReceiverAttributes {
    public static final Attribute<String> normalizedClassFqn =
            new Attribute<>("jmm.normalizedClassFqn", String.class);
    public static final Attribute<Boolean> normalizedClassImported =
            new Attribute<>("jmm.normalizedClassImported", Boolean.class);

    private ReceiverAttributes() {
    }
}
