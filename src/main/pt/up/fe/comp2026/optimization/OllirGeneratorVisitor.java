package pt.up.fe.comp2026.optimization;

import pt.up.fe.comp.jmm.analysis.table.MethodSymbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmArrayType;
import pt.up.fe.comp.jmm.analysis.table.type.JmmType;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2026.ast.AccessType;
import pt.up.fe.comp2026.ast.TypeUtils;
import pt.up.fe.comp2026.jmm.ast.JmmAttributes;

import java.util.stream.Collectors;

import static pt.up.fe.comp2026.jmm.ast.JmmKind.*;

/**
 * Generates OLLIR code from JmmNodes that are not expressions.
 */
public class OllirGeneratorVisitor extends AJmmVisitor<Void, String> {

    private static final String SPACE = " ";
    private static final String ASSIGN = ":=";
    private static final String END_STMT = ";\n";
    private static final String NL = "\n";
    private static final String L_BRACKET = " {\n";
    private static final String R_BRACKET = "}\n";
    private static final String DEFAULT_SUPER = "Object";
    private static final String INDENT = "    ";

    private final SymbolTable table;
    private final TypeUtils types;
    private final OptUtils ollirTypes;
    private final OllirExprGeneratorVisitor exprVisitor;

    private MethodSymbol currentMethod;

    public OllirGeneratorVisitor(SymbolTable table) {
        this.table = table;
        this.types = new TypeUtils(table);
        this.ollirTypes = new OptUtils(types);
        this.exprVisitor = new OllirExprGeneratorVisitor(table, ollirTypes);
        this.currentMethod = null;
    }


    @Override
    protected void buildVisitor() {
        addVisit(PROGRAM, this::visitProgram);
        addVisit(PACKAGE_DECL, this::visitPackageDecl);
        addVisit(IMPORT_DECL, this::visitImportDecl);
        addVisit(CLASS_DECL, this::visitClass);
        addVisit(VAR_DECL, this::visitVarDecl);
        addVisit(PARAM, this::visitParam);
        addVisit(METHOD_DECL, this::visitMethodDecl);

        addVisit(RETURN_STMT, this::visitReturn);
        addVisit(ASSIGN_STMT, this::visitAssignStmt);
        addVisit(IF_STMT, this::visitIfStmt);
        addVisit(WHILE_STMT, this::visitWhileStmt);
        addVisit(DO_WHILE_STMT, this::visitDoWhileStmt);
        addVisit(FOR_STMT, this::visitForStmt);
        addVisit(COMPOUND_STMT, this::visitCompoundStmt);
        addVisit(EXPR_STMT, this::visitExprStmt);
        addVisit(ARRAY_ASSIGN_STMT, this::visitArrayAssignStmt);
    }

    private String visitProgram(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        for (var child : node.getChildren()) {
            code.append(visit(child));
        }
        return code.toString();
    }

    private String visitPackageDecl(JmmNode packageDecl, Void unused) {
        return "package " + String.join(".", packageDecl.getObjectAsList("path", String.class)) + ";\n";
    }

    private String visitImportDecl(JmmNode importDecl, Void unused) {
        return "import " + String.join(".", importDecl.getObjectAsList("path", String.class)) + ";\n";
    }

    private String visitVarDecl(JmmNode varDecl, Void unused) {
        // Local variables are implicit in OLLIR (introduced by their first assignment).
        return "";
    }

    private String visitParam(JmmNode param, Void unused) {
        // Sanitize name: parameter identifiers like `array`, `bool`, `static`, etc. clash with
        // OLLIR keywords and would otherwise misparse the type that follows.
        return ollirTypes.sanitizeId(param.get("name"))
                + ollirTypes.toOllirType(param.getObject("typeNode", JmmNode.class));
    }

    private String visitClass(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        code.append(NL);

        code.append(table.getClassName());
        code.append(" extends ").append(superSimpleName());
        code.append(L_BRACKET).append(NL);

        code.append(buildConstructor()).append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            code.append(visit(child));
        }

