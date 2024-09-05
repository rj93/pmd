/**
 * BSD-style license; for more info see http://pmd.sourceforge.net/license.html
 */

package net.sourceforge.pmd.lang.java.rule.codestyle;

import net.sourceforge.pmd.lang.java.ast.ASTExpression;
import net.sourceforge.pmd.lang.java.ast.ASTExpressionStatement;
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration;
import net.sourceforge.pmd.lang.java.ast.ASTMethodCall;
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator;
import net.sourceforge.pmd.lang.java.ast.JavaNode;
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule;
import net.sourceforge.pmd.lang.java.types.JMethodSig;
import net.sourceforge.pmd.lang.java.types.JTypeMirror;
import net.sourceforge.pmd.lang.java.types.OverloadSelectionResult;
import net.sourceforge.pmd.lang.java.types.TypeTestUtil;
import net.sourceforge.pmd.lang.rule.RuleTargetSelector;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.stream.BaseStream;

public class StreamOperationsRule extends AbstractJavaRule {

    @Override
    protected @NonNull RuleTargetSelector buildTargetSelector() {
        return RuleTargetSelector.forTypes(ASTExpressionStatement.class, ASTLocalVariableDeclaration.class);
    }

    @Override
    public Object visit(ASTExpressionStatement node, Object data) {
        ASTExpression expr = node.getExpr();
        if (expr instanceof ASTMethodCall) {
            ASTMethodCall call = (ASTMethodCall) expr;
            process(node, data, call);
        }

        return data;
    }

    @Override
    public Object visit(ASTLocalVariableDeclaration node, Object data) {
        for (ASTVariableDeclarator variable : node.children(ASTVariableDeclarator.class)) {
            for (ASTMethodCall call : variable.children(ASTMethodCall.class)) {
                process(node, data, call);
            }
        }
        return data;
    }

    private void process(JavaNode node, Object data, ASTMethodCall call) {
        if (isStreamAndUsesStreamMethod(call)) {
            asCtx(data).addViolation(node);
        }
    }

    private boolean isStreamAndUsesStreamMethod(ASTMethodCall call) {
        return call.getNumChildren() > 0
                && isStreamReturnType(call.getChild(0)) // TODO what if empty?
                && checkLines(call);
    }

    // TODO source operation on new lines property - enabled, disabled, ignored
    // TODO empty lines property - boolean
    private boolean checkLines(JavaNode node) {
        if (node == null || node.getNumChildren() == 0) {
            return false;
        }

        int numChildren = node.getNumChildren();
        JavaNode previousSibling = null;
        for (int i = 0; i < numChildren; i++) {
            JavaNode currentSibling = node.getChild(i);
            if (currentSibling instanceof ASTMethodCall || previousSibling instanceof ASTMethodCall) {
                if (checkSiblings(previousSibling, currentSibling) || checkLines(currentSibling)) {
                    return true;
                }
            }
            previousSibling = currentSibling;
        }
        return false;
    }

    private static boolean checkSiblings(JavaNode previousSibling, JavaNode currentSibling) {
        return previousSibling != null && currentSibling.getBeginLine() <= previousSibling.getEndLine();
    }

    private boolean isStreamReturnType(JavaNode node) {
        if (node instanceof ASTMethodCall) {
            ASTMethodCall call = (ASTMethodCall) node;
            return isStream(call.getOverloadSelectionInfo());
        }
        return false;
    }

    private boolean isStream(OverloadSelectionResult result) {
        if (result.isFailed()) {
            return false;
        }

        JMethodSig methodType = result.getMethodType();
        JTypeMirror returnType = methodType.getReturnType();
        return TypeTestUtil.isA(BaseStream.class, returnType);
//        JExecutableSymbol symbol = methodType.getSymbol();
//        return TypeTestUtil.isExactlyA(BaseStream.class, symbol.getEnclosingClass());
    }

}
