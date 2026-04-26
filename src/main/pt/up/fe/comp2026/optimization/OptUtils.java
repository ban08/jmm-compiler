package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.specs.util.collections.AccumulatorMap;

import java.util.Set;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.TYPE;

/**
 * Utility methods related to the optimization middle-end.
 */
public class OptUtils {

    private static final Set<String> OLLIR_KEYWORDS = Set.of("bool", "i32", "V", "array", "String", "final", "goto", "if", "import", "interface", "new", "package"
            , "private", "protected", "public", "ret", "static", "this", "ldc", "invokespecial", "invokevirtual", "invokestatic", "arraylength", "getfield", "putfield", "getstatic", "putstatic",
            ".method", ".construct", ".field", "extends"
    );

    private final TypeUtils types;
    private AccumulatorMap<String> temporaries;
    private int labelCounter;

    public OptUtils(TypeUtils types) {
        this.types = types;
        this.temporaries = new AccumulatorMap<>();
        this.labelCounter = 0;
    }

    /**
     * Resets temporary counters. Labels stay globally unique across the whole class
     * so nested loops/ifs from different methods can never collide if the OLLIR is
     * later concatenated or inspected linearly.
     */
    public void resetTemporaries() {
        this.temporaries = new AccumulatorMap<>();
    }

    public String nextTemp() {
        return nextTemp("tmp");
    }

    public String nextTemp(String prefix) {
        // Subtract 1 because the AccumulatorMap base is 1
        var nextTempNum = temporaries.add(prefix) - 1;
        return prefix + nextTempNum;
    }

    public String nextLabel(String prefix) {
        return prefix + (labelCounter++);
    }

    public String toOllirType(JmmNode typeNode) {
        TYPE.checkOrThrow(typeNode);
        return toOllirType(types.convertType(typeNode));
    }

    public String toOllirType(JmmType type) {
        return "." + buildOllirTypeBody(type);
    }

    private static String buildOllirTypeBody(JmmType type) {
        if (type.isArray()) {
            JmmArrayType array = type.asArray();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < array.dimension(); i++) {
                sb.append("array.");
            }
            sb.append(buildOllirTypeBody(array.itemType()));
            return sb.toString();
        }

        if (type.isPrimitive()) {
            JmmPrimitiveType prim = type.asPrimitive();
            return switch (prim) {
                case INT -> "i32";
                case BOOLEAN -> "bool";
                case VOID -> "V";
                default -> throw new RuntimeException("Unsupported OLLIR primitive type: " + prim);
            };
        }

        if (type.isClass()) {
            return simpleClassName(type.asClass().fullyQualifiedName());
        }

        throw new RuntimeException("Unknown JmmType: " + type);
    }

    public static String simpleClassName(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return "Object";
        }
        int dot = fullyQualifiedName.lastIndexOf('.');
        return dot >= 0 ? fullyQualifiedName.substring(dot + 1) : fullyQualifiedName;
    }

    public String sanitizeId(String id) {
        if (OLLIR_KEYWORDS.contains(id)) {
            return '"' + id + '"';
        }

        if (id.startsWith("$")) {
            return '"' + id + '"';
        }

        return id;
    }
}
