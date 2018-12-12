package net.dloud.platform.maven;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


/**
 * @author QuDasheng
 * @create 2018-09-03 20:24
 **/
public class ValueVisitor extends ASTVisitor {
    private String baseSplit = ",";

    private boolean isArray = false;

    private List<Object> value = new ArrayList<>();

    @Override
    public boolean visit(NumberLiteral node) {
        value.add(node.getToken());
        return true;
    }

    @Override
    public boolean visit(BooleanLiteral node) {
        value.add(node.booleanValue());
        return true;
    }

    @Override
    public boolean visit(CharacterLiteral node) {
        value.add(node.charValue());
        return true;
    }

    @Override
    public boolean visit(StringLiteral node) {
        final String str = node.getLiteralValue();
        if (str.contains(baseSplit)) {
            isArray = true;
            for (String one : str.split(baseSplit)) {
                if (null != one) {
                    value.add(one.trim());
                }
            }
        } else {
            value.add(str.trim());
        }
        return true;
    }

    @Override
    public boolean visit(NullLiteral node) {
        value = null;
        return true;
    }

    @Override
    public boolean visit(ArrayInitializer node) {
        isArray = true;
        for (Object one : node.expressions()) {
            if (one instanceof Expression) {
                final Expression expression = (Expression) one;
                expression.accept(this);
            }
        }
        return true;
    }

    /**
     * null表示未匹配到
     */
    public Object getValue() {
        if (null == value) {
            return null;
        }

        if (isArray) {
            return new HashSet<>(value);
        } else {
            if (value.isEmpty()) {
                return null;
            } else {
                return value.get(0);
            }
        }
    }
}
