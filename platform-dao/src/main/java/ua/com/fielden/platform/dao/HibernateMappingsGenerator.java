	package ua.com.fielden.platform.dao;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.MapTo;
import static ua.com.fielden.platform.dao.DomainPersistenceMetadata.specialProps;

/**
 * Generates hibernate class mappings from MapTo annotations on domain entity types.
 *
 * @author TG Team
 *
 */
public class HibernateMappingsGenerator {
    private final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> hibTypeInfosMap;

    public HibernateMappingsGenerator(final Map<Class<? extends AbstractEntity<?>>, EntityMetadata> hibTypeInfosMap) {
	this.hibTypeInfosMap = hibTypeInfosMap;
    }

    public String generateMappings() {
	final StringBuffer sb = new StringBuffer();
	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
	sb.append("<!DOCTYPE hibernate-mapping PUBLIC\n");
	sb.append("\"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n");
	sb.append("\"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n");
	sb.append("<hibernate-mapping default-access=\"field\">\n");

	for (final Map.Entry<Class<? extends AbstractEntity<?>>, EntityMetadata> entityTypeEntry : hibTypeInfosMap.entrySet()) {
	    try {
		sb.append(getClassMapping(entityTypeEntry.getValue()));
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new RuntimeException("Couldn't generate mapping for " + entityTypeEntry.getKey().getName() + " due to: " + e.getMessage());
	    }
	    sb.append("\n");
	}
	sb.append("</hibernate-mapping>");

	final String result = sb.toString();
	System.out.println(result);
	return result;
    }

    private String getCommonEntityId(final String name, final PropertyColumn column, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<id name=\"" + name + "\" column=\"" + column.getName() + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
	sb.append("\t\t<generator class=\"hilo\">\n");
	sb.append("\t\t\t<param name=\"table\">UNIQUE_ID</param>\n");
	sb.append("\t\t\t<param name=\"column\">NEXT_VALUE</param>\n");
	sb.append("\t\t\t<param name=\"max_lo\">0</param>\n");
	sb.append("\t\t</generator>\n");
	sb.append("\t</id>\n");
	return sb.toString();
    }

    private String getOneToOneEntityId(final String name, final PropertyColumn column, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<id name=\"" + name + "\" column=\"" + column.getName() + "\" type=\"" + hibTypeName + "\" access=\"property\">\n");
	sb.append("\t\t<generator class=\"foreign\">\n");
	sb.append("\t\t\t<param name=\"property\">key</param>\n");
	sb.append("\t\t</generator>\n");
	sb.append("\t</id>\n");

