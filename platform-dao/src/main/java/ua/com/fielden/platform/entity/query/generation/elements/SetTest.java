package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

public class SetTest extends AbstractCondition {
    private final ISingleOperand leftOperand;
    private final ISetOperand rightOperand;
    private final boolean negated;

    @Override
    public String sql() {
        return leftOperand.sql() + (negated ? " NOT IN " : " IN ") + rightOperand.sql();
    }

    public SetTest(final ISingleOperand leftOperand, final boolean negated, final ISetOperand rightOperand) {
        this.leftOperand = leftOperand;
        this.rightOperand = rightOperand;
        this.negated = negated;
    }

    @Override
    public List<EntQuery> getLocalSubQueries() {
        final List<EntQuery> result = new ArrayList<EntQuery>();
        result.addAll(leftOperand.getLocalSubQueries());
        result.addAll(rightOperand.getLocalSubQueries());
        return result;
    }

    @Override
    public List<EntProp> getLocalProps() {
        final List<EntProp> result = new ArrayList<EntProp>();
        result.addAll(leftOperand.getLocalProps());
        result.addAll(rightOperand.getLocalProps());
        return result;
    }

    @Override
    public List<EntValue> getAllValues() {
        final List<EntValue> result = new ArrayList<EntValue>();
        result.addAll(leftOperand.getAllValues());
        result.addAll(rightOperand.getAllValues());
        return result;
    }

    @Override
    public boolean ignore() {
        return leftOperand.ignore();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((leftOperand == null) ? 0 : leftOperand.hashCode());
        result = prime * result + (negated ? 1231 : 1237);
        result = prime * result + ((rightOperand == null) ? 0 : rightOperand.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof SetTest)) {
            return false;
        }
        final SetTest other = (SetTest) obj;
        if (leftOperand == null) {
            if (other.leftOperand != null) {
                return false;
            }
        } else if (!leftOperand.equals(other.leftOperand)) {
            return false;
        }
        if (negated != other.negated) {
            return false;
        }
        if (rightOperand == null) {
            if (other.rightOperand != null) {
                return false;
            }
        } else if (!rightOperand.equals(other.rightOperand)) {
            return false;
        }
        return true;
    }

    @Override
    protected List<IPropertyCollector> getCollection() {
        return new ArrayList<IPropertyCollector>() {
            {
                add(leftOperand);
                add(rightOperand);
            }
        };
    }
}