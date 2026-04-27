package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.ArrayOperand;
import org.specs.comp.ollir.Element;
import org.specs.comp.ollir.Method;
import org.specs.comp.ollir.Operand;
import org.specs.comp.ollir.inst.AssignInstruction;
import org.specs.comp.ollir.inst.BinaryOpInstruction;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.inst.CondBranchInstruction;
import org.specs.comp.ollir.inst.GetFieldInstruction;
import org.specs.comp.ollir.inst.Instruction;
import org.specs.comp.ollir.inst.PutFieldInstruction;
import org.specs.comp.ollir.inst.ReturnInstruction;
import org.specs.comp.ollir.inst.SingleOpInstruction;
import org.specs.comp.ollir.inst.UnaryOpInstruction;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

final class LivenessAnalysis {

    private LivenessAnalysis() {
    }

    static Result analyze(Method method) {
        var instructions = method.getInstructions();
        var use = new HashMap<Instruction, Set<String>>();
        var def = new HashMap<Instruction, Set<String>>();

        for (var instruction : instructions) {
            var info = instructionInfo(method, instruction);
            use.put(instruction, info.uses());
            def.put(instruction, info.defs());
        }

        return new Result(def, liveOut(instructions, use, def));
    }

    private static Map<Instruction, Set<String>> liveOut(
            List<Instruction> instructions,
            Map<Instruction, Set<String>> use,
            Map<Instruction, Set<String>> def) {

        var liveIn = new HashMap<Instruction, Set<String>>();
        var liveOut = new HashMap<Instruction, Set<String>>();

        for (var instruction : instructions) {
            liveIn.put(instruction, new TreeSet<>());
            liveOut.put(instruction, new TreeSet<>());
        }

        boolean changed;
        do {
            changed = false;

            for (int i = instructions.size() - 1; i >= 0; i--) {
                var instruction = instructions.get(i);

                var nextOut = new TreeSet<String>();
                for (var successor : instruction.getSuccessorsAsInst()) {
                    nextOut.addAll(liveIn.getOrDefault(successor, Set.of()));
                }

                var nextIn = new TreeSet<>(nextOut);
                nextIn.removeAll(def.getOrDefault(instruction, Set.of()));
                nextIn.addAll(use.getOrDefault(instruction, Set.of()));

                changed |= replaceIfChanged(liveOut, instruction, nextOut);
                changed |= replaceIfChanged(liveIn, instruction, nextIn);
            }
        } while (changed);

        return liveOut;
    }

    private static InstructionInfo instructionInfo(Method method, Instruction instruction) {
        var uses = new LinkedHashSet<String>();
        var defs = new LinkedHashSet<String>();

        if (instruction instanceof AssignInstruction assign) {
            addAssignmentDestination(method, assign.getDest(), uses, defs);
            addInstructionUses(method, assign.getRhs(), uses);
            return new InstructionInfo(uses, defs);
        }

        addInstructionUses(method, instruction, uses);
        return new InstructionInfo(uses, defs);
    }

    private static void addInstructionUses(Method method, Instruction instruction, Set<String> uses) {
        if (instruction instanceof SingleOpInstruction singleOp) {
            addElementUse(method, singleOp.getSingleOperand(), uses);
            return;
        }

        if (instruction instanceof BinaryOpInstruction binaryOp) {
            addElementUse(method, binaryOp.getLeftOperand(), uses);
            addElementUse(method, binaryOp.getRightOperand(), uses);
            return;
        }

        if (instruction instanceof UnaryOpInstruction unaryOp) {
            addElementUse(method, unaryOp.getOperand(), uses);
            return;
        }

        if (instruction instanceof ReturnInstruction returnInstruction) {
            returnInstruction.getOperand().ifPresent(operand -> addElementUse(method, operand, uses));
            return;
        }

        if (instruction instanceof CondBranchInstruction branch) {
            for (var operand : branch.getOperands()) {
                addElementUse(method, operand, uses);
            }
            return;
        }

        if (instruction instanceof PutFieldInstruction putField) {
            addElementUse(method, putField.getObject(), uses);
            addElementUse(method, putField.getValue(), uses);
            return;
        }

        if (instruction instanceof GetFieldInstruction getField) {
            addElementUse(method, getField.getObject(), uses);
            return;
        }

        if (instruction instanceof CallInstruction call) {
            addElementUse(method, call.getCaller(), uses);
            for (var argument : call.getArguments()) {
                addElementUse(method, argument, uses);
            }
        }
    }

    private static void addAssignmentDestination(Method method, Element destination, Set<String> uses,
            Set<String> defs) {

        if (destination instanceof ArrayOperand arrayOperand) {
            addElementUse(method, arrayOperand, uses);
            return;
        }

        if (destination instanceof Operand operand && RegisterAllocationUtils.isAllocatableLocal(method,
                operand.getName())) {

            defs.add(operand.getName());
        }
    }

    private static void addElementUse(Method method, Element element, Set<String> uses) {
        if (element == null || element.isLiteral()) {
            return;
        }

        if (element instanceof ArrayOperand arrayOperand) {
            if (RegisterAllocationUtils.isAllocatableLocal(method, arrayOperand.getName())) {
                uses.add(arrayOperand.getName());
            }

            for (var index : arrayOperand.getIndexOperands()) {
                addElementUse(method, index, uses);
            }

            return;
        }

        if (element instanceof Operand operand && RegisterAllocationUtils.isAllocatableLocal(method,
                operand.getName())) {

            uses.add(operand.getName());
        }
    }

    private static boolean replaceIfChanged(
            Map<Instruction, Set<String>> target,
            Instruction instruction,
            Set<String> nextValue) {

        if (target.get(instruction).equals(nextValue)) {
            return false;
        }

        target.put(instruction, nextValue);
        return true;
    }

    record Result(Map<Instruction, Set<String>> def, Map<Instruction, Set<String>> liveOut) {
    }

    private record InstructionInfo(Set<String> uses, Set<String> defs) {
    }
}
