package pt.up.fe.comp2026.optimization.register;

import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.VarScope;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;

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

    static boolean isGeneratedBooleanConditionTemporary(Method method, String name) {
        var descriptor = method.getVarTable().get(name);
        return descriptor != null
                && descriptor.getScope() == VarScope.LOCAL
                && isGeneratedTemporaryName(name)
                && BuiltinType.is(descriptor.getVarType(), BuiltinKind.BOOLEAN)
                && isUsedDirectlyByConditionalBranch(method, name);
    }

    private static boolean isUsedDirectlyByConditionalBranch(Method method, String name) {
        return method.getInstructions().stream()
                .filter(CondBranchInstruction.class::isInstance)
                .map(CondBranchInstruction.class::cast)
                .flatMap(branch -> branch.getOperands().stream())
                .anyMatch(operand -> isNamedOperand(operand, name));
    }

    private static boolean isNamedOperand(Element element, String name) {
        return element instanceof Operand operand && operand.getName().equals(name);
    }

    private static boolean isGeneratedTemporaryName(String name) {
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
