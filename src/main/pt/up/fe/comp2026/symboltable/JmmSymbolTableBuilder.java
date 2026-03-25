package pt.up.fe.comp2026.symboltable;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Visibility;
import pt.up.fe.comp.jmm.analysis.table.reflection.Importer;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp.jmm.utils.Attributes;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.specs.util.SpecsCheck;

import java.util.*;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

public class JmmSymbolTableBuilder {

    private final JmmNode root;
    private final Importer importer;
    public String className;
    private String fullyQualifiedName;
    private final List<Report> reports;
    private final List<String> imports;
    private final Map<String, String> declaredClasses;

    private JmmSymbolTableBuilder(JmmNode root) {
        this.root = root;
        reports = new ArrayList<>();
        imports = new ArrayList<>();
        declaredClasses = new HashMap<>();
        this.importer = Importer.fromThisClassPath();
    }

    private static Report newError(JmmNode node, String message) {
        return Report.newError(
                Stage.SEMANTIC,
                NodeUtils.getLine(node),
                NodeUtils.getColumn(node),
                message,
                null);
    }

    public static SymbolTableBuilderResult build(JmmNode root) {
        return new JmmSymbolTableBuilder(root).buildInternal();
    }

    private SymbolTableBuilderResult buildInternal() {

        var packageDecl = root.getChildren(PACKAGE_DECL).getFirst();
        var packagePathList = packageDecl.getObjectAsList("path", String.class);
        var packagePath = String.join(".", packagePathList);

        // Process imports (with dedup) and validate that each imported class exists.
        var importSet = new LinkedHashSet<String>();
        var importNamesToFqn = new HashMap<String, String>();
        for (var importDecl : root.getChildren(IMPORT_DECL)) {
            var importPath = importDecl.getObjectAsList("path", String.class);
            var importFqn = String.join(".", importPath);

            if (importSet.contains(importFqn)) {
                continue;
            }

            var importName = importPath.getLast();
            var previousImport = importNamesToFqn.putIfAbsent(importName, importFqn);
            if (previousImport != null && !previousImport.equals(importFqn)) {
                reports.add(newError(importDecl,
                        "Imported class '" + importFqn + "' conflicts with previously imported class '" +
                                previousImport + "'"));
                continue;
            }

            importSet.add(importFqn);
            validateImport(importDecl, importFqn);
        }
        imports.addAll(importSet);

        var classDecl = root.getObject("classNode", JmmNode.class);
        SpecsCheck.checkArgument(CLASS_DECL.check(classDecl), () -> "Expected a class declaration: " + classDecl);

        this.className = classDecl.get("name");
        this.fullyQualifiedName = packagePath + "." + className;

        // Check if className is available
        if (declaredClasses.containsKey(className)) {
            reports.add(newError(root, "'" + className + "' is already defined in this compilation unit"));
        }
        declaredClasses.put(className, fullyQualifiedName);

        // Resolve super class
        String superQualifiedName = null;
        var superNameOpt = classDecl.getOptional("superName");
        if (superNameOpt.isPresent()) {
            var superName = superNameOpt.get();

            // Check class doesn't extend itself
            if (superName.equals(className)) {
                reports.add(newError(classDecl, "Class '" + className + "' cannot extend itself"));
            } else {
                superQualifiedName = resolveClassName(superName, classDecl);
            }
        }

        // Build fields
        var fields = buildFields(classDecl);

        // Build methods
        var methods = buildMethods(classDecl);

        var symbolTable = new JmmSymbolTable(imports, fullyQualifiedName, superQualifiedName, fields, methods, importer);

        return new SymbolTableBuilderResult(symbolTable, reports);
    }

    /**
     * Resolve a class name to its fully qualified name.
     * Checks: explicit imports, implicit imports (java.lang), declared class.
     */
    private String resolveClassName(String simpleName, JmmNode contextNode) {
        // Check explicit imports
        var dotName = "." + simpleName;
        for (var imp : imports) {
            if (imp.equals(simpleName) || imp.endsWith(dotName)) {
                return imp;
            }
        }

        // Check if it's the declared class
        if (simpleName.equals(className)) {
            return fullyQualifiedName;
        }

        // Check implicit imports (java.lang.*)
        if (importer.isImplicitImport(simpleName)) {
            var clazz = importer.loadImplicit(simpleName);
            if (clazz.isPresent()) {
                return clazz.get().getName();
            }
        }

        // Not found - report error
        reports.add(newError(contextNode, "Class '" + simpleName + "' is not imported"));
        return simpleName;
    }

    private void validateImport(JmmNode importDecl, String importFqn) {
        if (importer.getSymbolTableOf(importFqn).isEmpty()) {
            reports.add(newError(importDecl, "Imported class '" + importFqn + "' does not exist"));
        }
    }

