package pt.up.fe.comp2026.analysis.attributes;

import pt.up.fe.comp.jmm.analysis.table.Signature;

public class MethodDeclAttributes {
    public static final Attribute<Signature> signature = new Attribute<>("jmm.signature", Signature.class);
    public static final Attribute<Boolean> isMainMethod = new Attribute<>("jmm.isMainMethod", Boolean.class);
}
