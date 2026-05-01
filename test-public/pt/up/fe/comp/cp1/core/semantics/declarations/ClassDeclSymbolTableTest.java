package pt.up.fe.comp.cp1.core.semantics.declarations;

import org.junit.Test;
import pt.up.fe.comp.jmm.analysis.table.Signature;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmClassType;
import pt.up.fe.comp.jmm.analysis.table.type.impls.JmmPrimitiveType;
import pt.up.fe.comp.test.env.JmmTestEnv;

import java.util.List;


/**
 * Test variable lookup.
 */
public class ClassDeclSymbolTableTest extends JmmTestEnv {
    private static final String BASE_PATH = "pt/up/fe/comp/cp1/core/semantics/declarations/symboltable/";
    private static final String RESOURCES_LOCATION = "test-public";

    public ClassDeclSymbolTableTest() {
        super(BASE_PATH, RESOURCES_LOCATION);
    }


    @Test
    public void PackageDeclaration() {
        var semantics = symbolTable("Package.jmm", false);
        super.assertEquals("Package name should be ${expected}", resourcesPackage(), semantics.getSymbolTable().packageName());
    }

    @Test
    public void ClassDecl() {
        var semantics = symbolTable("ClassDecl.jmm", false);
        super.assertEquals("Class name should be ${expected}", "ClassDecl", semantics.getSymbolTable().getClassName());
        super.assertEquals("Class fully qualified name should be ${expected}", qualifiedNameFor("ClassDecl"), semantics.getSymbolTable().getFullyQualifiedName());
    }


    @Test
    public void ClassAndSuper() {
        var semantics = symbolTable("Super.jmm", false);
        super.assertEquals("Class name should be ${expected}", "Super", semantics.getSymbolTable().getClassName());
        super.assertEquals("Super class should be ${expected}", "Quicksort", semantics.getSymbolTable().getSuper());
        super.assertEquals("Super fully qualified name should be ${expected}", "examples.Quicksort", semantics.getSymbolTable().getSuperFullyQualifiedName());
    }


    @Test
    public void Fields() {
        var semantics = symbolTable("MethodsAndFields.jmm", false);
        var fields = semantics.getSymbolTable().getFields();
        super.assertEquals("Number of fields should be ${expected}, but was ${actual}", 3, fields.size());
        var checkInt = 0;
        var checkBool = 0;
        var checkObj = 0;

        var classObj = JmmClassType.ofInstance(qualifiedNameFor("MethodsAndFields"), false);

        for (var f : fields) {
            super.assertTrue("Field type is " + f.type() + ", but it was expected either a class, int or boolean", f.type().equals(classObj) || f.type().equals(JmmPrimitiveType.BOOLEAN) || f.type().equals(JmmPrimitiveType.INT));
            if (f.type().equals(classObj))
                checkObj++;
            else if (f.type().equals(JmmPrimitiveType.BOOLEAN))
                checkBool++;
            else if (f.type().equals(JmmPrimitiveType.INT))
                checkInt++;

        }

        super.assertEquals("A Field of type int", 1, checkInt);
        super.assertEquals("A Field of type boolean", 1, checkBool);
        super.assertEquals("A Field of type object", 1, checkObj);
    }

    @Test
    public void Methods() {
        var semantics = symbolTable("MethodsAndFields.jmm", false);
        var st = semantics.getSymbolTable();
        var methods = st.getMethods();
        super.assertTrue("Expected to have at least ${expected} methods.", methods.size() >= 4);

        var getField1Opt = st.getMethod(Signature.of("getField1", List.of()));
        super.assertTrue("getField1 method should exist", getField1Opt.isPresent());
    }



}
