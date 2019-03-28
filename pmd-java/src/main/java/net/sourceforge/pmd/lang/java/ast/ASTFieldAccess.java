/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */
/* Generated By:JJTree: Do not edit this line. ASTNullLiteral.java */

package net.sourceforge.pmd.lang.java.ast;

import javax.annotation.Nullable;

import net.sourceforge.pmd.lang.ast.Node;

/**
 * A field access expression.
 *
 * <pre class="grammar">
 *
 * FieldAccess ::=  {@link ASTPrimaryExpression PrimaryExpression} "." &lt;IDENTIFIER&gt;
 *               |  {@link ASTClassOrInterfaceType TypeName} "." &lt;IDENTIFIER&gt;
 *               |  {@link ASTAmbiguousName AmbiguousName} "." &lt;IDENTIFIER&gt;
 * </pre>
 */
public final class ASTFieldAccess extends AbstractJavaTypeNode implements ASTPrimaryExpression, ASTQualifiableExpression, LeftRecursiveNode {
    ASTFieldAccess(int id) {
        super(id);
    }


    ASTFieldAccess(JavaParser p, int id) {
        super(p, id);
    }


    /**
     * Promotes an ambiguous name to the LHS of this node.
     */
    ASTFieldAccess(ASTAmbiguousName lhs, String fieldName) {
        super(JavaParserTreeConstants.JJTFIELDACCESS);
        this.jjtAddChild(lhs, 0);
        this.setImage(fieldName);
    }

    /**
     * Returns the type to the left of the "." if it exists.
     * That may be an {@linkplain ASTAmbiguousName ambiguous name}.
     * May return empty if this call is not qualified (no "."),
     * or if the qualifier is an expression instead of a type.
     */
    @Nullable
    public ASTClassOrInterfaceType getLhsType() {
        return getChildAs(0, ASTClassOrInterfaceType.class);
    }


    /**
     * Returns the name of the field.
     */
    public String getFieldName() {
        return getImage();
    }


    @Override
    public Object jjtAccept(JavaParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }


    @Override
    public <T> void jjtAccept(SideEffectingVisitor<T> visitor, T data) {
        visitor.visit(this, data);
    }
}
