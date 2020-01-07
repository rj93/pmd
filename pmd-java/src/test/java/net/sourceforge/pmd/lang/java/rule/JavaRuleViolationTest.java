/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.LanguageVersion;
import net.sourceforge.pmd.lang.LanguageVersionHandler;
import net.sourceforge.pmd.lang.ParserOptions;
import net.sourceforge.pmd.lang.ast.AstAnalysisContext;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit;
import net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.lang.java.internal.JavaProcessingStage;
import net.sourceforge.pmd.lang.java.symboltable.ScopeAndDeclarationFinder;

/**
 * @author Philip Graf
 */
public class JavaRuleViolationTest {
    /**
     * Verifies that {@link JavaRuleViolation} sets the variable name for an
     * {@link ASTFormalParameter} node.
     */
    @Test
    public void testASTFormalParameterVariableName() {
        ASTCompilationUnit ast = parse("class Foo { void bar(int x) {} }");
        final ASTFormalParameter node = ast.getFirstDescendantOfType(ASTFormalParameter.class);
        final RuleContext context = new RuleContext();
        final JavaRuleViolation violation = new JavaRuleViolation(null, context, node, null);
        assertEquals("x", violation.getVariableName());
    }

    private ASTCompilationUnit parse(final String code) {
        LanguageVersion version = LanguageRegistry.getLanguage(JavaLanguageModule.NAME).getDefaultVersion();
        final LanguageVersionHandler languageVersionHandler = version.getLanguageVersionHandler();
        final ParserOptions options = languageVersionHandler.getDefaultParserOptions();
        final ASTCompilationUnit ast = (ASTCompilationUnit) languageVersionHandler.getParser(options).parse(null,
                new StringReader(code));
        // set scope of AST nodes
        ast.jjtAccept(new ScopeAndDeclarationFinder(), null);
        JavaProcessingStage.JAVA_PROCESSING.processAST(ast, new AstAnalysisContext() {
            @Override
            public ClassLoader getTypeResolutionClassLoader() {
                return JavaRuleViolation.class.getClassLoader();
            }

            @Override
            public LanguageVersion getLanguageVersion() {
                return version;
            }
        });
        return ast;
    }

    /**
     * Tests that the method name is taken correctly from the given node.
     *
     * @see <a href="https://sourceforge.net/p/pmd/bugs/1250/">#1250</a>
     */
    @Test
    public void testMethodName() {
        ASTCompilationUnit ast = parse("class Foo { void bar(int x) {} }");
        ASTMethodDeclaration md = ast.getFirstDescendantOfType(ASTMethodDeclaration.class);
        final RuleContext context = new RuleContext();
        final JavaRuleViolation violation = new JavaRuleViolation(null, context, md, null);
        assertEquals("bar", violation.getMethodName());
    }

    /**
     * Tests that the enum name is taken correctly from the given node.
     */
    @Test
    public void testEnumName() {
        ASTCompilationUnit ast = parse("enum Foo {FOO; void bar(int x) {} }");
        ASTMethodDeclaration md = ast.getFirstDescendantOfType(ASTMethodDeclaration.class);
        final RuleContext context = new RuleContext();
        final JavaRuleViolation violation = new JavaRuleViolation(null, context, md, null);
        assertEquals("Foo", violation.getClassName());
    }

    /**
     * Tests that the class name is taken correctly, even if the node is outside
     * of a class scope, e.g. a import declaration.
     *
     * @see <a href="https://sourceforge.net/p/pmd/bugs/1529/">#1529</a>
     */
    @Test
    public void testPackageAndClassName() {
        ASTCompilationUnit ast = parse("package pkg; import java.util.List; public class Foo { }");
        ASTImportDeclaration importNode = ast.getFirstDescendantOfType(ASTImportDeclaration.class);

        JavaRuleViolation violation = new JavaRuleViolation(null, new RuleContext(), importNode, null);
        assertEquals("pkg", violation.getPackageName());
        assertEquals("Foo", violation.getClassName());
    }

    @Test
    public void testPackageAndEnumName() {
        ASTCompilationUnit ast = parse("package pkg; import java.util.List; public enum FooE { }");
        ASTImportDeclaration importNode = ast.getFirstDescendantOfType(ASTImportDeclaration.class);

        JavaRuleViolation violation = new JavaRuleViolation(null, new RuleContext(), importNode, null);
        assertEquals("pkg", violation.getPackageName());
        assertEquals("FooE", violation.getClassName());
    }

    @Test
    public void testDefaultPackageAndClassName() {
        ASTCompilationUnit ast = parse("import java.util.List; public class Foo { }");
        ASTImportDeclaration importNode = ast.getFirstDescendantOfType(ASTImportDeclaration.class);

        JavaRuleViolation violation = new JavaRuleViolation(null, new RuleContext(), importNode, null);
        assertEquals("", violation.getPackageName());
        assertEquals("Foo", violation.getClassName());
    }

    @Test
    public void testPackageAndMultipleClassesName() {
        ASTCompilationUnit ast = parse("package pkg; import java.util.List; class Foo { } public class Bar { }");
        ASTImportDeclaration importNode = ast.getFirstDescendantOfType(ASTImportDeclaration.class);

        JavaRuleViolation violation = new JavaRuleViolation(null, new RuleContext(), importNode, null);
        assertEquals("pkg", violation.getPackageName());
        assertEquals("Bar", violation.getClassName());
    }

    @Test
    public void testPackageAndPackagePrivateClassesName() {
        ASTCompilationUnit ast = parse("package pkg; import java.util.List; class Foo { }");
        ASTImportDeclaration importNode = ast.getFirstDescendantOfType(ASTImportDeclaration.class);

        JavaRuleViolation violation = new JavaRuleViolation(null, new RuleContext(), importNode, null);
        assertEquals("pkg", violation.getPackageName());
        assertEquals("Foo", violation.getClassName());
    }

    /**
     * Test that the name of the inner class is taken correctly.
     */
    @Test
    public void testInnerClass() {
        ASTCompilationUnit ast = parse("class Foo { class Bar { } }");
        List<ASTClassOrInterfaceDeclaration> classes = ast.findDescendantsOfType(ASTClassOrInterfaceDeclaration.class);
        assertEquals(2, classes.size());

        JavaRuleViolation fooViolation = new JavaRuleViolation(null, new RuleContext(), classes.get(0), null);
        assertEquals("Foo", fooViolation.getClassName());

        JavaRuleViolation barViolation = new JavaRuleViolation(null, new RuleContext(), classes.get(1), null);
        assertEquals("Foo$Bar", barViolation.getClassName());
    }
}
