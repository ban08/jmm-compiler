package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.utils.Attributes;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.NodeUtils;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private final String END_STMT = ";\n";
    private final String NL = "\n";
    private final String L_BRACKET = " {\n";
    private final String R_BRACKET = "}\n";


    private final SymbolTable table;

    private final TypeUtils types;
    private final OptUtils ollirTypes;


    private final OllirExprGeneratorVisitor exprVisitor;

    private MethodSymbol currentMethod;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        exprVisitor = new OllirExprGeneratorVisitor(table, ollirTypes);
        currentMethod = null;
    }


    @Override
    protected void buildVisitor() {

        addVisit(PROGRAM, this::visitProgram);
        addVisit(PACKAGE_DECL, this::visitPackageDecl);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(VAR_DECL, this::simpleVarDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(METHOD_DECL, this::visitMethodDecl);
        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
//        setDefaultVisit(this::defaultVisit);
    }


    private String simpleVarDecl(JmmNode varDecl, Void unused) {
        return varDecl.get("name") + ollirTypes.toOllirType(varDecl.getObject("typeNode", JmmNode.class)) + ";";
    }

    private String visitParam(JmmNode varDecl, Void unused) {
        return varDecl.get("name") + ollirTypes.toOllirType(varDecl.getObject("typeNode", JmmNode.class));
    }



    private String visitPackageDecl(JmmNode packageDecl, Void unused) {
        return "package " + String.join(".", packageDecl.getObjectAsList("path", String.class)) + ";\n";
    }


    private String visitAssignStmt(JmmNode node, Void unused) {
        // TODO: Several hard-coded things, should be rewritten

        var rhs = exprVisitor.visit(node.getChild(0));

        // code to compute self
        // statement has type of lhs
        var varName = node.get(JmmAttributes.ASSIGN_STMT.VAR);
        JmmType thisType = TypeUtils.intType();
        String typeString = ollirTypes.toOllirType(thisType);
        var varCode = ollirTypes.sanitizeId(varName) + typeString;


        var code = new StringBuilder();

        // code to compute the children
        code.append(rhs.getComputation());

        code.append(varCode);
        code.append(SPACE);

        code.append(ASSIGN);
        code.append(typeString);
        code.append(SPACE);

        code.append(rhs.getCode());

        code.append(END_STMT);

        return code.toString();
    }

    private String visitReturn(JmmNode node, Void unused) {

        System.out.println("[TODO] OllirGeneratorVisitor.visitReturn(): Assuming always returns an integer expression, needs to be expanded");
        JmmType retType = TypeUtils.intType();
        var expr = exprVisitor.visit(node.getChild(0));


        StringBuilder code = new StringBuilder();

        code.append(expr.getComputation());
        code.append("ret");
        code.append(ollirTypes.toOllirType(retType));
        code.append(SPACE);

        code.append(expr.getCode());

        code.append(END_STMT);

        return code.toString();
    }

    private String visitMethodDecl(JmmNode node, Void unused) {

        currentMethod = table.getMethod(TypeUtils.with(table).getMethodDeclSignature(node)).orElseThrow();

        StringBuilder code = new StringBuilder(".method ");

        boolean isPublic = NodeUtils.getBooleanAttribute(node, "isPublic", "false");

        if (isPublic) {
            code.append("public ");
        }

        if (node.getObject("isStatic", Boolean.class)) {
            code.append("static ");
        }

        // name
        var name = ollirTypes.sanitizeId(node.get("name"));
        code.append(name);

        // params
        // TODO: Hardcoded for a single parameter, needs to be expanded
        var paramsCode = visit(node.getChild(1));
        code.append("(" + paramsCode + ")");

        // type
        var retType = ollirTypes.toOllirType(currentMethod.returnType());//table.getReturnType(node.get("name")));
        code.append(retType);
        code.append(L_BRACKET);

        var stmts = node.getChildren(STMT);
        if (!stmts.isEmpty()) {
            // rest of its children stmts
            var stmtsCode = stmts.stream().map(this::visit).collect(Collectors.joining("\n   ", "   ", ""));
            code.append(stmtsCode);
        }

        code.append(R_BRACKET);
        code.append(NL);

        currentMethod = null;

        return code.toString();
    }

    private String visitClass(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        code.append(NL);
        code.append(table.getClassName());

        code.append(L_BRACKET);
        code.append(NL);
        code.append(NL);


        code.append(buildConstructor());
        code.append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            var result = visit(child);
            code.append(result);
        }

        code.append(R_BRACKET);

        return code.toString();
    }

    private String buildConstructor() {
        return """
                .construct %s().V {
                    invokespecial(this, "<init>").V;
                }
                """.formatted(table.getClassName());
    }

    private String visitProgram(JmmNode node, Void unused) {

        StringBuilder code = new StringBuilder();

        node.getChildren().stream().map(this::visit).forEach(code::append);

        return code.toString();
    }

    /**
     * Default visitor. Visits every child node and return an empty string.
     *
     * @param node
     * @param unused
     * @return
     */
    private String defaultVisit(JmmNode node, Void unused) {

        for (var child : node.getChildren()) {
            visit(child);
        }

        return "";
    }
}
