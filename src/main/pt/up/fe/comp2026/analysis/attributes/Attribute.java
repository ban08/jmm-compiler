package pt.up.fe.comp2026.analysis.attributes;

import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.Optional;

public class Attribute<T> {
    private final String name;
    private final Class<T> clazz;

    public Attribute(String name, Class<T> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the value of an attribute, or adds the attribute if not present.
     *
     * @param node
     * @param value
     * @return the previous value assigned to this attribute, or null if value was not assigned before
     * @see JmmNode#putObject(String, Object)
     */
    public T set(JmmNode node, T value) {
        return clazz.cast(node.putObject(this.name, value));
    }

    /**
     * Get the value of an attribute on a given node.
     *
     * @param node
     * @return the value assigned to this attribute, or null if it was not assigned before
     */
    public T get(JmmNode node) {
        return node.getObject(this.name, this.clazz);
    }

    public Optional<T> getOptional(JmmNode node) {
        return Optional.ofNullable(this.get(node));
    }
}
