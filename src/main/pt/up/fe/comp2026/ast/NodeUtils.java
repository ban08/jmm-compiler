package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.NodeAttribute;
import pt.up.fe.comp.jmm.ast.NodePosition;

public class NodeUtils {

    public static int getLine(JmmNode node) {
        return getIntegerAttribute(node, NodePosition.LINE_START, -1);
    }

    public static int getColumn(JmmNode node) {
        return getIntegerAttribute(node, NodePosition.COL_START, -1);
    }

    public static int getIntegerAttribute(JmmNode node, String attribute, String defaultVal) {
        String line = node.getOptional(attribute).orElse(defaultVal);
        return Integer.parseInt(line);
    }

    public static int getIntegerAttribute(JmmNode node, NodeAttribute attribute, int defaultVal) {
        return node.getInteger(attribute, defaultVal);
    }

    public static boolean getBooleanAttribute(JmmNode node, String attribute, String defaultVal) {
        String line = node.getOptional(attribute).orElse(defaultVal);
        return Boolean.parseBoolean(line);
    }

    public static void copyPositionAttributes(JmmNode source, JmmNode target) {
        for (var position : NodePosition.values()) {
            if (source.hasAttribute(position)) {
                target.putObject(position, source.getObject(position));
            }
        }
    }
}
