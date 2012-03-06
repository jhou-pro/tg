package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.Collections;
import java.util.List;

import ua.com.fielden.platform.dao.MappingsGenerator;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public class EntQuerySourceFromEntityType extends AbstractEntQuerySource {
    private final Class<? extends AbstractEntity> entityType;
    private final boolean generated;

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final MappingsGenerator mappingsGenerator) {
    	this(entityType, alias, false, mappingsGenerator);
    }

    public EntQuerySourceFromEntityType(final Class<? extends AbstractEntity> entityType, final String alias, final boolean generated, final MappingsGenerator mappingsGenerator) {
	super(alias, mappingsGenerator);
	this.entityType = entityType;
	this.generated = generated;
    }

    @Override
    public void populateSourceItems(final boolean parentLeftJoinLegacy) {
	for (final PropertyPersistenceInfo ppi : getMappingsGenerator().getEntityPPIs(sourceType())) {
		// if parent nullability = false then take the one from ppi, else true
	    sourceItems.put(ppi.getName(), new PropertyPersistenceInfo.Builder(ppi.getName(), ppi.getJavaType(), ppi.isNullable() || parentLeftJoinLegacy). //
		    hibType(ppi.getHibType()).column(ppi.getColumn()).build());
	}
    }

    @Override
    protected Pair<PurePropInfo, PurePropInfo> lookForProp(final String dotNotatedPropName) {
	final PropertyPersistenceInfo finalPropInfo = getMappingsGenerator().getPropPersistenceInfoExplicitly(entityType, dotNotatedPropName);

	if (finalPropInfo != null) {
	    final boolean finalPropNullability = getMappingsGenerator().isNullable(entityType, dotNotatedPropName);
	    final PurePropInfo ppi = new PurePropInfo(finalPropInfo.getName(), finalPropInfo.getJavaType(), finalPropInfo.getHibType(), finalPropNullability || isNullable());
	    return new Pair<PurePropInfo, PurePropInfo>(ppi, ppi);
	} else {
	    final PropertyPersistenceInfo propInfo = getMappingsGenerator().getInfoForDotNotatedProp(entityType, dotNotatedPropName);
	    if (propInfo == null) {
		return null;
	    } else {
		final boolean propNullability = getMappingsGenerator().isNullable(entityType, dotNotatedPropName);
		final PropertyPersistenceInfo explicitPartPropInfo = getMappingsGenerator().getPropPersistenceInfoExplicitly(entityType, EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey());
		final boolean explicitPropNullability = getMappingsGenerator().isNullable(entityType, EntityUtils.splitPropByFirstDot(dotNotatedPropName).getKey());
		return new Pair<PurePropInfo, PurePropInfo>( //
		new PurePropInfo(explicitPartPropInfo.getName(), explicitPartPropInfo.getJavaType(), explicitPartPropInfo.getHibType(), explicitPropNullability || isNullable()), //
		new PurePropInfo(dotNotatedPropName, propInfo.getJavaType(), propInfo.getHibType(), propNullability || isNullable()));
	    }
	}
    }

    @Override
    public boolean generated() {
	return generated;
    }

    @Override
    public Class sourceType() {
	return entityType;
    }

    @Override
    public String sql() {
	return getMappingsGenerator().getTableClause(sourceType()) + " AS " + sqlAlias + "/*" + (alias == null ? " " : alias) + "*/";
    }

    @Override
    public List<EntValue> getValues() {
	return Collections.emptyList();
    }

    @Override
    public String toString() {
        return entityType.getSimpleName() + "-table AS " + getAlias() + " /*" + (generated ? " GEN " : "") + "*/";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((getAlias() == null) ? 0 : getAlias().hashCode());
	result = prime * result + ((entityType == null) ? 0 : entityType.hashCode());
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
	if (!(obj instanceof EntQuerySourceFromEntityType)) {
	    return false;
	}
	final EntQuerySourceFromEntityType other = (EntQuerySourceFromEntityType) obj;
	if (getAlias() == null) {
	    if (other.getAlias() != null) {
		return false;
	    }
	} else if (!getAlias().equals(other.getAlias())) {
	    return false;
	}
	if (entityType == null) {
	    if (other.entityType != null) {
		return false;
	    }
	} else if (!entityType.equals(other.entityType)) {
	    return false;
	}
	return true;
    }
}