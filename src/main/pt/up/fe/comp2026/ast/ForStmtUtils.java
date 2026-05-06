package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

/**
 * Keeps the three optional for-header slots distinct after parsing.
 */
public final class ForStmtUtils {

    private ForStmtUtils() {
    }

    public static Parts split(JmmNode forStmt) {
        JmmKind.FOR_STMT.checkOrThrow(forStmt);

        JmmNode init = null;
        JmmNode condition = null;
        JmmNode update = null;
        var body = forStmt.getChildren(JmmKind.STMT).stream().findFirst().orElse(null);

        for (var child : forStmt.getChildren()) {
            if (JmmKind.FOR_INIT.check(child)) {
                init = child.getChild(0);
            } else if (JmmKind.FOR_CONDITION.check(child)) {
                condition = child.getChild(0);
            } else if (JmmKind.FOR_UPDATE.check(child)) {
                update = child.getChild(0);
            }
        }

        if (init != null || condition != null || update != null) {
            return new Parts(init, condition, update, body);
        }

        return splitLegacyChildren(forStmt);
    }

    private static Parts splitLegacyChildren(JmmNode forStmt) {
        JmmNode init = null;
        JmmNode condition = null;
        JmmNode update = null;
        JmmNode body = null;

        boolean conditionSeen = false;
        for (var child : forStmt.getChildren()) {
            if (JmmKind.FOR_HEADER_ASSIGN.check(child)) {
                if (conditionSeen || init != null) {
                    update = child;
                } else {
                    init = child;
                }
            } else if (JmmKind.EXPR.check(child)) {
                condition = child;
                conditionSeen = true;
            } else if (JmmKind.STMT.check(child)) {
                body = child;
            }
        }

        return new Parts(init, condition, update, body);
    }

    public record Parts(JmmNode init, JmmNode condition, JmmNode update, JmmNode body) {
    }
}
