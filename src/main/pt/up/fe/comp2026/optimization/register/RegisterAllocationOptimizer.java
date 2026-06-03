package pt.up.fe.comp2026.optimization.register;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entry point for OLLIR-level register allocation.
 */
public class RegisterAllocationOptimizer {

    public List<Report> optimize(ClassUnit classUnit, int registerLimit) {
        var reports = new ArrayList<Report>();

        if (registerLimit < 0) {
            return reports;
        }

        try {
            classUnit.checkMethodLabels();
            classUnit.buildCFGs();
            classUnit.buildVarTables();
        } catch (OllirErrorException e) {
            reports.add(errorReport("Could not prepare OLLIR control-flow information for register allocation", e));
            return reports;
        }

        for (var method : classUnit.getMethods()) {
            if (method.isConstructMethod()) {
                continue;
            }

            try {
                reports.add(mappingReport(optimize(method, registerLimit)));
            } catch (RegisterAllocationFailure e) {
                reports.add(errorReport(e.getMessage(), e));
                return reports;
            }
        }

        return reports;
    }

    private MethodAllocation optimize(Method method, int registerLimit) {
        var firstLocalRegister = RegisterAllocationUtils.firstLocalRegister(method);
        if (registerLimit > 0 && registerLimit < firstLocalRegister) {
            throw new RegisterAllocationFailure(method.getMethodName(), registerLimit, firstLocalRegister, 0);
        }

        var locals = RegisterAllocationUtils.allocatableLocals(method);

        if (!locals.isEmpty()) {
            var liveness = LivenessAnalysis.analyze(method);
            var graph = InterferenceGraphBuilder.build(locals, liveness.def(), liveness.liveOut());
            var coloring = new RegisterColoring(method.getMethodName(), firstLocalRegister,
                    name -> RegisterAllocationUtils.isGeneratedBooleanTemporary(method, name))
                    .select(graph, registerLimit);

            var varTable = method.getVarTable();
            for (var entry : coloring.colors().entrySet()) {
                varTable.get(entry.getKey()).setVirtualReg(firstLocalRegister + entry.getValue());
            }
        }

        return new MethodAllocation(method.getMethodName(), RegisterAllocationUtils.variableMapping(method));
    }

    private Report mappingReport(MethodAllocation allocation) {
        var message = new StringBuilder("Register allocation for method '")
                .append(allocation.methodName())
                .append("': ");

        if (allocation.mapping().isEmpty()) {
            message.append("<no JVM locals>");
        } else {
            var first = true;
            for (var entry : allocation.mapping().entrySet()) {
                if (!first) {
                    message.append(", ");
                }

                message.append(entry.getKey()).append(" -> ").append(entry.getValue());
                first = false;
            }
        }

        return Report.newLog(Stage.LLIR_OPTIMIZATION, -1, -1, message.toString(), null);
    }

    private Report errorReport(String message, Exception exception) {
        return Report.newError(Stage.LLIR_OPTIMIZATION, -1, -1, message, exception);
    }

    private record MethodAllocation(String methodName, Map<String, Integer> mapping) {
    }
}
