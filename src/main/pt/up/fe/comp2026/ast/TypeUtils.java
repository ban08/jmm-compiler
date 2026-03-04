package pt.up.fe.comp2026.ast;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.jmm.ast.JmmKind;
import pt.up.fe.comp2026.symboltable.JmmSymbolTable;
import pt.up.fe.specs.util.SpecsCheck;
import pt.up.fe.specs.util.exceptions.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;


/**
 * Utility methods regarding types.
 */
public class TypeUtils {


    private final JmmSymbolTable table;

    public TypeUtils(SymbolTable table) {
        this.table = (JmmSymbolTable) table;
    }

    public static TypeUtils with(SymbolTable table) {
        return new TypeUtils(table);
    }

    public static JmmPrimitiveType intType() {
        return JmmPrimitiveType.INT;
    }

    public JmmType convertType(JmmNode typeNode) {
        assert (TYPE.check(typeNode));

        var name = typeNode.get("name");
        boolean isArray = NodeUtils.getBooleanAttribute(typeNode, "isArray", "false");

        // Check primitives first
        var primitive = JmmPrimitiveType.fromString(name);
        if (primitive.isPresent()) {
            if (isArray) {
                return JmmArrayType.of(primitive.get());
            }
            return primitive.get();
        }

        // Class type - resolve via symbol table
        var importedFqn = table.getImportedFullyQualifiedName(name);
        if (importedFqn.isPresent()) {
            var classType = JmmClassType.ofInstance(importedFqn.get(), true);
            if (isArray) return JmmArrayType.of(classType);
            return classType;
        }

        // Check if it's the declared class itself
        var className = table.getFullyQualifiedName();
        var simpleName = className.substring(className.lastIndexOf('.') + 1);
        if (name.equals(simpleName)) {
            var classType = JmmClassType.ofInstance(className, false);
            if (isArray) return JmmArrayType.of(classType);
            return classType;
        }

        // Check implicit imports
        if (table.isImplicitImport(name)) {
            var implicitTable = table.getImplicitImport(name);
            if (implicitTable.isPresent()) {
                var classType = JmmClassType.ofInstance(implicitTable.get().getFullyQualifiedName(), true);
                if (isArray) return JmmArrayType.of(classType);
                return classType;
            }
        }

        // Fallback - unknown class type
        var classType = JmmClassType.ofInstance(name, false);
        if (isArray) return JmmArrayType.of(classType);
        return classType;
    }


    /**
     * Gets the {@link JmmType} of an arbitrary expression.
     *
     * @param expr
     * @return
     */
    public JmmType getExprType(JmmNode expr) {
        return switch (expr.getKind()) {
            case INTEGER_LITERAL -> intType();
            case BINARY_EXPR -> getBinExprType(expr);
            case VAR_REF_EXPR -> getVarExprType(expr);
            default ->
                    throw new UnsupportedOperationException("Can't compute type for expression kind '" + expr.getKind() + "'");
        };
    }


    public Signature getMethodDeclSignature(JmmNode methodDecl) {
        // Ensure given node is a MethodDecl
        METHOD_DECL.check(methodDecl);

        // Get name of the method
        var methodName = methodDecl.get("name");

        // Get the types of all parameters
        var paramTypes = methodDecl.getChildren(PARAM).stream()
                .map(param -> {
                    var typeNode = param.getObject("typeNode", JmmNode.class);
                    return convertType(typeNode);
                })
                .toList();

        // Create method signature with method name and types of parameters
        return new Signature(methodName, paramTypes);
    }


    private JmmType getBinExprType(JmmNode binaryExpr) {

        // Get operator
        String operator = binaryExpr.get("op");

        return switch (operator) {
            case "+", "*" -> intType();
            default ->
                    throw new RuntimeException("Unknown operator '" + operator + "' of expression '" + binaryExpr + "'");
        };
    }

    private JmmType getVarExprType(JmmNode varRefExpr) {
        System.out.println("[TODO] TypeUtils.getVarExprType(): Implement type inference for VarExpr. You will need to determine in which method the VarRef is and use the symbol table");
        return intType();
    }

}
