package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.NodePosition;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;

final class EntityAccessNormalizer {

    private EntityAccessNormalizer() {
    }

    static void normalizeResolvedImplicitThisCalls(JmmNode root, SymbolTable table) {
        var types = TypeUtils.with(table);
        var implicitCalls = new ArrayList<>(root.getDescendants(JmmKind.IMPLICIT_THIS_CALL_EXPR));

        for (var implicitCall : implicitCalls) {
            if (types.resolveImplicitThisMethodCall(implicitCall).isEmpty()) {
                continue;
            }

            var normalizedCall = implicitCall.copy(JmmKind.METHOD_CALL_EXPR);
            normalizedCall.add(buildReceiver(implicitCall, table.getClassName(), types.isStaticMethodContext(implicitCall)));

            while (implicitCall.getNumChildren() > 0) {
                normalizedCall.add(implicitCall.removeChild(0));
            }

            implicitCall.replace(normalizedCall);
        }
    }

    private static JmmNode buildReceiver(JmmNode implicitCall, String className, boolean staticContext) {
        var receiver = new JmmNodeImpl(staticContext ? JmmKind.VAR_REF_EXPR : JmmKind.THIS_EXPR);
        copySourceLocation(implicitCall, receiver);

        if (staticContext) {
            receiver.put(JmmAttributes.VAR_REF_EXPR.NAME, className);
        }

        return receiver;
    }

    private static void copySourceLocation(JmmNode source, JmmNode target) {
        for (var position : NodePosition.values()) {
            if (source.hasAttribute(position)) {
                target.putObject(position, source.getObject(position));
            }
        }
    }
}
