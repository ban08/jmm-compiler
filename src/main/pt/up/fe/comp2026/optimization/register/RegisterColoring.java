package pt.up.fe.comp2026.optimization.register;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

final class RegisterColoring {

    private final String methodName;
    private final int firstLocalRegister;

    RegisterColoring(String methodName, int firstLocalRegister) {
        this.methodName = methodName;
        this.firstLocalRegister = firstLocalRegister;
    }

    Coloring select(Map<String, Set<String>> graph, int registerLimit) {
        if (graph.isEmpty()) {
            return new Coloring(Map.of());
        }

        if (registerLimit == 0) {
            return minimumColoring(graph);
        }

        var localRegisterLimit = registerLimit - firstLocalRegister;
        var strictColoring = colorWithDomains(graph, node -> Math.max(localRegisterLimit, 0));

        if (strictColoring.isPresent()) {
            return strictColoring.get();
        }

        var temporaryRelaxedColoring = colorWithDomains(graph, node ->
                RegisterAllocationUtils.isGeneratedTemporary(node) ? graph.size() : Math.max(localRegisterLimit, 0));

        if (temporaryRelaxedColoring.isPresent()) {
            return temporaryRelaxedColoring.get();
        }

        var minimumColoring = minimumColoring(graph);
        throw new RegisterAllocationFailure(methodName, registerLimit,
                firstLocalRegister + minimumColoring.usedColorCount(),
                minimumColoring.usedColorCount());
    }

    private Coloring minimumColoring(Map<String, Set<String>> graph) {
        for (int colors = 1; colors <= graph.size(); colors++) {
            var colorCount = colors;
            var coloring = colorWithDomains(graph, node -> colorCount);
            if (coloring.isPresent()) {
                return coloring.get();
            }
        }

        throw new IllegalStateException("Could not color interference graph");
    }

    private Optional<Coloring> colorWithDomains(
            Map<String, Set<String>> graph,
            Function<String, Integer> domainSize) {

        var colors = new TreeMap<String, Integer>();

        if (colorBacktrack(graph, domainSize, colors)) {
            return Optional.of(new Coloring(colors));
        }

        return Optional.empty();
    }

    private boolean colorBacktrack(
            Map<String, Set<String>> graph,
            Function<String, Integer> domainSize,
            Map<String, Integer> colors) {

        if (colors.size() == graph.size()) {
            return true;
        }

        var node = selectNextNode(graph, colors);
        var maxColors = domainSize.apply(node);

        for (int color = 0; color < maxColors; color++) {
            if (!canUseColor(graph, colors, node, color)) {
                continue;
            }

            colors.put(node, color);
            if (colorBacktrack(graph, domainSize, colors)) {
                return true;
            }
            colors.remove(node);
        }

        return false;
    }

    private String selectNextNode(Map<String, Set<String>> graph, Map<String, Integer> colors) {
        String bestNode = null;
        int bestSaturation = -1;
        int bestDegree = -1;

        for (var entry : graph.entrySet()) {
            var node = entry.getKey();
            if (colors.containsKey(node)) {
                continue;
            }

            var saturation = saturation(graph, colors, node);
            var degree = entry.getValue().size();

            if (saturation > bestSaturation || saturation == bestSaturation && degree > bestDegree) {
                bestNode = node;
                bestSaturation = saturation;
                bestDegree = degree;
            }
        }

        return bestNode;
    }

    private int saturation(Map<String, Set<String>> graph, Map<String, Integer> colors, String node) {
        var neighborColors = new HashSet<Integer>();

        for (var neighbor : graph.get(node)) {
            var color = colors.get(neighbor);
            if (color != null) {
                neighborColors.add(color);
            }
        }

        return neighborColors.size();
    }

    private boolean canUseColor(Map<String, Set<String>> graph, Map<String, Integer> colors, String node, int color) {
        for (var neighbor : graph.get(node)) {
            if (colors.getOrDefault(neighbor, -1) == color) {
                return false;
            }
        }

        return true;
    }
}
