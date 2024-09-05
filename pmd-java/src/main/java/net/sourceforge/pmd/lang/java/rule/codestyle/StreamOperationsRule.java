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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.BaseStream;

public class StreamOperationsRule extends AbstractJavaRule {

    // TODO get stream methods
    private static final Set<String> STREAM_METHOD_NAMES = new HashSet<>(Arrays.asList(
            "stream",
            "flatMap",
            "map",
            "collect"
    ));

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

    private boolean isStreamAndUsesStreamMethod(@Nullable ASTMethodCall call) {
        return STREAM_METHOD_NAMES.contains(call.getMethodName())
                && isStreamReturnType(call.getChild(0)) // TODO what if empty?
                && checkLines(call);
    }

    // TODO source operation on new lines property - enabled, disabled, ignored
    // TODO empty lines property - boolean
    private boolean checkLines(JavaNode node) {
        int numChildren = node.getNumChildren();
        JavaNode previousSibling = node.getChild(0); // TODO What if empty?
        for (int i = 1; i < numChildren; i++) {
            JavaNode currentSibling = node.getChild(i);
            if (currentSibling.getBeginLine() <= previousSibling.getEndLine()) {
                return true;
            }
            previousSibling = currentSibling;
        }
        return false;
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
