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

        code.append(buildFields());
        code.append(buildConstructor(node)).append(NL);

        for (var child : node.getChildren(METHOD_DECL)) {
            code.append(visit(child));
        }

        code.append(R_BRACKET);
        return code.toString();
    }

    private String buildConstructor(JmmNode classNode) {
        String superName = superSimpleName();
        ollirTypes.resetTemporaries();

        StringBuilder body = new StringBuilder();
        body.append("invokespecial(this.").append(superName).append(", \"<init>\").V").append(END_STMT);

        for (var fieldDecl : classNode.getChildren(FIELD_DECL)) {
            if (fieldDecl.getNumChildren() < 2) {
                continue;
            }

            String fieldName = fieldDecl.get("name");

            JmmType fieldType = table.getFields().stream()
                    .filter(field -> field.name().equals(fieldName))
                    .findFirst()
                    .orElseThrow()
                    .type();

            String typeSuffix = ollirTypes.toOllirType(fieldType);

            var initExpr = exprVisitor.visit(fieldDecl.getChild(1));

            body.append(initExpr.getComputation());
            body.append("putfield(this, ")
                    .append(ollirTypes.sanitizeId(fieldName))
                    .append(typeSuffix)
                    .append(", ")
                    .append(initExpr.getCode())
                    .append(").V")
                    .append(END_STMT);
        }

        body.append("ret.V").append(END_STMT);

        StringBuilder code = new StringBuilder();
        code.append(INDENT).append(".construct \"<init>\"().V").append(L_BRACKET);
        code.append(indentBody(body.toString()));
        code.append(INDENT).append(R_BRACKET);
        return code.toString();
    }

    private String buildFields() {
        StringBuilder code = new StringBuilder();

        for (var field : table.getFields()) {
            code.append(INDENT)
                    .append(".field private ")
                    .append(ollirTypes.sanitizeId(field.name()))
                    .append(ollirTypes.toOllirType(field.type()))
                    .append(END_STMT);
        }

        if (!table.getFields().isEmpty()) {
            code.append(NL);
        }

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

        // OLLIR rejects labels that don't precede an instruction. Trailing labels can
        // appear at the tail of a method when the last source statement is an if/else
        // or a loop. visitIfStmt elides the end label when both branches always exit;
        // for everything else (e.g. a non-void method that ends with a while(true) loop
        // whose body always returns) we append a synthetic return with a default value
        // for the method's return type. The synthetic return is unreachable in valid
        // programs, since semantic return-path validation rejects fall-through.
        if (endsWithLabel(body)) {
            body.append(buildSyntheticReturn(currentMethod.returnType()));
        } else if (TypeUtils.voidType().equals(currentMethod.returnType()) && !endsWithReturn(body)) {
            body.append("ret.V").append(END_STMT);
        }

        code.append(indentBody(body.toString()));

        code.append(INDENT).append(R_BRACKET);
        code.append(NL);

        currentMethod = null;
        return code.toString();
    }

    private static boolean endsWithLabel(CharSequence body) {
        int len = body.length();
        int end = len;
        while (end > 0 && Character.isWhitespace(body.charAt(end - 1))) {
            end--;
        }
        if (end == 0) {
            return false;
        }
        int start = end;
        while (start > 0 && body.charAt(start - 1) != '\n') {
            start--;
        }
        String last = body.subSequence(start, end).toString().trim();
        if (!last.endsWith(":")) {
            return false;
        }
        String labelName = last.substring(0, last.length() - 1).trim();
        if (labelName.isEmpty()) {
            return false;
        }
        for (int i = 0; i < labelName.length(); i++) {
            char c = labelName.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '$')) {
                return false;
            }
        }
        return true;
    }

    private String buildSyntheticReturn(JmmType returnType) {
        // Unreachable trailing return chosen to keep OLLIR parseable. Returning the
        // type's natural default avoids inventing a fresh local that might confuse
        // var-table builders.
        if (TypeUtils.voidType().equals(returnType)) {
            return "ret.V" + END_STMT;
        }

        String typeSuffix = ollirTypes.toOllirType(returnType);
        if (TypeUtils.intType().equals(returnType)) {
            return "ret" + typeSuffix + SPACE + "0" + typeSuffix + END_STMT;
        }
        if (TypeUtils.booleanType().equals(returnType)) {
            return "ret" + typeSuffix + SPACE + "0" + typeSuffix + END_STMT;
        }

        String intSuffix = ollirTypes.toOllirType(TypeUtils.intType());
        String tmp = ollirTypes.nextTemp("unreach") + typeSuffix;
        StringBuilder code = new StringBuilder();

        if (returnType.isArray()) {
            code.append(tmp).append(SPACE)
                    .append(ASSIGN).append(typeSuffix).append(SPACE)
                    .append("new(array, 0").append(intSuffix).append(")")
                    .append(typeSuffix).append(END_STMT);
            code.append("ret").append(typeSuffix).append(SPACE).append(tmp).append(END_STMT);
            return code.toString();
        }

        String className = returnType.isClass()
                ? OptUtils.simpleClassName(returnType.asClass().fullyQualifiedName())
                : "Object";
        code.append(tmp).append(SPACE)
                .append(ASSIGN).append(typeSuffix).append(SPACE)
                .append("new(").append(className).append(")")
                .append(typeSuffix).append(END_STMT);
        code.append("invokespecial(").append(tmp).append(", \"<init>\").V").append(END_STMT);
        code.append("ret").append(typeSuffix).append(SPACE).append(tmp).append(END_STMT);
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
        return emitSimpleAssignment(node, lhsName, node.getChild(0));
    }

    private String emitSimpleAssignment(JmmNode context, String lhsName, JmmNode rhsNode) {
        var rhs = exprVisitor.visit(rhsNode);
        var resolved = types.resolveIdentifier(context, lhsName);

        StringBuilder code = new StringBuilder();
        code.append(rhs.getComputation());

        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            JmmType lhsType = resolved.get().type();
            String typeSuffix = ollirTypes.toOllirType(lhsType);

            code.append("putfield(this, ")
                    .append(ollirTypes.sanitizeId(lhsName))
                    .append(typeSuffix)
                    .append(", ")
                    .append(rhs.getCode())
                    .append(").V")
                    .append(END_STMT);

            return code.toString();
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

        // Build branch bodies first so we can detect always-exits paths and avoid
        // dangling end labels (OLLIR forbids labels not followed by an instruction).
        String thenCode = visit(node.getChild(1));
        if (thenCode == null) thenCode = "";
        String elseCode = "";
        if (hasElse) {
            String visited = visit(node.getChild(2));
            if (visited != null) elseCode = visited;
        }

        boolean thenAlwaysExits = endsWithUnconditionalExit(thenCode);
        // The fall-through (no else) path always reaches end_label, so the else side
        // can only be considered terminating when an explicit else exists and exits.
        boolean elseAlwaysExits = hasElse && endsWithUnconditionalExit(elseCode);

        StringBuilder code = new StringBuilder();
        code.append(cond.getComputation());
        code.append("if (").append(cond.getCode()).append(") goto ").append(thenLabel).append(END_STMT);

        code.append(elseCode);
        if (!elseAlwaysExits) {
            code.append("goto ").append(endLabel).append(END_STMT);
        }

        code.append(thenLabel).append(":\n");
        code.append(thenCode);

        if (!thenAlwaysExits || !elseAlwaysExits) {
            code.append(endLabel).append(":\n");
        }
        return code.toString();
    }

    /**
     * True when {@code body}'s last non-empty line is an unconditional control transfer
     * (return/goto). Used by if/else lowering to avoid emitting an end-label that no
     * branch can fall through to, which would otherwise trail a method as a dangling
     * label and fail OLLIR parsing.
     */
    private static boolean endsWithUnconditionalExit(CharSequence body) {
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
        return last.startsWith("ret.")
                || last.startsWith("ret ")
                || last.startsWith("goto ");
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

        return emitSimpleAssignment(header, name, header.getChild(0));
    }

    private String emitIndexedAssignment(JmmNode context, String arrayName, java.util.List<JmmNode> children) {
        int childCount = children.size();
        if (childCount < 2) {
            return "";
        }

        var resolved = types.resolveIdentifier(context, arrayName);

        JmmType currentType = resolved.map(TypeUtils.ResolvedIdentifier::type)
            .orElse(JmmArrayType.of(TypeUtils.intType()));
        String currentOperand = ollirTypes.sanitizeId(arrayName) + ollirTypes.toOllirType(currentType);
        int indexCount = childCount - 1;

        StringBuilder code = new StringBuilder();

        if (resolved.isPresent() && resolved.get().accessType() == AccessType.FIELD) {
            String arraySuffix = ollirTypes.toOllirType(currentType);
            currentOperand = ollirTypes.nextTemp("arr") + arraySuffix;
            code.append(currentOperand).append(SPACE)
                    .append(ASSIGN).append(arraySuffix).append(SPACE)
                    .append("getfield(this, ")
                    .append(ollirTypes.sanitizeId(arrayName))
                    .append(arraySuffix)
                    .append(")")
                    .append(arraySuffix)
                    .append(END_STMT);
        }

        for (int i = 0; i < indexCount; i++) {
            var indexExpr = exprVisitor.visit(children.get(i));
            code.append(indexExpr.getComputation());

            JmmType elementType = reduceArrayDepth(currentType, 1);
            String elementSuffix = ollirTypes.toOllirType(elementType != null ? elementType : TypeUtils.intType());

            boolean isLastIndex = i == indexCount - 1;
            if (isLastIndex) {
                var rhs = exprVisitor.visit(children.getLast());
                code.append(rhs.getComputation());
                code.append(currentOperand).append("[").append(indexExpr.getCode()).append("]").append(elementSuffix)
                        .append(SPACE)
                        .append(ASSIGN).append(elementSuffix).append(SPACE)
                        .append(rhs.getCode()).append(END_STMT);
                return code.toString();
            }

            String tmp = ollirTypes.nextTemp("arr") + elementSuffix;
            code.append(tmp).append(SPACE)
                .append(ASSIGN).append(elementSuffix).append(SPACE)
                .append(currentOperand).append("[").append(indexExpr.getCode()).append("]").append(elementSuffix)
                .append(END_STMT);

            currentOperand = tmp;
            currentType = elementType;
        }

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
