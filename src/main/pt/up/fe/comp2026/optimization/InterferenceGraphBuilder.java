package pt.up.fe.comp2026.optimization;

import org.specs.comp.ollir.inst.Instruction;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

final class InterferenceGraphBuilder {

    private InterferenceGraphBuilder() {
    }

    static Map<String, Set<String>> build(
            Set<String> locals,
            Map<Instruction, Set<String>> def,
            Map<Instruction, Set<String>> liveOut) {

        var graph = new TreeMap<String, Set<String>>();
        for (var local : locals) {
            graph.put(local, new TreeSet<>());
        }

        for (var entry : def.entrySet()) {
            var live = new TreeSet<>(entry.getValue());
            live.addAll(liveOut.getOrDefault(entry.getKey(), Set.of()));
            live.retainAll(locals);
            addClique(graph, live);
        }

        return graph;
    }

    private static void addClique(Map<String, Set<String>> graph, Set<String> nodes) {
        var nodeList = new ArrayList<>(nodes);

        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = i + 1; j < nodeList.size(); j++) {
                addEdge(graph, nodeList.get(i), nodeList.get(j));
            }
        }
    }

    private static void addEdge(Map<String, Set<String>> graph, String first, String second) {
        if (first.equals(second) || !graph.containsKey(first) || !graph.containsKey(second)) {
            return;
        }

        graph.get(first).add(second);
        graph.get(second).add(first);
    }
}
