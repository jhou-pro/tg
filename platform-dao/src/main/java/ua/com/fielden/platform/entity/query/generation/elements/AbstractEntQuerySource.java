package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.determinePropertyType;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

public abstract class AbstractEntQuerySource implements IEntQuerySource {
    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;
    /**
     * List of props implicitly associated with given source (e.g. dot.notation is supported)
     */
    private final List<PropResolutionInfo> referencingProps = new ArrayList<PropResolutionInfo>();

    /**
     * List of props explicitly associated with given source (e.g. each prop should have corresponding physically achievable item (sql column) within given source).
     */
    private final List<PropResolutionInfo> finalReferencingProps = new ArrayList<PropResolutionInfo>();
    /**
     * Sql alias for query source table/query
     */
    protected String sqlAlias;
    /**
     * Map between business name and sql column name.
     */
    protected Map<String, String> sourceColumns = new HashMap<String, String>();

    @Override
    public void assignSqlAlias(final String sqlAlias) {
	this.sqlAlias = sqlAlias;
    }

    public AbstractEntQuerySource(final String alias) {
	this.alias = alias;
    }

    protected boolean isEntityAggregates(final Class type) {
	return EntityAggregates.class.isAssignableFrom(type);
    }

    protected boolean isSyntheticEntity(final Class type) {
	return AbstractEntity.class.isAssignableFrom(type) && IQueryModelProvider.class.isAssignableFrom(type);
    }

    @Override
    public String getAlias() {
	return alias;
    }

    @Override
    public void addReferencingProp(final PropResolutionInfo prop) {
	referencingProps.add(prop);
    }

    @Override
    public void addFinalReferencingProp(final PropResolutionInfo prop) {
	if (!prop.isImplicitId()) {
	    	final Pair<String, String> propStructure = EntityUtils.splitPropByFirstDot(prop.getPropPart());
		if (propStructure.getValue() != null && isPersistedEntityType(determinePropertyType(sourceType(), propStructure.getKey())) && !prop.allExplicit()) {
	    	//if (!prop.allExplicit()) {
		    throw new IllegalStateException("Property [" + prop.entProp + "] incorrectly resolved finally to source [" + this + "] as [" + prop + "]");
		}
	}

	finalReferencingProps.add(prop);
	prop.entProp.setSql(sqlAlias + "." + sourceColumns.get(prop.propPart));
    }

    @Override
    public List<PropResolutionInfo> getReferencingProps() {
	return referencingProps;
    }

    /**
     * If there is alias and property is prepended with this alias, then return property with alias removed, otherwise return null
     * @param dotNotatedPropName
     * @param sourceAlias
     * @return
     */
    protected String dealiasPropName(final String dotNotatedPropName, final String sourceAlias) {
	return (sourceAlias != null && dotNotatedPropName.startsWith(sourceAlias + ".")) ? dotNotatedPropName.substring(sourceAlias.length() + 1) : null;
    }

    abstract Pair<String, Class> lookForProp(final String dotNotatedPropName);

    protected PropResolutionInfo propAsIs(final EntProp prop) {
	final Pair<String, Class> propAsIsSearchResult = lookForProp(prop.getName());
	return propAsIsSearchResult != null/* && alias == null*/ ? new PropResolutionInfo(prop, null, prop.getName(), false, propAsIsSearchResult.getValue(), propAsIsSearchResult.getKey()) : null;
	// this condition will prevent usage of not-aliased properties if their source has alias
    }

    protected PropResolutionInfo propAsAliased(final EntProp prop) {
	final String dealisedProp = dealiasPropName(prop.getName(), getAlias());
	if (dealisedProp == null) {
	    return null;
	} else {
		final Pair<String, Class> propAsAliasedSearchResult = lookForProp(dealisedProp);
		return propAsAliasedSearchResult != null ? new PropResolutionInfo(prop, getAlias(), dealisedProp, false, propAsAliasedSearchResult.getValue(), propAsAliasedSearchResult.getKey()) : null;
	}
    }

    protected PropResolutionInfo propAsImplicitId(final EntProp prop) {
	if (isPersistedEntityType(sourceType())) {
	    return prop.getName().equalsIgnoreCase(getAlias()) ? new PropResolutionInfo(prop, getAlias(), null, true, Long.class, "") : null; // id property is meant here, but is it for all contexts?
	} else {
	    return null;
	}
    }

    @Override
    public Class propType(final String propSimpleName) {
	return determinePropertyType(sourceType(), propSimpleName);
    }

    @Override
    public List<PropResolutionInfo> getFinalReferencingProps() {
	return finalReferencingProps;
    }

