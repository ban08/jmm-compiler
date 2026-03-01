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

        System.out.println("[TODO] TypeUtils.convertType(): Implement for classes and arrays");
        var name = typeNode.get("name");
        return JmmPrimitiveType.fromString(name).orElseThrow();
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

        System.out.println("[TODO] TypeUtils.getMethodDeclSignature(): Supporting only methods with a single parameter that is an int, needs to be expanded");
        var params = List.of(intType());

        // Create method signature with method name and types of parameters
        return new Signature(methodName, params);
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
