package pt.up.fe.comp2026.analysis;

import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.NodePosition;
import pt.up.fe.comp2026.analysis.attributes.ReceiverAttributes;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.comp2026.jmm.ast.JmmKind;

import java.util.ArrayList;

final class EntityAccessNormalizer {

    private EntityAccessNormalizer() {
    }

    static void normalizeResolvedMethodCalls(JmmNode root, SymbolTable table) {
        var types = TypeUtils.with(table);
        normalizeImplicitThisCalls(root, types);
        normalizeStaticCallReceivers(root, types);
    }

    private static void normalizeImplicitThisCalls(JmmNode root, TypeUtils types) {
        var implicitCalls = new ArrayList<>(root.getDescendants(JmmKind.IMPLICIT_THIS_CALL_EXPR));

        for (var implicitCall : implicitCalls) {
            var resolvedCall = types.resolveImplicitThisMethodCall(implicitCall);
            if (resolvedCall.isEmpty()) {
                continue;
            }

            var normalizedCall = implicitCall.copy(JmmKind.METHOD_CALL_EXPR);
            normalizedCall.add(buildReceiver(
                    implicitCall,
                    resolvedCall.get().receiverType().asClass(),
                    resolvedCall.get().method().isStatic()
            ));

            while (implicitCall.getNumChildren() > 0) {
                normalizedCall.add(implicitCall.removeChild(0));
            }

            implicitCall.replace(normalizedCall);
        }
    }

    private static void normalizeStaticCallReceivers(JmmNode root, TypeUtils types) {
        var methodCalls = new ArrayList<>(root.getDescendants(JmmKind.METHOD_CALL_EXPR));

        for (var methodCall : methodCalls) {
            var resolvedCall = types.resolveMethodCall(methodCall);
            if (resolvedCall.isEmpty() || !resolvedCall.get().method().isStatic()) {
                continue;
            }

            var receiver = methodCall.getChild(0);
            receiver.replace(buildReceiver(receiver, resolvedCall.get().receiverType().asClass(), true));
        }
    }

    private static JmmNode buildReceiver(JmmNode sourceNode, JmmClassType receiverType, boolean staticContext) {
        var receiver = new JmmNodeImpl(staticContext ? JmmKind.VAR_REF_EXPR : JmmKind.THIS_EXPR);
        copySourceLocation(sourceNode, receiver);

        if (staticContext) {
            receiver.put(JmmAttributes.VAR_REF_EXPR.NAME, getSimpleClassName(receiverType.fullyQualifiedName()));
            ReceiverAttributes.normalizedClassFqn.set(receiver, receiverType.fullyQualifiedName());
            ReceiverAttributes.normalizedClassImported.set(receiver, receiverType.isImported());
        }

        return receiver;
    }

    private static String getSimpleClassName(String fullyQualifiedName) {
        var separatorIndex = fullyQualifiedName.lastIndexOf('.');
        return separatorIndex >= 0
                ? fullyQualifiedName.substring(separatorIndex + 1)
                : fullyQualifiedName;
    }

    private static void copySourceLocation(JmmNode source, JmmNode target) {
        for (var position : NodePosition.values()) {
            if (source.hasAttribute(position)) {
                target.putObject(position, source.getObject(position));
            }
        }
    }
}