    @Override
    public PropResolutionInfo containsProperty(final EntProp prop) {
	// what are the cases/scenarios for one source and one property
	// 1) AsIs
	// 2) AsAliased
	// 3) AsImplicitId
	// possible combinations are
	// o. none
	// a. 1) Vehicle.class as v, prop "model"
	// b. 2) Vehicle.class as v, prop "v.model"
	// c. 3) Vehicle.class as v, prop "v"
	// d. 1) and 2) OrgUnit4.class as parent, prop "parent.parent"  => take 2) - its more explicit
	// e. 1) and 3) OrgUnit4.class as parent, prop "parent"		=> take 1) - favor real prop over implicit id
	// the result formula: if (1 and not 2) then 1
	//			else if (2) then 2
	//			else if (3) then 3
	//			else exception

	final PropResolutionInfo propAsIs = propAsIs(prop);
	final PropResolutionInfo propAsAliased = propAsAliased(prop);
	final PropResolutionInfo propAsImplicitId = propAsImplicitId(prop);

	if (propAsIs == null && propAsAliased == null && propAsImplicitId == null) {
	    return null;
	} else if (propAsIs != null && propAsAliased == null) {
	    return propAsIs;
	} else if (propAsAliased != null) {
	    return propAsAliased;
	} else if (propAsImplicitId != null) {
	    return propAsImplicitId;
	} else {
	    throw new RuntimeException("Unforeseen branch!");
	}
    }

    @Override
    public Map<String, Set<String>> determinePropGroups() {
	final Set<PropResolutionInfo> dotNotatedPropNames = new HashSet<PropResolutionInfo>(getReferencingProps());

	final Map<String, Set<String>> result = new HashMap<String, Set<String>>();
	for (final PropResolutionInfo dotNotatedPropName : dotNotatedPropNames) {
	    final String first = dotNotatedPropName.isImplicitId() ? "id" : dotNotatedPropName.explicitPropPart;
	    final String rest = (dotNotatedPropName.isImplicitId() || dotNotatedPropName.allExplicit()) //
		    ? "" : //
		dotNotatedPropName.propPart.substring(dotNotatedPropName.explicitPropPart.length() + 1);

	    Set<String> propGroup = result.get(first);
	    if (propGroup == null) {
		propGroup = new HashSet<String>();
		result.put(first, propGroup);
	    }
	    if (!StringUtils.isEmpty(rest)) {
		propGroup.add(rest);
	    }
	}

	return result;
    }

    /**
     * Represent data structure to hold information about potential prop resolution against some query source.
     * @author TG Team
     *
     */
    public static class PropResolutionInfo {
	private EntProp entProp;
	private String aliasPart;
	private String explicitPropPart;
	private String propPart;
	private boolean implicitId;
	private Class propType;

	public boolean allExplicit() {
	    return explicitPropPart.equals(propPart);
	}

	@Override
	public String toString() {
	    return "PropResolutionInfo: aliasPart = " + aliasPart + " : propPart = " + propPart + " : impId = " + implicitId + " : type = " + (propType != null ? propType.getSimpleName() : null) + " : explicitPropPart = " + explicitPropPart;
	}

	public PropResolutionInfo(final EntProp entProp, final String aliasPart, final String propPart, final boolean implicitId, final Class propType, final String explicitPropPart) {
	    this.entProp = entProp;
	    if(entProp.getPropType() == null && propType != null) {
		entProp.setPropType(propType);
	    }
	    this.aliasPart = aliasPart;
	    this.propPart = propPart;
	    this.implicitId = implicitId;
	    this.propType = propType;
	    this.explicitPropPart = explicitPropPart;
	}

	public String getAliasPart() {
	    return aliasPart;
	}

	public String getPropPart() {
	    return propPart;
	}

	public boolean isImplicitId() {
	    return implicitId;
	}

	public Class getPropType() {
	    return propType;
	}

	public Integer getPreferenceNumber() {
	    return implicitId ? 2000 : propPart.length();
	}

	public EntProp getEntProp() {
	    return entProp;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((aliasPart == null) ? 0 : aliasPart.hashCode());
	    result = prime * result + ((entProp == null) ? 0 : entProp.hashCode());
	    result = prime * result + ((explicitPropPart == null) ? 0 : explicitPropPart.hashCode());
	    result = prime * result + (implicitId ? 1231 : 1237);
	    result = prime * result + ((propPart == null) ? 0 : propPart.hashCode());
	    result = prime * result + ((propType == null) ? 0 : propType.hashCode());
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
	    if (!(obj instanceof PropResolutionInfo)) {
		return false;
	    }
	    final PropResolutionInfo other = (PropResolutionInfo) obj;
	    if (aliasPart == null) {
		if (other.aliasPart != null) {
		    return false;
		}
	    } else if (!aliasPart.equals(other.aliasPart)) {
		return false;
	    }
	    if (entProp == null) {
		if (other.entProp != null) {
		    return false;
		}
	    } else if (!entProp.equals(other.entProp)) {
		return false;
	    }
	    if (explicitPropPart == null) {
		if (other.explicitPropPart != null) {
		    return false;
		}
	    } else if (!explicitPropPart.equals(other.explicitPropPart)) {
		return false;
	    }
	    if (implicitId != other.implicitId) {
		return false;
	    }
	    if (propPart == null) {
		if (other.propPart != null) {
		    return false;
		}
	    } else if (!propPart.equals(other.propPart)) {
		return false;
	    }
	    if (propType == null) {
		if (other.propType != null) {
		    return false;
		}
	    } else if (!propType.equals(other.propType)) {
		return false;
	    }
	    return true;
	}
    }
}