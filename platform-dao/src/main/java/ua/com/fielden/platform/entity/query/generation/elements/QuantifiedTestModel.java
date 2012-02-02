package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;

public class QuantifiedTestModel implements ICondition {
    private final ISingleOperand leftOperand;
    private final EntQuery rightOperand;
    private final Quantifier quantifier;
    private final ComparisonOperator operator;

    @Override
    public String sql() {
	return leftOperand.sql() + " " + operator + " " + quantifier + " " + rightOperand.sql();
    }

    public QuantifiedTestModel(final ISingleOperand leftOperand, final ComparisonOperator operator, final Quantifier quantifier, final EntQuery rightOperand) {
	this.leftOperand = leftOperand;
	this.rightOperand = rightOperand;
	this.operator = operator;
	this.quantifier = quantifier;
    }

    @Override
    public List<EntProp> getProps() {
	final List<EntProp> result = new ArrayList<EntProp>();
	result.addAll(leftOperand.getProps());
	result.addAll(rightOperand.getProps());
	return result;
    }

    @Override
    public List<EntQuery> getSubqueries() {
	final List<EntQuery> result = new ArrayList<EntQuery>();
	result.addAll(leftOperand.getSubqueries());
	result.addAll(rightOperand.getSubqueries());
	return result;
    }

    @Override
    public List<EntValue> getValues() {
	final List<EntValue> result = new ArrayList<EntValue>();
	result.addAll(leftOperand.getValues());
	result.addAll(rightOperand.getValues());
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
	result = prime * result + ((operator == null) ? 0 : operator.hashCode());
	result = prime * result + ((quantifier == null) ? 0 : quantifier.hashCode());
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
	if (!(obj instanceof QuantifiedTestModel)) {
	    return false;
	}
	final QuantifiedTestModel other = (QuantifiedTestModel) obj;
	if (leftOperand == null) {
	    if (other.leftOperand != null) {
		return false;
	    }
	} else if (!leftOperand.equals(other.leftOperand)) {
	    return false;
	}
	if (operator != other.operator) {
	    return false;
	}
	if (quantifier != other.quantifier) {
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
}