        code.append(R_BRACKET);
        return code.toString();
    }

    private String buildConstructor() {
        String superName = superSimpleName();
        ollirTypes.resetTemporaries();

        StringBuilder body = new StringBuilder();
        body.append("invokespecial(this.").append(superName).append(", \"<init>\").V").append(END_STMT);
        body.append("ret.V").append(END_STMT);

        StringBuilder code = new StringBuilder();
        code.append(INDENT).append(".construct \"<init>\"().V").append(L_BRACKET);
        code.append(indentBody(body.toString()));
        code.append(INDENT).append(R_BRACKET);
        return code.toString();
    }

    private String superSimpleName() {
        var superFqn = table.getSuperFullyQualifiedName();
        if (superFqn == null || superFqn.isBlank()) {
            return DEFAULT_SUPER;
        }
        return OptUtils.simpleClassName(superFqn);
    }

    private String visitMethodDecl(JmmNode node, Void unused) {
        currentMethod = table.getMethod(TypeUtils.with(table).getMethodDeclSignature(node)).orElseThrow();
        ollirTypes.resetTemporaries();

        StringBuilder code = new StringBuilder();
        code.append(INDENT).append(".method ");

        // Visibility is optional in the JMM grammar; absence means package-default,
        // which the OLLIR parser maps to AccessModifier.DEFAULT when no modifier is emitted.
        var visibility = node.getOptional("visibility");
        if (visibility.isPresent()) {
            code.append(visibility.get()).append(SPACE);
        }

        if (node.getObject("isStatic", Boolean.class)) {
            code.append("static ");
        }

        code.append(ollirTypes.sanitizeId(node.get("name")));

        // params: iterate ALL PARAM children
        String paramsCode = node.getChildren(PARAM).stream()
                .map(this::visit)
                .collect(Collectors.joining(", "));
        code.append("(").append(paramsCode).append(")");

        // return type
        code.append(ollirTypes.toOllirType(currentMethod.returnType()));
        code.append(L_BRACKET);

        StringBuilder body = new StringBuilder();
        for (var stmt : node.getChildren(STMT)) {
            String stmtCode = visit(stmt);
            if (stmtCode != null) {
                body.append(stmtCode);
            }
        }

        // Void methods that don't end with an explicit return must still terminate with ret.V
        // so that any trailing label (from ifs/loops) attaches to a real instruction.
        if (TypeUtils.voidType().equals(currentMethod.returnType()) && !endsWithReturn(body)) {
            body.append("ret.V").append(END_STMT);
        }

        code.append(indentBody(body.toString()));

        code.append(INDENT).append(R_BRACKET);
        code.append(NL);

        currentMethod = null;
        return code.toString();
    }

    private static boolean endsWithReturn(CharSequence body) {
        // Look for the last non-empty trimmed line and check it is a `ret.<...>;`.
        int len = body.length();
        int end = len;
        while (end > 0 && Character.isWhitespace(body.charAt(end - 1))) {
            end--;
        }
        int start = end;
        while (start > 0 && body.charAt(start - 1) != '\n') {
            start--;
        }
        String last = body.subSequence(start, end).toString().trim();
        return last.startsWith("ret.") || last.startsWith("ret ");
    }

    private static String indentBody(String body) {
        if (body.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String line : body.split("\n", -1)) {
            if (line.isEmpty()) {
                sb.append('\n');
                continue;
            }
            sb.append(INDENT).append(INDENT).append(line).append('\n');
        }
        return sb.toString();
    }

    private String visitReturn(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        if (node.getNumChildren() == 0) {
            code.append("ret.V").append(END_STMT);
            return code.toString();
        }

        var expr = exprVisitor.visit(node.getChild(0));
        code.append(expr.getComputation());
        code.append("ret").append(ollirTypes.toOllirType(currentMethod.returnType())).append(SPACE);
        code.append(expr.getCode()).append(END_STMT);
        return code.toString();
    }

    private String visitAssignStmt(JmmNode node, Void unused) {
        String lhsName = node.get(JmmAttributes.ASSIGN_STMT.VAR);
        var rhs = exprVisitor.visit(node.getChild(0));

        var resolved = types.resolveIdentifier(node, lhsName);

        StringBuilder code = new StringBuilder();
        code.append(rhs.getComputation());

        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            throw new UnsupportedOperationException("Field writes are owned by the field OLLIR task");
        }

        // Local or param target.
        JmmType lhsType = resolved.map(TypeUtils.ResolvedIdentifier::type).orElse(TypeUtils.intType());
        String typeSuffix = ollirTypes.toOllirType(lhsType);

        code.append(ollirTypes.sanitizeId(lhsName)).append(typeSuffix).append(SPACE)
                .append(ASSIGN).append(typeSuffix).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);
        return code.toString();
    }

    private String visitArrayAssignStmt(JmmNode node, Void unused) {
        String lhsName = node.get(JmmAttributes.ARRAY_ASSIGN_STMT.VAR);
        return emitIndexedAssignment(node, lhsName, node.getChildren());
    }

    private String visitCompoundStmt(JmmNode node, Void unused) {
        StringBuilder code = new StringBuilder();
        for (var child : node.getChildren(STMT)) {
            String c = visit(child);
            if (c != null) code.append(c);
        }
        return code.toString();
    }

    private String visitExprStmt(JmmNode node, Void unused) {
        var expr = exprVisitor.visit(node.getChild(0));
        // The value is discarded; only the side effects matter.
        return expr.getComputation();
    }

    private String visitIfStmt(JmmNode node, Void unused) {
        var cond = exprVisitor.visit(node.getChild(0));
        boolean hasElse = node.getNumChildren() > 2;

        String thenLabel = ollirTypes.nextLabel("if_then_");
        String endLabel = ollirTypes.nextLabel("if_end_");

        StringBuilder code = new StringBuilder();
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(thenLabel).append(END_STMT);

        // Else branch (or fall-through nothing) executes when cond is false.
        if (hasElse) {
            String elseCode = visit(node.getChild(2));
            if (elseCode != null) code.append(elseCode);
        }
        code.append("goto ").append(endLabel).append(END_STMT);

        code.append(thenLabel).append(":\n");
        String thenCode = visit(node.getChild(1));
        if (thenCode != null) code.append(thenCode);

        code.append(endLabel).append(":\n");
        return code.toString();
    }

    private String visitWhileStmt(JmmNode node, Void unused) {
        String condLabel = ollirTypes.nextLabel("while_cond_");
        String bodyLabel = ollirTypes.nextLabel("while_body_");
        String endLabel = ollirTypes.nextLabel("while_end_");

        var cond = exprVisitor.visit(node.getChild(0));

        StringBuilder code = new StringBuilder();
        code.append(condLabel).append(":\n");
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(bodyLabel).append(END_STMT);
        code.append("goto ").append(endLabel).append(END_STMT);
        code.append(bodyLabel).append(":\n");
        String bodyCode = visit(node.getChild(1));
        if (bodyCode != null) code.append(bodyCode);
        code.append("goto ").append(condLabel).append(END_STMT);
        code.append(endLabel).append(":\n");
        return code.toString();
    }

    private String visitDoWhileStmt(JmmNode node, Void unused) {
        // Children: body, cond
        String bodyLabel = ollirTypes.nextLabel("dowhile_body_");
        StringBuilder code = new StringBuilder();
        code.append(bodyLabel).append(":\n");
        String bodyCode = visit(node.getChild(0));
        if (bodyCode != null) code.append(bodyCode);

        var cond = exprVisitor.visit(node.getChild(1));
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(bodyLabel).append(END_STMT);
        return code.toString();
    }

    private String visitForStmt(JmmNode node, Void unused) {
        // The grammar admits an optional init, cond, update, then a STMT body.
        JmmNode init = null;
        JmmNode cond = null;
        JmmNode update = null;
        JmmNode body = null;

        boolean firstHeaderSeen = false;
        for (var child : node.getChildren()) {
            if (FOR_HEADER_ASSIGN.check(child)) {
                if (!firstHeaderSeen) {
                    init = child;
                    firstHeaderSeen = true;
                } else {
                    update = child;
                }
            } else if (EXPR.check(child)) {
                cond = child;
            } else if (STMT.check(child)) {
                body = child;
            }
        }

        StringBuilder code = new StringBuilder();
        if (init != null) {
            code.append(emitForHeaderAssign(init));
        }

        String condLabel = ollirTypes.nextLabel("for_cond_");
        String bodyLabel = ollirTypes.nextLabel("for_body_");
        String endLabel = ollirTypes.nextLabel("for_end_");

        code.append(condLabel).append(":\n");
        if (cond != null) {
            var condResult = exprVisitor.visit(cond);
            code.append(condResult.getComputation());
            code.append("if (").append(condResult.getCode()).append(") goto ").append(bodyLabel).append(END_STMT);
            code.append("goto ").append(endLabel).append(END_STMT);
        } else {
            code.append("goto ").append(bodyLabel).append(END_STMT);
        }

        code.append(bodyLabel).append(":\n");
        if (body != null) {
            String bodyCode = visit(body);
            if (bodyCode != null) code.append(bodyCode);
        }
        if (update != null) {
            code.append(emitForHeaderAssign(update));
        }
        code.append("goto ").append(condLabel).append(END_STMT);
        code.append(endLabel).append(":\n");

        return code.toString();
    }

    private String emitForHeaderAssign(JmmNode header) {
        String name = header.get(JmmAttributes.FOR_HEADER_ASSIGN.VAR);
        int childCount = header.getNumChildren();

        if (childCount != 1) {
            return emitIndexedAssignment(header, name, header.getChildren());
        }

        var rhs = exprVisitor.visit(header.getChild(0));
        var resolved = types.resolveIdentifier(header, name);

        StringBuilder code = new StringBuilder();
        code.append(rhs.getComputation());

        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            throw new UnsupportedOperationException("Field writes are owned by the field OLLIR task");
        }

        JmmType lhsType = resolved.map(TypeUtils.ResolvedIdentifier::type).orElse(TypeUtils.intType());
        String typeSuffix = ollirTypes.toOllirType(lhsType);
        code.append(ollirTypes.sanitizeId(name)).append(typeSuffix).append(SPACE)
                .append(ASSIGN).append(typeSuffix).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return code.toString();
    }

    private String emitIndexedAssignment(JmmNode context, String arrayName, java.util.List<JmmNode> children) {
        int childCount = children.size();
        if (childCount < 2) {
            return "";
        }

        var resolved = types.resolveIdentifier(context, arrayName);
        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            throw new UnsupportedOperationException("Field writes are owned by the field OLLIR task");
        }

        JmmType arrayType = resolved.map(TypeUtils.ResolvedIdentifier::type)
                .orElse(JmmArrayType.of(TypeUtils.intType()));

        String arrayTypeSuffix = ollirTypes.toOllirType(arrayType);
        int indexCount = childCount - 1;

        StringBuilder code = new StringBuilder();

        StringBuilder indexedLhs = new StringBuilder();
        indexedLhs.append(ollirTypes.sanitizeId(arrayName)).append(arrayTypeSuffix);

        for (int i = 0; i < indexCount; i++) {
            var indexExpr = exprVisitor.visit(children.get(i));
            code.append(indexExpr.getComputation());
            indexedLhs.append("[").append(indexExpr.getCode()).append("]");
        }

        JmmType targetType = reduceArrayDepth(arrayType, indexCount);
        String targetTypeSuffix = ollirTypes.toOllirType(targetType != null ? targetType : TypeUtils.intType());
        indexedLhs.append(targetTypeSuffix);

        var rhs = exprVisitor.visit(children.getLast());
        code.append(rhs.getComputation());

        code.append(indexedLhs).append(SPACE)
                .append(ASSIGN).append(targetTypeSuffix).append(SPACE)
                .append(rhs.getCode()).append(END_STMT);

        return code.toString();
    }

    private static JmmType reduceArrayDepth(JmmType type, int depth) {
        JmmType current = type;

        for (int i = 0; i < depth; i++) {
            if (current == null || !current.isArray()) {
                return current;
            }

            var array = current.asArray();
            current = array.dimension() == 1
                    ? array.itemType()
                    : JmmArrayType.of(array.itemType(), array.dimension() - 1);
        }

        return current;
    }
}
