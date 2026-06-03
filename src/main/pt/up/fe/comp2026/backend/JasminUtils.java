package pt.up.fe.comp2026.backend;

import org.specs.comp.ollir.AccessModifier;
import org.specs.comp.ollir.Descriptor;
import org.specs.comp.ollir.inst.CallInstruction;
import org.specs.comp.ollir.type.ArrayType;
import org.specs.comp.ollir.type.BuiltinKind;
import org.specs.comp.ollir.type.BuiltinType;
import org.specs.comp.ollir.type.ClassType;
import org.specs.comp.ollir.type.Type;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.ollir.OllirResult;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JasminUtils {

    private final OllirResult ollirResult;

    private final Map<String, String> fullClassnames;

    private final Importer importer;

    public JasminUtils(OllirResult ollirResult) {
        this.ollirResult = ollirResult;
        this.importer = Importer.fromThisClassPath();
        fullClassnames = new HashMap<>();

        for (var fullImport : ollirResult.getOllirClass().getImports()) {
            var splitted = fullImport.split("\\.");
            var key = splitted[splitted.length - 1];
            fullClassnames.put(key, fullImport.replace('.', '/'));
        }
    }

    public String getTypePrefix(Type type) {
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32, BOOLEAN -> "i";
                case STRING -> "a";
                case VOID -> "";
            };
        }

        if (type instanceof ArrayType || type instanceof ClassType) {
            return "a";
        }

        throw new RuntimeException("Not implemented for element type '" + type + "'");
    }

    public String getTypeDescriptor(Type type) {
        if (type instanceof BuiltinType builtinType) {
            return switch (builtinType.getKind()) {
                case INT32 -> "I";
                case BOOLEAN -> "Z";
                case VOID -> "V";
                case STRING -> "Ljava/lang/String;";
            };
        }

        if (type instanceof ArrayType arrayType) {
            var dimensions = Math.max(1, arrayType.getNumDimensions());
            return IntStream.range(0, dimensions)
                    .mapToObj(ignored -> "[")
                    .collect(Collectors.joining())
                    + getTypeDescriptor(arrayType.getElementType());
        }

        if (type instanceof ClassType classType) {
            return "L" + getInternalName(classType) + ";";
        }

        throw new RuntimeException("Not implemented for element type '" + type + "'");
    }

    public String getInternalName(Type type) {
        if (type instanceof BuiltinType builtinType && builtinType.getKind() == BuiltinKind.STRING) {
            return "java/lang/String";
        }

        if (type instanceof ClassType classType) {
            return getInternalName(classType);
        }

        throw new RuntimeException("Type does not have an internal JVM class name: '" + type + "'");
    }

    public String getInternalName(String className) {
        var normalizedName = normalizeClassName(className);

        if (normalizedName.isEmpty() || normalizedName.equals("Object") || normalizedName.equals("java.lang.Object")
                || normalizedName.equals("java/lang/Object")) {
            return "java/lang/Object";
        }

        var importedName = fullClassnames.get(normalizedName);
        if (importedName != null) {
            return importedName;
        }

        var implicitImport = importer.loadImplicit(normalizedName);
        if (implicitImport.isPresent()) {
            return implicitImport.get().getName().replace('.', '/');
        }

        var currentClass = ollirResult.getOllirClass();
        if (normalizedName.equals(currentClass.getClassName())
                || normalizedName.equals(currentClass.getClassFullyQualifiedName())
                || normalizedName.equals(currentClass.getClassFullyQualifiedName().replace('.', '/'))) {
            return currentClass.getClassFullyQualifiedName().replace('.', '/');
        }

        return normalizedName.replace('.', '/');
    }

    public String getMethodDescriptor(CallInstruction call) {
        var params = call.getArguments().stream()
                .map(argument -> getTypeDescriptor(argument.getType()))
                .collect(Collectors.joining());

        return "(" + params + ")" + getTypeDescriptor(call.getReturnType());
    }

    public String getModifier(AccessModifier accessModifier) {
        if (accessModifier == null || accessModifier == AccessModifier.DEFAULT) {
            return "";
        }

        return accessModifier.name().toLowerCase() + " ";
    }

    public String getLoad(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();

        return prefix + "load " + value;
    }

    public String getStore(Descriptor reg) {
        var prefix = getTypePrefix(reg.getVarType());
        var value = reg.getVirtualReg();

        return prefix + "store " + value;
    }

    private String getInternalName(ClassType classType) {
        return switch (classType.getKind()) {
            case THIS -> ollirResult.getOllirClass().getClassFullyQualifiedName().replace('.', '/');
            case CLASS, OBJECTREF -> getInternalName(classType.getName());
        };
    }

    private String normalizeClassName(String className) {
        if (className == null) {
            return "";
        }

        var normalizedName = className.strip();
        if (normalizedName.startsWith("L") && normalizedName.endsWith(";")) {
            normalizedName = normalizedName.substring(1, normalizedName.length() - 1);
        }

        return normalizedName.replace("\"", "");
    }
}
