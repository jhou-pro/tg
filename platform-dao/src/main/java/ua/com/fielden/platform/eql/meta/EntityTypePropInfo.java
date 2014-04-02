package ua.com.fielden.platform.eql.meta;

import ua.com.fielden.platform.eql.s1.elements.Expression1;

public class EntityTypePropInfo extends AbstractPropInfo {
    private final EntityInfo propEntityInfo;

    @Override
    public String toString() {
        return super.toString() + ": " + propEntityInfo.javaType().getSimpleName();
    }

    public EntityTypePropInfo(final String name, final EntityInfo parent, final EntityInfo propEntityInfo, final Expression1 expression) {
        super(name, parent, expression, true);
        this.propEntityInfo = propEntityInfo;
    }

    protected EntityInfo getPropEntityInfo() {
        return propEntityInfo;
    }

    @Override
    public AbstractPropInfo resolve(final String dotNotatedSubPropName) {
        return dotNotatedSubPropName != null ? getPropEntityInfo().resolve(dotNotatedSubPropName) : this;
    }

    @Override
    public Class javaType() {
        return propEntityInfo.javaType();
    }
}