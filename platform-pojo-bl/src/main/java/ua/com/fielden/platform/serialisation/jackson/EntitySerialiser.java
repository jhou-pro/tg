package ua.com.fielden.platform.serialisation.jackson;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.UpperCase;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.serialisation.jackson.deserialisers.EntityJsonDeserialiser;
import ua.com.fielden.platform.serialisation.jackson.serialisers.EntityJsonSerialiser;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.esotericsoftware.kryo.Context;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Serialises / deserialises descendants of {@link AbstractEntity}.
 *
 * @author TG Team
 *
 */
public class EntitySerialiser<T extends AbstractEntity<?>> {
    public static final String ENTITY_JACKSON_REFERENCES = "entity-references";
    private final Class<T> type;
    private final EntityJsonSerialiser<T> serialiser;
    private final EntityJsonDeserialiser<T> deserialiser;
    private final List<CachedProperty> properties;
    private final TgJacksonModule module;
    private final EntityFactory factory;
    private final EntityType entityType;
    private final DefaultValueContract defaultValueContract;

    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory) {
        this(type, module, mapper, factory, false);
    }

    public EntitySerialiser(final Class<T> type, final TgJacksonModule module, final ObjectMapper mapper, final EntityFactory factory, final boolean excludeNulls) {
        this.type = type;
        this.factory = factory;
        // cache all properties annotated with @IsProperty
        properties = createCachedProperties(type);
        entityType = this.factory.newEntity(EntityType.class, 1L); // use id to have not dirty properties (reduce the amount of serialised JSON)
        entityType.beginInitialising();
        entityType.setKey(type.getName());

        defaultValueContract = new DefaultValueContract();
        serialiser = new EntityJsonSerialiser<T>(type, properties, entityType, defaultValueContract, excludeNulls);
        deserialiser = new EntityJsonDeserialiser<T>(mapper, factory, type, properties, entityType, defaultValueContract);
        this.module = module;
    }

    public EntityType register() {
        // register serialiser and deserialiser
        module.addSerializer(type, serialiser);
        module.addDeserializer(type, deserialiser);

        if (EntityUtils.isCompositeEntity(type)) {
            final List<String> compositeKeyNames = new ArrayList<>();
            final List<Field> keyMembers = Finder.getKeyMembers(type);
            for (final Field keyMember : keyMembers) {
                compositeKeyNames.add(keyMember.getName());
            }
            entityType.set_compositeKeyNames(compositeKeyNames);

            final String compositeKeySeparator = Reflector.getKeyMemberSeparator((Class<? extends AbstractEntity<DynamicEntityKey>>) type);
            if (!defaultValueContract.isCompositeKeySeparatorDefault(compositeKeySeparator)) {
                entityType.set_compositeKeySeparator(compositeKeySeparator);
            }
        }
        final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(type);
        if (!defaultValueContract.isEntityTitleDefault(type, entityTitleAndDesc.getKey())) {
            entityType.set_entityTitle(entityTitleAndDesc.getKey());
        }
        if (!defaultValueContract.isEntityDescDefault(type, entityTitleAndDesc.getValue())) {
            entityType.set_entityDesc(entityTitleAndDesc.getValue());
        }

        if (!properties.isEmpty()) {
            final Map<String, EntityTypeProp> props = new LinkedHashMap<>();
            for (final CachedProperty prop : properties) {
                // non-composite keys should be persisted by identifying their actual type
                final String name = prop.field().getName();
                final EntityTypeProp entityTypeProp = this.factory.newEntity(EntityTypeProp.class, 1L); // use id to have not dirty properties (reduce the amount of serialised JSON);
                entityTypeProp.beginInitialising();

                final Boolean secrete = AnnotationReflector.isSecreteProperty(type, name);
                if (!defaultValueContract.isSecreteDefault(secrete)) {
                    entityTypeProp.set_secrete(secrete);
                }
                final Boolean upperCase = AnnotationReflector.isAnnotationPresent(prop.field, UpperCase.class);
                if (!defaultValueContract.isUpperCaseDefault(upperCase)) {
                    entityTypeProp.set_upperCase(upperCase);
                }
                final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc(name, type);
                entityTypeProp.set_title(titleAndDesc.getKey());
                entityTypeProp.set_desc(titleAndDesc.getValue());
                entityTypeProp.endInitialising();

                props.put(name, entityTypeProp);
            }
            entityType.set_props(props);
        }

        return entityType;
    }

    public static <M extends AbstractEntity<?>> String newSerialisationId(final M entity, final References references, final Long typeNumber) {
        return typeId(typeNumber) + "#" + newIdWithinTheType(entity, references);
    }

    private static <M extends AbstractEntity<?>> String typeId(final Long typeNumber) {
        return typeNumber.toString();
    }

    private static <M extends AbstractEntity<?>> String newIdWithinTheType(final M entity, final References references) {
        final Long newId = references.addNewId(entity.getType());
        return newId.toString();
    }

    static private ThreadLocal<JacksonContext> contextThreadLocal = new ThreadLocal<JacksonContext>() {
        @Override
        protected JacksonContext initialValue() {
            return new JacksonContext();
        }
    };

    public static <T extends AbstractEntity<?>> List<CachedProperty> createCachedProperties(final Class<T> type) {
        final boolean hasCompositeKey = EntityUtils.isCompositeEntity(type);
        final List<CachedProperty> properties = new ArrayList<CachedProperty>();
        for (final Field propertyField : Finder.findRealProperties(type)) {
            // take into account only persistent properties
            //if (!propertyField.isAnnotationPresent(Calculated.class)) {
            propertyField.setAccessible(true);
            // need to handle property key in a special way -- composite key does not have to be serialised
            if (AbstractEntity.KEY.equals(propertyField.getName())) {
                if (!hasCompositeKey) {
                    final CachedProperty prop = new CachedProperty(propertyField);
                    properties.add(prop);
                    final Class<?> fieldType = AnnotationReflector.getKeyType(type);
                    final int modifiers = fieldType.getModifiers();
                    if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                        prop.setPropertyType(fieldType);
                    }
                }
            } else {
                final CachedProperty prop = new CachedProperty(propertyField);
                properties.add(prop);
                final Class<?> fieldType = PropertyTypeDeterminator.stripIfNeeded(propertyField.getType());
                final int modifiers = fieldType.getModifiers();
                if (!Modifier.isAbstract(modifiers) && !Modifier.isInterface(modifiers)) {
                    prop.setPropertyType(fieldType);
                }
            }
            //}
        }
        return Collections.unmodifiableList(properties);
    }

    /**
     * A convenient class to store property related information.
     *
     * @author TG Team
     *
     */
    public final static class CachedProperty {
        private final Field field;
        private Class<?> propertyType;

        CachedProperty(final Field field) {
            this.field = field;
        }

        public Class<?> getPropertyType() {
            return propertyType;
        }

        public void setPropertyType(final Class<?> type) {
            this.propertyType = type;
        }

        public Field field() {
            return field;
        }
    }

    /**
     * Returns the thread local context for serialization and deserialization.
     *
     * @see Context
     */
    static public JacksonContext getContext() {
        return contextThreadLocal.get();
    }
}