	return sb.toString();
    }

    private String getCommonEntityVersion(final String name, final PropertyColumn column, final String hibTypeName) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<version name=\"" + name + "\" type=\"" + hibTypeName + "\" access=\"field\" insert=\"false\">\n");
	sb.append("\t\t<column name=\"" + column.getName() + "\" default=\"0\" />\n");
	sb.append("\t</version>\n");
	return sb.toString();
    }

    private String getManyToOneProperty(final String propName, final PropertyColumn propColumn, final Class entityType) {
	final StringBuffer sb = new StringBuffer();
	sb.append("\t<many-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" column=\"" + propColumn.getName() + "\"");
	sb.append("/>\n");
	return sb.toString();
    }

    private String getOneToOneProperty(final String propName, final Class entityType) {
	return "\t<one-to-one name=\"" + propName + "\" class=\"" + entityType.getName() + "\" constrained=\"true\"/>\n";
    }


    private String getUnionEntityProperty(final PropertyMetadata info) {
	    final StringBuffer sb = new StringBuffer();
	    sb.append("\t<component name=\"" + info.getName() + "\" class=\"" + info.getJavaType().getName() + "\">\n");
	    final List<Field> propsFields = AbstractUnionEntity.unionProperties(info.getJavaType());
	    for (final Field subpropField : propsFields) {
		final MapTo mapTo = subpropField.getAnnotation(MapTo.class);
		if (mapTo == null) {
		    throw new IllegalStateException("Property [" + subpropField.getName() + "] in union entity type [" + info.getJavaType() + "] is not annotated  no MapTo ");
		}
		final String column = info.getColumn().getName() + "_" + (StringUtils.isEmpty(mapTo.value()) ? subpropField.getName() : mapTo.value());
		sb.append("\t\t<many-to-one name=\"" + subpropField.getName() +  "\" class=\"" + subpropField.getType().getName() + "\" column = \"" + column.toUpperCase() + "\"/>\n");
	    }
	    sb.append("\t</component>\n");
	    return sb.toString();
    }

    private String getPropMappingString(final String propName, final List<PropertyColumn> propColumns, final String hibTypeName) {
	final String propNameClause = "\t<property name=\"" + propName + "\"";
	final String typeClause = hibTypeName == null ? "" : " type=\"" + hibTypeName + "\"";
	final String endClause = "/>\n";
	if (propColumns.size() == 1) {
	    final PropertyColumn column = propColumns.get(0);
	    final String columnClause = " column=\"" + column.getName() + "\"";
	    final String lengthClause = column.getLength() == null ? "" : " length=\"" + column.getLength() + "\"";
	    final String precisionClause = column.getPrecision() == null ? "" : " precision=\"" + column.getPrecision() + "\"";
	    final String scaleClause = column.getScale() == null ? "" : " scale=\"" + column.getScale() + "\"";
	    return propNameClause + columnClause + typeClause + lengthClause + precisionClause + scaleClause + endClause;
	} else {
	    final StringBuffer sb = new StringBuffer();
	    sb.append(propNameClause + typeClause + ">\n");
	    for (final PropertyColumn column : propColumns) {
		sb.append("\t\t<column name=\"" + column.getName() + "\"" + endClause);
	    }
	    sb.append("\t</property>\n");
	    return sb.toString();
	}
    }

    /**
     * Generates mapping for entity type.
     *
     * @param entityType
     * @return
     * @throws Exception
     */
    private String getClassMapping(final EntityMetadata map) throws Exception {
	final StringBuffer sb = new StringBuffer();
	sb.append("<class name=\"" + map.getType().getName() + "\" table=\"" + map.getTable() + "\">\n");

	sb.append(getCommonPropMappingString(map.getProps().get(AbstractEntity.ID)));
	sb.append(getCommonPropMappingString(map.getProps().get(AbstractEntity.VERSION)));

	final PropertyMetadata keyProp = map.getProps().get(AbstractEntity.KEY);
	if (!keyProp.isVirtual()) {
	    sb.append(getCommonPropMappingString(keyProp));
	}

	for (final PropertyMetadata ppi : map.getProps().values()) {
	    if (ppi.affectsMapping() && !specialProps.contains(ppi.getName())) {
		    sb.append(getCommonPropMappingString(ppi));
	    }
	}
	sb.append("</class>\n");
	return sb.toString();
    }


    /**
     * Generates mapping string for common property based on it persistence info.
     * @param info
     * @return
     * @throws Exception
     */
    private String getCommonPropMappingString(final PropertyMetadata info) throws Exception {
	switch (info.getType()) {
	case UNION_ENTITY:
	    return getUnionEntityProperty(info);
	case ENTITY_KEY:
	    return getOneToOneProperty(info.getName(), info.getJavaType());
	case ENTITY:
	case ENTITY_MEMBER_OF_COMPOSITE_KEY:
	    return getManyToOneProperty(info.getName(), info.getColumn(), info.getJavaType());
	case VERSION:
	    return getCommonEntityVersion(info.getName(), info.getColumn(), info.getTypeString());
	case ID:
	    return getCommonEntityId(info.getName(), info.getColumn(), info.getTypeString());
	case ONE2ONE_ID:
	    return getOneToOneEntityId(info.getName(), info.getColumn(), info.getTypeString());
	default:
	    return getPropMappingString(info.getName(), info.getColumns(), info.getTypeString());
	}
    }
}