package ua.com.fielden.platform.criteria.generator.impl;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.is;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.not;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.criteria.enhanced.CentreEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.LocatorEntityQueryCriteriaToEnhance;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.criteria.generator.ICriteriaGenerator;
import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.IAddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.factory.AfterChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.BeforeChangeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CritOnlyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.CriteriaPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.EntityTypeAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.FirstParamAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.HandlerAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.ParamAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.RequiredAnnotation;
import ua.com.fielden.platform.entity.annotation.factory.SecondParamAnnotation;
import ua.com.fielden.platform.entity.annotation.mutator.ClassParam;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.validation.EntityExistsValidator;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedLocatorEntityQueryCriteria;
import ua.com.fielden.platform.entity_centre.review.criteria.EntityQueryCriteria;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Inject;

/**
 * The implementation of the {@link ICriteriaGenerator} that generates {@link EntityQueryCriteria} with criteria properties.
 *
 * @author TG Team
 *
 */
public class CriteriaGenerator implements ICriteriaGenerator {

    private static final Logger logger = Logger.getLogger(CriteriaGenerator.class);

    private final EntityFactory entityFactory;

    private final ICompanionObjectFinder coFinder;

    private final Map<Class<?>, Class<?>> generatedClasses;

    @Inject
    public CriteriaGenerator(final EntityFactory entityFactory, final ICompanionObjectFinder controllerProvider) {
        this.entityFactory = entityFactory;
        this.coFinder = controllerProvider;
        this.generatedClasses = new WeakHashMap<>();
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final Annotation... customAnnotations) {
        return (EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>>) generateQueryCriteria(root, cdtme, CentreEntityQueryCriteriaToEnhance.class, null, customAnnotations);
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>> generateLocatorQueryCriteria(final Class<T> root, final ILocatorDomainTreeManagerAndEnhancer ldtme, final Annotation... customAnnotations) {
        return (EnhancedLocatorEntityQueryCriteria<T, IEntityDao<T>>) generateQueryCriteria(root, ldtme, LocatorEntityQueryCriteriaToEnhance.class, null, customAnnotations);
    }

    @Override
    public <T extends AbstractEntity<?>> EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>> generateCentreQueryCriteria(final Class<T> root, final ICentreDomainTreeManagerAndEnhancer cdtme, final Class<?> miType, final Annotation... customAnnotations) {
        return (EnhancedCentreEntityQueryCriteria<T, IEntityDao<T>>) generateQueryCriteria(root, cdtme, CentreEntityQueryCriteriaToEnhance.class, miType, customAnnotations);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> EntityQueryCriteria<CDTME, T, IEntityDao<T>> generateQueryCriteria(final Class<T> root, final CDTME cdtme, final Class<? extends EntityQueryCriteria> entityClass, final Class<?> miType, final Annotation... customAnnotations) {
        try {
            final Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>> queryCriteriaClass;

            if (generatedClasses.containsKey(miType)) {
                queryCriteriaClass = (Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) generatedClasses.get(miType);
            } else {
                final List<NewProperty> newProperties = new ArrayList<NewProperty>();
                for (final String propertyName : cdtme.getFirstTick().checkedProperties(root)) {
                    if (!AbstractDomainTree.isPlaceholder(propertyName)) {
                        newProperties.addAll(generateCriteriaProperties(root, cdtme.getEnhancer(), propertyName));
                    }
                }
                final DynamicEntityClassLoader cl = DynamicEntityClassLoader.getInstance(ClassLoader.getSystemClassLoader());

                queryCriteriaClass = (Class<? extends EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) cl.startModification(entityClass.getName()).addClassAnnotations(customAnnotations).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
                generatedClasses.put(miType, queryCriteriaClass);
            }

            final DefaultEntityProducerWithContext<EntityQueryCriteria<CDTME, T, IEntityDao<T>>> entityProducer = new DefaultEntityProducerWithContext<>(entityFactory, (Class<EntityQueryCriteria<CDTME, T, IEntityDao<T>>>) queryCriteriaClass, coFinder);
            entityProducer.newEntity();
            final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity = entityProducer.newEntity(); // entityFactory.newByKey(queryCriteriaClass, "not required");
            entity.beginInitialising();
            entity.setKey("not required");
            entity.endInitialising();

            //Set dao for generated entity query criteria.
            final Field daoField = Finder.findFieldByName(EntityQueryCriteria.class, "dao");
            final boolean isDaoAccessable = daoField.isAccessible();
            daoField.setAccessible(true);
            daoField.set(entity, coFinder.find(root));
            daoField.setAccessible(isDaoAccessable);

            //Set domain tree manager for entity query criteria.
            final Field dtmField = Finder.findFieldByName(EntityQueryCriteria.class, "cdtme");
            final boolean isCdtmeAccessable = dtmField.isAccessible();
            dtmField.setAccessible(true);
            dtmField.set(entity, cdtme);
            dtmField.setAccessible(isCdtmeAccessable);

            //Add change support to the entity query criteria instance
            //in order to synchronise entity query criteria values with model values
            synchroniseWithModel(entity);

            return entity;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

    /**
     * Generates criteria properties for specified list of properties and their root type.
     *
     * @param root
     * @param propertyName
     * @return
     */
    private static List<NewProperty> generateCriteriaProperties(final Class<?> root, final IDomainTreeEnhancer enhancer, final String propertyName) {
        final Class<?> managedType = enhancer.getManagedType(root);
        final boolean isEntityItself = "".equals(propertyName); // empty property means "entity itself"
        final Class<?> propertyType = isEntityItself ? managedType : PropertyTypeDeterminator.determinePropertyType(managedType, propertyName);
        final CritOnly critOnlyAnnotation = isEntityItself ? null : AnnotationReflector.getPropertyAnnotation(CritOnly.class, managedType, propertyName);
        final Pair<String, String> titleAndDesc = CriteriaReflector.getCriteriaTitleAndDesc(managedType, propertyName);
        final List<NewProperty> generatedProperties = new ArrayList<NewProperty>();

        if (AbstractDomainTree.isDoubleCriterionOrBoolean(managedType, propertyName)) {
            generatedProperties.addAll(generateRangeCriteriaProperties(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation));
        } else {
            generatedProperties.add(generateSingleCriteriaProperty(root, managedType, propertyType, propertyName, titleAndDesc, critOnlyAnnotation));
        }
        return generatedProperties;
    }

    /**
     * Generates criteria property with appropriate annotations.
     *
     * @param root
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static NewProperty generateSingleCriteriaProperty(final Class<?> root, final Class<?> managedType, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation) {
        final boolean isEntityItself = "".equals(propertyName);
        final boolean isEntity = EntityUtils.isEntityType(propertyType);
        final boolean isSingle = critOnlyAnnotation != null && Type.SINGLE.equals(critOnlyAnnotation.value());
        final boolean isRequired = isEntityItself ? false : AnnotationReflector.isPropertyAnnotationPresent(Required.class, managedType, propertyName);
        boolean hasEntityExists = false;
        try {
            final Method setter = isEntityItself ? null : Reflector.obtainPropertySetter(managedType, propertyName);
            hasEntityExists = setter == null ? false : AnnotationReflector.isAnnotationPresent(setter, EntityExists.class);
        } catch (final ReflectionException e) {
            // TODO if this is an error -- please handle it appropriately, if not -- please remove rigorous logging
            logger.warn("Couldn't found an setter for property " + propertyName + " on the type " + managedType.getSimpleName());
        }
        final boolean finalHasEntityExists = hasEntityExists;
        final Class<?> newPropertyType = isEntity ? (isSingle ? propertyType : List.class) : (EntityUtils.isBoolean(propertyType) ? Boolean.class : propertyType);

        final List<Annotation> annotations = new ArrayList<Annotation>();
        if (isEntity && !isSingle && EntityUtils.isCollectional(newPropertyType)) {
            annotations.add(new IsPropertyAnnotation(String.class, "--stub-link-property--").newInstance());
            annotations.add(new EntityTypeAnnotation((Class<? extends AbstractEntity<?>>) propertyType).newInstance());
        }
        if (isSingle && isRequired) {
            annotations.add(new RequiredAnnotation().newInstance());
        }
        if (isEntity && isSingle && finalHasEntityExists) {
            annotations.add(new BeforeChangeAnnotation(
                    new Handler[] {
                            new HandlerAnnotation(EntityExistsValidator.class).
                                    non_ordinary(new ClassParam[] { ParamAnnotation.classParam("coFinder", ICompanionObjectFinder.class) }).
                                    clazz(new ClassParam[] { ParamAnnotation.classParam("type", newPropertyType) }).
                                    newInstance() }
                    ).newInstance());
        }
        annotations.add(new CriteriaPropertyAnnotation(managedType, propertyName).newInstance());
        annotations.add(new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());

        final Optional<CritOnlyAnnotation> newCritOnly = generateCritOnlyAnnotation(critOnlyAnnotation, propertyType);
        if (newCritOnly.isPresent()) {
            annotations.add(newCritOnly.get().newInstance());
        }

        return new NewProperty(CriteriaReflector.generateCriteriaPropertyName(root, propertyName), newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(), annotations.toArray(new Annotation[0]));
    }

    /**
     * Generates two criteria properties for range properties (i. e. number, money, date or boolean properties).
     *
     * @param root
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @param critOnlyAnnotation
     * @return
     */
    @SuppressWarnings("serial")
    private static List<NewProperty> generateRangeCriteriaProperties(final Class<?> root, final Class<?> managedType, final Class<?> propertyType, final String propertyName, final Pair<String, String> titleAndDesc, final CritOnly critOnlyAnnotation) {
        //final boolean isEntityItself = "".equals(propertyName);
        final String firstPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, EntityUtils.isBoolean(propertyType) ? is(propertyName) : from(propertyName));
        final String secondPropertyName = CriteriaReflector.generateCriteriaPropertyName(root, EntityUtils.isBoolean(propertyType) ? not(propertyName) : to(propertyName));
        final Class<?> newPropertyType = EntityUtils.isBoolean(propertyType) ? boolean.class : propertyType;
        final Optional<CritOnlyAnnotation> newCritOnly = generateCritOnlyAnnotation(critOnlyAnnotation, propertyType);
        //final boolean isRequired = isEntityItself ? false : AnnotationReflector.isPropertyAnnotationPresent(Required.class, managedType, propertyName);

        final NewProperty firstProperty = new NewProperty(firstPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(),
                new CriteriaPropertyAnnotation(managedType, propertyName).newInstance(), new FirstParamAnnotation(secondPropertyName).newInstance(), new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());
        final NewProperty secondProperty = new NewProperty(secondPropertyName, newPropertyType, false, titleAndDesc.getKey(), titleAndDesc.getValue(),
                new CriteriaPropertyAnnotation(managedType, propertyName).newInstance(), new SecondParamAnnotation(firstPropertyName).newInstance(), new AfterChangeAnnotation(SynchroniseCriteriaWithModelHandler.class).newInstance());

        if (newCritOnly.isPresent()) {
            firstProperty.addAnnotation(newCritOnly.get().newInstance());
            secondProperty.addAnnotation(newCritOnly.get().newInstance());
        }

        return new ArrayList<NewProperty>() {
            {
                add(firstProperty);
                add(secondProperty);
            }
        };
    }

    /**
     * Generates {@link CritOnlyAnnotation} instance for the specified property in the managed type.
     *
     * @param managedType
     * @param propertyType
     * @param propertyName
     * @return
     */
    private static Optional<CritOnlyAnnotation> generateCritOnlyAnnotation(final CritOnly critOnlyAnnotation, final Class<?> propertyType) {
        if (BigDecimal.class.isAssignableFrom(propertyType) && critOnlyAnnotation != null && critOnlyAnnotation.scale() >= 0) {
            return Optional.of(new CritOnlyAnnotation(Type.SINGLE, critOnlyAnnotation.scale(), critOnlyAnnotation.precision()));
        }
        return Optional.empty();
    }

    /**
     * Synchronises entity query criteria property values with domain tree model.
     *
     * @param entity
     */
    private static <T extends AbstractEntity<?>, CDTME extends ICentreDomainTreeManagerAndEnhancer> void synchroniseWithModel(final EntityQueryCriteria<CDTME, T, IEntityDao<T>> entity) {
        final IAddToCriteriaTickManager ftm = entity.getCentreDomainTreeMangerAndEnhancer().getFirstTick();
        for (final Field propertyField : CriteriaReflector.getCriteriaProperties(entity.getType())) {
            final SecondParam secondParam = AnnotationReflector.getAnnotation(propertyField, SecondParam.class);
            final CriteriaProperty critProperty = AnnotationReflector.getAnnotation(propertyField, CriteriaProperty.class);
            final Class<T> root = entity.getEntityClass();
            entity.set(propertyField.getName(), secondParam == null ? ftm.getValue(root, critProperty.propertyName()) : ftm.getValue2(root, critProperty.propertyName()));
        }
    }
}
