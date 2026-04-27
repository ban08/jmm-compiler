package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.VarScope;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class RegisterAllocationUtils {

    private static final String THIS = "this";
    private static final String TEMP_PREFIX = "tmp";

    private RegisterAllocationUtils() {
    }

    static Set<String> allocatableLocals(Method method) {
        var locals = new TreeSet<String>();

        for (var entry : method.getVarTable().entrySet()) {
            if (entry.getValue().getScope() == VarScope.LOCAL && !THIS.equals(entry.getKey())) {
                locals.add(entry.getKey());
            }
        }

        return locals;
    }

    static int firstLocalRegister(Method method) {
        return (method.isStaticMethod() ? 0 : 1) + method.getParams().size();
    }

    static boolean isAllocatableLocal(Method method, String name) {
        var descriptor = method.getVarTable().get(name);
        return descriptor != null && descriptor.getScope() == VarScope.LOCAL && !THIS.equals(name);
    }

    static boolean isGeneratedTemporary(String name) {
        if (!name.startsWith(TEMP_PREFIX) || name.length() == TEMP_PREFIX.length()) {
            return false;
        }

        for (int i = TEMP_PREFIX.length(); i < name.length(); i++) {
            if (!Character.isDigit(name.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    static Map<String, Integer> variableMapping(Method method) {
        var mapping = new TreeMap<String, Integer>();

        for (var entry : method.getVarTable().entrySet()) {
            if (entry.getValue().getScope() != VarScope.FIELD) {
                mapping.put(entry.getKey(), entry.getValue().getVirtualReg());
            }
        }

        return mapping;
    }
}
