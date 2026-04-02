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

    static void normalizeResolvedMethodCalls(JmmNode root, SymbolTable table) {
        var types = TypeUtils.with(table);
        normalizeImplicitThisCalls(root, table, types);
        normalizeExplicitThisStaticCalls(root, table, types);
    }

    private static void normalizeImplicitThisCalls(JmmNode root, SymbolTable table, TypeUtils types) {
        var implicitCalls = new ArrayList<>(root.getDescendants(JmmKind.IMPLICIT_THIS_CALL_EXPR));

        for (var implicitCall : implicitCalls) {
            var resolvedCall = types.resolveImplicitThisMethodCall(implicitCall);
            if (resolvedCall.isEmpty()) {
                continue;
            }

            var normalizedCall = implicitCall.copy(JmmKind.METHOD_CALL_EXPR);
            normalizedCall.add(buildReceiver(implicitCall, table.getClassName(), resolvedCall.get().method().isStatic()));

            while (implicitCall.getNumChildren() > 0) {
                normalizedCall.add(implicitCall.removeChild(0));
            }

            implicitCall.replace(normalizedCall);
        }
    }

    private static void normalizeExplicitThisStaticCalls(JmmNode root, SymbolTable table, TypeUtils types) {
        var methodCalls = new ArrayList<>(root.getDescendants(JmmKind.METHOD_CALL_EXPR));

        for (var methodCall : methodCalls) {
            var resolvedCall = types.resolveMethodCall(methodCall);
            if (resolvedCall.isEmpty() || !resolvedCall.get().method().isStatic()) {
                continue;
            }

            var receiver = methodCall.getChild(0);
            if (!JmmKind.THIS_EXPR.check(receiver)) {
                continue;
            }

            receiver.replace(buildReceiver(receiver, table.getClassName(), true));
        }
    }

    private static JmmNode buildReceiver(JmmNode sourceNode, String className, boolean staticContext) {
        var receiver = new JmmNodeImpl(staticContext ? JmmKind.VAR_REF_EXPR : JmmKind.THIS_EXPR);
        copySourceLocation(sourceNode, receiver);

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
