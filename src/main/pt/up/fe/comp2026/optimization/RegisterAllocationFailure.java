package pt.up.fe.comp2026.optimization;

final class RegisterAllocationFailure extends RuntimeException {

    RegisterAllocationFailure(String methodName, int requestedRegisters, int requiredJvmRegisters,
            int requiredLocalRegisters) {

        super("Could not allocate registers for method '" + methodName + "' with -r=" + requestedRegisters
                + ". Minimum required: " + requiredJvmRegisters + " JVM local register(s), including "
                + requiredLocalRegisters + " allocatable local/temporary register(s).");
    }
}