    /**
     * Convert a type AST node into a JmmType.
     */
    private JmmType convertType(JmmNode typeNode) {
        var typeName = typeNode.get("name");
        boolean isArray = NodeUtils.getBooleanAttribute(typeNode, "isArray", "false");
        int arrayDepth = NodeUtils.getIntegerAttribute(typeNode, "arrayDepth", isArray ? "1" : "0");

        // Check primitives
        var primitive = JmmPrimitiveType.fromString(typeName);
        if (primitive.isPresent()) {
            return wrapArrayType(primitive.get(), arrayDepth);
        }

        // It's a class type - resolve it
        String resolvedFqn;

        // Check explicit imports
        var dotName = "." + typeName;
        String importedFqn = null;
        for (var imp : imports) {
            if (imp.equals(typeName) || imp.endsWith(dotName)) {
                importedFqn = imp;
                break;
            }
        }

        if (importedFqn != null) {
            // Explicitly imported class
            var classType = JmmClassType.ofInstance(importedFqn, true);
            return wrapArrayType(classType, arrayDepth);
        }

        // Check if it's the declared class itself
        if (typeName.equals(className)) {
            var classType = JmmClassType.ofInstance(fullyQualifiedName, false);
            return wrapArrayType(classType, arrayDepth);
        }

        // Check implicit imports (java.lang.*)
        if (importer.isImplicitImport(typeName)) {
            var clazz = importer.loadImplicit(typeName);
            if (clazz.isPresent()) {
                var classType = JmmClassType.ofInstance(clazz.get().getName(), true);
                return wrapArrayType(classType, arrayDepth);
            }
        }

        // Unknown class - treat as non-imported class type
        var classType = JmmClassType.ofInstance(typeName, false);
        return wrapArrayType(classType, arrayDepth);
    }

    private JmmType wrapArrayType(JmmType baseType, int arrayDepth) {
        var currentType = baseType;
        for (int i = 0; i < arrayDepth; i++) {
            currentType = JmmArrayType.of(currentType);
        }

        return currentType;
    }

    /**
     * Build the list of class-level fields from varDecl children of classDecl.
     */
    private List<Symbol> buildFields(JmmNode classDecl) {
        var fields = new ArrayList<Symbol>();
        for (var varDecl : classDecl.getChildren(VAR_DECL)) {
            var fieldName = varDecl.get(JmmAttributes.VAR_DECL.NAME);
            var typeNode = varDecl.getObject("typeNode", JmmNode.class);
            var type = convertType(typeNode);
            fields.add(new Symbol(type, fieldName));
        }
        return fields;
    }

    private List<MethodSymbol> buildMethods(JmmNode classDecl) {
        var methods = new ArrayList<MethodSymbol>();
        var methodSignatures = new HashSet<Signature>();

        for (var methodNode : classDecl.getChildren(METHOD_DECL)) {
            var method = buildMethod(methodNode);
            var signature = method.signature();

            if (!methodSignatures.add(signature)) {
                reports.add(newError(methodNode,
                        "Duplicate method signature '" + formatSignature(signature) +
                                "' in class '" + className + "'"));
                continue;
            }

            methods.add(method);
        }
        return methods;
    }

    private String formatSignature(Signature signature) {
        var builder = new StringBuilder(signature.name()).append("(");

        for (int i = 0; i < signature.parameters().size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }

            builder.append(signature.parameters().get(i).print());
        }

        return builder.append(")").toString();
    }

    private MethodSymbol buildMethod(JmmNode method) {
        var methodName = method.get("name");

        // Get return type
        var returnTypeNode = method.getObject("returnType", JmmNode.class);
        var returnType = convertType(returnTypeNode);

        // Get parameters
        var paramNodes = method.getChildren(PARAM);
        var params = new ArrayList<Symbol>();
        var paramNames = new HashSet<String>();

        for (var paramNode : paramNodes) {
            var paramName = paramNode.get(JmmAttributes.PARAM.NAME);
            var paramTypeNode = paramNode.getObject("typeNode", JmmNode.class);
            var paramType = convertType(paramTypeNode);

            // Check for duplicate parameter names
            if (!paramNames.add(paramName)) {
                reports.add(newError(paramNode, "Duplicate parameter name '" + paramName + "' in method '" + methodName + "'"));
            }

            params.add(new Symbol(paramType, paramName));
        }

        // Get local variables
        var localVarNodes = method.getChildren(VAR_DECL);
        var locals = new ArrayList<Symbol>();
        var localNames = new HashSet<String>();

        for (var varDecl : localVarNodes) {
            var localName = varDecl.get(JmmAttributes.VAR_DECL.NAME);
            var localTypeNode = varDecl.getObject("typeNode", JmmNode.class);
            var localType = convertType(localTypeNode);

            // Check for duplicate local variable names
            if (!localNames.add(localName)) {
                reports.add(newError(varDecl, "Duplicate local variable '" + localName + "' in method '" + methodName + "'"));
            }

            // Check for parameter-local conflict
            if (paramNames.contains(localName)) {
                reports.add(newError(varDecl, "Local variable '" + localName + "' conflicts with parameter in method '" + methodName + "'"));
            }

            locals.add(new Symbol(localType, localName));
        }

        var visibility = method.getOptional("visibility")
                .map(Visibility::fromString)
                .orElse(Visibility.PACKAGE_PROTECTED);
        var isStatic = method.getBoolean(JmmAttributes.METHOD_DECL.IS_STATIC, false);
        return new MethodSymbol(methodName, returnType, params, locals, isStatic, visibility);
    }

}
