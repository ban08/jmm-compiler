package pt.up.fe.comp2026.optimization;

import java.util.Map;

record Coloring(Map<String, Integer> colors) {
    int usedColorCount() {
        return colors.values().stream().mapToInt(Integer::intValue).max().orElse(-1) + 1;
    }
}
