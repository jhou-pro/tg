package ua.com.fielden.platform.entity.query.generation.elements;

import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;

public class YieldModel {
    private final ISingleOperand operand;
    private final String alias;
    private PropertyPersistenceInfo info;

    public YieldModel(final ISingleOperand operand, final String alias) {
	this(operand, alias, null);
    }

    public YieldModel(final ISingleOperand operand, final String alias, final PropertyPersistenceInfo info) {
	super();
	this.operand = operand;
	this.alias = alias;
	this.info = info;
    }

    @Override
    public String toString() {
	return sql();
    }

    public String sql() {
	return operand.sql() + " AS " + info.getColumn() + "/*" + alias + "*/";
    }

    public ISingleOperand getOperand() {
	return operand;
    }

    public String getAlias() {
	return alias;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((alias == null) ? 0 : alias.hashCode());
	result = prime * result + ((operand == null) ? 0 : operand.hashCode());
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
	if (!(obj instanceof YieldModel)) {
	    return false;
	}
	final YieldModel other = (YieldModel) obj;
	if (alias == null) {
	    if (other.alias != null) {
		return false;
	    }
	} else if (!alias.equals(other.alias)) {
	    return false;
	}
	if (operand == null) {
	    if (other.operand != null) {
		return false;
	    }
	} else if (!operand.equals(other.operand)) {
	    return false;
	}
	return true;
    }

    public PropertyPersistenceInfo getInfo() {
	return info;
    }

    public void setInfo(final PropertyPersistenceInfo info) {
	this.info = info;
    }
}