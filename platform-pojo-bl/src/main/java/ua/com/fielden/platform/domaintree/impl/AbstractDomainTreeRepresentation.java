package ua.com.fielden.platform.domaintree.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.FunctionUtils;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * A base domain tree representation for all TG trees. Includes strict TG domain rules that should be used by all specific tree implementations. <br><br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeRepresentation extends AbstractDomainTree implements IDomainTreeRepresentation {
    private final EnhancementLinkedRootsSet rootTypes;
    private final EnhancementSet excludedProperties;
    private final ITickRepresentation firstTick;
    private final ITickRepresentation secondTick;
    /** Please do not use this field directly, use {@link #includedPropertiesMutable(Class)} lazy getter instead. */
    private final EnhancementRootsMap<ListenedArrayList> includedProperties;
    private final transient AbstractDomainTreeManager dtm;

    private final transient List<IPropertyListener> propertyListeners, disabledPropertyListeners;

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected AbstractDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final ITickRepresentation firstTick, final ITickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser);
	this.rootTypes = new EnhancementLinkedRootsSet();
	this.rootTypes.addAll(rootTypes);
	this.excludedProperties = createSet();
	this.excludedProperties.addAll(excludedProperties);
	this.firstTick = firstTick;
	this.secondTick = secondTick;

	propertyListeners = new ArrayList<IPropertyListener>();
	disabledPropertyListeners = new ArrayList<IPropertyListener>();

	this.dtm = null; // IMPORTANT : to use this "dtm", this representation should be passed into manager constructor, which should initialise "dtm" field.

	// initialise the references on this instance in its children
	try {
	    final Field dtrField = Finder.findFieldByName(AbstractTickRepresentation.class, "dtr");
	    final boolean isAccessible = dtrField.isAccessible();
	    dtrField.setAccessible(true);
	    dtrField.set(firstTick, this);
	    dtrField.set(secondTick, this);
	    dtrField.setAccessible(isAccessible);
	} catch (final Exception e) {
	    e.printStackTrace();
	    throw new IllegalStateException(e);
	}

	// this field unfortunately should be lazy loaded due to heavy-weight nature (deep, circular tree of properties)
	this.includedProperties = createRootsMap();
	this.includedProperties.putAll(includedProperties);

	for (final Entry<Class<?>, ListenedArrayList> entry : this.includedProperties.entrySet()) {
	    // initialise the references on this instance in "included properties" lists
	    try {
		final Field rootField = Finder.findFieldByName(ListenedArrayList.class, "root");
		final boolean isAccessible = rootField.isAccessible();
		rootField.setAccessible(true);
		rootField.set(entry.getValue(), entry.getKey());
		rootField.setAccessible(isAccessible);
	    } catch (final Exception e) {
		e.printStackTrace();
		throw new IllegalStateException(e);
	    }
	}
    }

    /**
     * Constructs recursively the list of properties using given list of fields.
     *
     * @param rootType
     * @param path
     * @param fieldsAndKeys
     * @return
     */
    private List<String> constructProperties(final Class<?> rootType, final String path, final List<Field> fieldsAndKeys) {
	final List<String> newIncludedProps = new ArrayList<String>();
	for (final Field field : fieldsAndKeys) {
	    final String property = (StringUtils.isEmpty(path)) ? field.getName() : (path + "." + field.getName());
	    final String reflectionProperty = reflectionProperty(property);
	    if (!isExcludedImmutably(rootType, reflectionProperty)) {
		newIncludedProps.add(property);

		// determine the type of property, which can be a) "union entity" property b) property under "union entity" c) collection property d) entity property e) simple property
		final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(rootType, reflectionProperty);
		final Class<?> parentType = penultAndLast.getKey();
		final Class<?> propertyType = PropertyTypeDeterminator.determineClass(parentType, penultAndLast.getValue(), true, true);

		// add the children for "property" based on its nature
		if (EntityUtils.isEntityType(propertyType)) {
		    final boolean propertyTypeWasInHierarchyBefore = typesInHierarchy(rootType, reflectionProperty, true).contains(DynamicEntityClassLoader.getOriginalType(propertyType));
		    final boolean isKeyPart = Finder.getKeyMembers(parentType).contains(field); // indicates if field is the part of the key.
		    if (propertyTypeWasInHierarchyBefore && !isKeyPart) {
			newIncludedProps.add(createDummyMarker(property));
		    } else if (EntityUtils.isUnionEntityType(propertyType)) { // "union entity" property
			final Pair<List<Field>, List<Field>> commonAndUnion = commonAndUnion((Class<? extends AbstractUnionEntity>) propertyType);
			// a new tree branch should be created for "common" properties under "property"
			final String commonBranch = createCommonBranch(property);
			newIncludedProps.add(commonBranch); // final DefaultMutableTreeNode nodeForCommonProperties = addHotNode("common", null, false, klassNode, new Pair<String, String>("Common", TitlesDescsGetter.italic("<b>Common properties</b>")));
			newIncludedProps.addAll(constructProperties(rootType, commonBranch, commonAndUnion.getKey()));
			// "union" properties should be added directly to "property"
			newIncludedProps.addAll(constructProperties(rootType, property, commonAndUnion.getValue()));
		    } else if (EntityUtils.isUnionEntityType(parentType)) { // property under "union entity"
			// the property under "union entity" should have only "non-common" properties added
			final List<Field> propertiesWithoutCommon = constructKeysAndProperties(propertyType);
			final List<String> parentCommonNames = AbstractUnionEntity.commonProperties((Class<? extends AbstractUnionEntity>) parentType);
			propertiesWithoutCommon.removeAll(constructKeysAndProperties(propertyType, parentCommonNames));
			newIncludedProps.addAll(constructProperties(rootType, property, propertiesWithoutCommon));
		    } else { // collectional or non-collectional entity property
			newIncludedProps.addAll(constructProperties(rootType, property, constructKeysAndProperties(propertyType)));
		    }
		}
	    }
	}
	return newIncludedProps;
    }

    /**
     * Determines the lists of common and union fields for concrete union entity type.
     *
     * @param unionClass
     * @return
     */
    private static Pair<List<Field>, List<Field>> commonAndUnion(final Class<? extends AbstractUnionEntity> unionClass) {
	final List<Field> unionProperties = AbstractUnionEntity.unionProperties(unionClass);
	final Class<? extends AbstractEntity> concreteUnionClass = (Class<? extends AbstractEntity>) (unionProperties.get(0).getType());
	final List<String> commonNames = AbstractUnionEntity.commonProperties(unionClass);
	final List<Field> commonProperties = constructKeysAndProperties(concreteUnionClass, commonNames);
	return new Pair<List<Field>, List<Field>>(commonProperties, unionProperties);
    }

    /**
     * Forms a list of fields for "type" in order ["key" or key members => "desc" (if exists) => other properties in order as declared in domain].
     *
     * @param type
     * @return
     */
    private static List<Field> constructKeysAndProperties(final Class<?> type) {
	final List<Field> properties = Finder.findProperties(type);
	properties.remove(Finder.getFieldByName(type, AbstractEntity.KEY));
	properties.remove(Finder.getFieldByName(type, AbstractEntity.DESC));
	final List<Field> keys = Finder.getKeyMembers(type);
	properties.removeAll(keys);

	final List<Field> fieldsAndKeys = new ArrayList<Field>();
	fieldsAndKeys.addAll(keys);
	fieldsAndKeys.add(Finder.getFieldByName(type, AbstractEntity.DESC));
	fieldsAndKeys.addAll(properties);
	return fieldsAndKeys;
    }

    /**
     * Forms a list of fields for "type" in order ["key" or key members => "desc" (if exists) => other properties in order as declared in domain] and chooses only fields with <code>names</code>.
     *
     * @param type
     * @param names
     * @return
     */
    private static List<Field> constructKeysAndProperties(final Class<?> type, final List<String> names) {
	final List<Field> allProperties = constructKeysAndProperties(type);
	final List<Field> properties = new ArrayList<Field>();
	for (final Field f : allProperties) {
	    if (names.contains(f.getName())) {
		properties.add(f);
	    }
	}
	return properties;
    }

    /**
     * Returns <code>true</code> if property is collection itself.
     *
     * @param root
     * @param property
     * @return
     */
    protected static boolean isCollection(final Class<?> root, final String property) {
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	if (isEntityItself) {
	    return false;
	}
	final Pair<Class<?>, String> penultAndLast = PropertyTypeDeterminator.transform(root, property);
	final Class<?> realType = isEntityItself ? null : PropertyTypeDeterminator.determineClass(penultAndLast.getKey(), penultAndLast.getValue(), true, false);
	return (!isEntityItself && realType != null && Collection.class.isAssignableFrom(realType)); // or collections itself
    }

    /**
     * Returns parent collection for specified property.
     *
     * @param root
     * @param property
     * @return
     */
    public static String parentCollection(final Class<?> root, final String property) {
	if (!isCollectionOrInCollectionHierarchy(root, property)) {
	    throw new IllegalArgumentException("The property [" + property + "] is not in collection hierarchy.");
	}
        return isCollection(root, property) ? property : parentCollection(root, PropertyTypeDeterminator.penultAndLast(property).getKey());
    }

    /**
     * Returns <code>true</code> if property is in collectional hierarchy.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isInCollectionHierarchy(final Class<?> root, final String property) {
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	return (!isEntityItself && typesInHierarchy(root, property, false).contains(Collection.class)); // properties in collectional hierarchy
    }

    /**
     * Returns <code>true</code> if property is in collectional hierarchy or is collection itself.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isCollectionOrInCollectionHierarchy(final Class<?> root, final String property) {
	return isCollection(root, property) || isInCollectionHierarchy(root, property);
    }

    @Override
    public boolean isExcludedImmutably(final Class<?> root, final String property) {
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(root, property);
	final Class<?> penultType = transformed.getKey();
	final String lastPropertyName = transformed.getValue();
	final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determineClass(penultType, lastPropertyName, true, true);
	final Field field = isEntityItself ? null : Finder.getFieldByName(penultType, lastPropertyName);

	return 	(excludedProperties.contains(key(root, property))) || // exclude manually excluded properties
		(!isEntityItself && AbstractEntity.KEY.equals(lastPropertyName) && propertyType == null) || // exclude "key" -- no KeyType annotation exists in direct owner of "key"
		(!isEntityItself && AbstractEntity.KEY.equals(lastPropertyName) && !AnnotationReflector.isAnnotationPresent(KeyTitle.class, penultType)) || // exclude "key" -- no KeyTitle annotation exists in direct owner of "key"
		(!isEntityItself && AbstractEntity.KEY.equals(lastPropertyName) && !EntityUtils.isEntityType(propertyType)) || // exclude "key" -- "key" is not of entity type
		(!isEntityItself && AbstractEntity.DESC.equals(lastPropertyName) && !AnnotationReflector.isAnnotationPresent(DescTitle.class, penultType)) || // exclude "desc" -- no DescTitle annotation exists in direct owner of "desc"
		(!isEntityItself && !Finder.findFieldByName(root, property).isAnnotationPresent(IsProperty.class)) || // exclude non-TG properties (not annotated by @IsProperty)
		(isEntityItself && !rootTypes().contains(propertyType)) || // exclude entities of non-"root types"
		(EntityUtils.isEnum(propertyType)) || // exclude enumeration properties / entities
		(EntityUtils.isEntityType(propertyType) && Modifier.isAbstract(propertyType.getModifiers()) || // exclude properties / entities of entity type with 'abstract' modifier
		(EntityUtils.isEntityType(propertyType) && !AnnotationReflector.isAnnotationPresent(KeyType.class, propertyType))) || // exclude properties / entities of entity type without KeyType annotation
		(!isEntityItself && Finder.getKeyMembers(penultType).contains(field) && typesInHierarchy(root, property, true).contains(DynamicEntityClassLoader.getOriginalType(propertyType))) || // exclude key parts which type was in hierarchy
		(!isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(Invisible.class, penultType, lastPropertyName)) || // exclude invisible properties
		(!isEntityItself && AnnotationReflector.isPropertyAnnotationPresent(Ignore.class, penultType, lastPropertyName)) || // exclude invisible properties
		(!isEntityItself && PropertyTypeDeterminator.isDotNotation(property) && AnnotationReflector.isAnnotationPresentInHierarchy(CritOnly.class, root, PropertyTypeDeterminator.penultAndLast(property).getKey())) || // exclude property if it is a child of other AE crit-only property (collection)
		(!isEntityItself && isExcludedImmutably(root, PropertyTypeDeterminator.isDotNotation(property) ? PropertyTypeDeterminator.penultAndLast(property).getKey() : "")); // exclude property if it is an ascender (any level) of already excluded property
    }

    /**
     * Finds a complete set of <b>NOT ENHANCED</b> types in hierarchy of dot-notation expression, excluding the type of last property and including the type of root class.<br><br>
     *
     * E.g. : "WorkOrder$$1.vehicle.fuelUsages.vehicle.fuelCards.initDate" => <br>
     *  => [WorkOrder.class, Vehicle.class, FuelUsage.class, FuelCard.class] (if addCollectionalElementType = true) or <br>
     *  => [WorkOrder.class, Vehicle.class, Collection.class] (if addCollectionalElementType = false)
     *
     * @param root
     * @param property
     * @param addCollectionalElementType -- true => then correct element type of collectional property will be added to set, otherwise a {@link Collection.class} will be added.
     * @return
     */
    protected static Set<Class<?>> typesInHierarchy(final Class<?> root, final String property, final boolean addCollectionalElementType) {
	if (!PropertyTypeDeterminator.isDotNotation(property)) {
	    return new HashSet<Class<?>>() {
		private static final long serialVersionUID = 6314144790005942324L;
		{
		    add(DynamicEntityClassLoader.getOriginalType(root));
		}
	    };
	} else {
	    final Pair<String, String> penultAndLast = PropertyTypeDeterminator.penultAndLast(property);
	    final String penult = penultAndLast.getKey();
	    final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(root, penult);

	    return new HashSet<Class<?>>() {
		private static final long serialVersionUID = 6314144760005942324L;
		{
		    if (addCollectionalElementType) {
			add(DynamicEntityClassLoader.getOriginalType(PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), true, true)));
		    } else {
			final Class<?> type = PropertyTypeDeterminator.determineClass(transformed.getKey(), transformed.getValue(), true, false);
			add(DynamicEntityClassLoader.getOriginalType(Collection.class.isAssignableFrom(type) ? Collection.class : type));
		    }
		    addAll(typesInHierarchy(root, PropertyTypeDeterminator.penultAndLast(property).getKey(), addCollectionalElementType)); // recursively add other types
		}
	    };
	}
    }

    @Override
    public final void excludeImmutably(final Class<?> root, final String property) {
	excludedProperties.add(key(root, property));

	if (includedProperties.get(root) != null) { // not yet loaded
	    includedPropertiesMutable(root).remove(property);
	}
    }

    /**
     * An {@link ArrayList} specific implementation which listens to structure modifications (add / remove elements) and fires appropriate events.
     *
     * @author TG Team
     *
     */
    public static class ListenedArrayList extends ArrayList<String> {
	private static final long serialVersionUID = -4295706377290507263L;
	private final transient Class<?> root;
	private final transient AbstractDomainTreeRepresentation parentDtr;

	public ListenedArrayList() {
	    this(null, null);
	}

	public ListenedArrayList(final Class<?> root, final AbstractDomainTreeRepresentation parentDtr) {
	    super();
	    this.root = root;
	    this.parentDtr = parentDtr;
	}

	private void fireProperty(final Class<?> root, final String property, final boolean added) {
	    if (parentDtr != null) {
		for (final IPropertyListener listener : parentDtr.propertyListeners) {
		    listener.propertyStateChanged(root, property, added, null);
		}
	    }
	}

	@Override
	public boolean add(final String property) {
	    final boolean added = super.add(property);
	    if (added) {
		fireProperty(root, property, true);
	    }
	    return added;
	}

	@Override
	public void add(final int index, final String property) {
	    super.add(index, property);
	    fireProperty(root, property, true);
	}

	@Override
	public boolean remove(final Object obj) {
	    final boolean removed = super.remove(obj);
	    if (removed) {
		final String property = (String) obj;
		fireProperty(root, property, false);
	    }
	    return removed;
	}

	@Override
	public boolean addAll(final Collection<? extends String> properties) {
	    final boolean added = super.addAll(properties);
	    if (added) {
		for (final String property : properties) {
		    fireProperty(root, property, true);
		}
	    }
	    return added;
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends String> properties) {
	    final boolean added = super.addAll(index, properties);
	    if (added) {
		for (final String property : properties) {
		    fireProperty(root, property, true);
		}
	    }
	    return added;
	}
    }

    /**
     * Getter of mutable "included properties" cache for internal purposes.
     * <p>
     * Please note that you can only mutate this list with methods {@link List#add(Object)} and {@link List#remove(Object)}
     * to correctly reflect the changes on depending objects. (e.g. UI tree models, checked properties etc.)
     *
     * @param root
     * @return
     */
    protected List<String> includedPropertiesMutable(final Class<?> root1) {
	final Class<?> root = DynamicEntityClassLoader.getOriginalType(root1);
	if (includedProperties.get(root) == null) { // not yet loaded
	    final Date st = new Date();
	    enableListening(false);
	    // initialise included properties using isExcluded contract and manually excluded properties
	    final ListenedArrayList includedProps = new ListenedArrayList(root, this);
	    if (!isExcludedImmutably(root, "")) { // the entity itself is included -- add it to "included properties" list
		includedProps.add("");
		if (!EntityUtils.isEntityType(root)) {
		    throw new IllegalArgumentException("Can not add children properties to non-entity type [" + root.getSimpleName() + "] in path [" + root.getSimpleName() + "=>" + "" + "].");
		}
		includedProps.addAll(constructProperties(root, "", constructKeysAndProperties(root)));
	    }
	    enableListening(true);
	    includedProperties.put(root, includedProps);
	    logger().info("Root [" + root.getSimpleName() + "] has been processed within " + (new Date().getTime() - st.getTime()) + "ms with " + includedProps.size() + " included properties => [" + includedProps + "].");
	}
        return includedProperties.get(root);
    }

    /**
     * Enables or disables listening for each {@link ListenedArrayList} structures.
     *
     * @param enable
     */
    private void enableListening(final boolean enable) {
	if (enable) {
	    propertyListeners.addAll(disabledPropertyListeners);
	    disabledPropertyListeners.clear();
	} else {
	    disabledPropertyListeners.addAll(propertyListeners);
	    propertyListeners.clear();
	}
    }

    @Override
    public boolean addPropertyListener(final IPropertyListener listener) {
	return propertyListeners.add(listener);
    }

    @Override
    public boolean removePropertyListener(final IPropertyListener listener) {
        return propertyListeners.remove(listener);
    }

    @Override
    public List<String> includedProperties(final Class<?> root) {
        return Collections.unmodifiableList(includedPropertiesMutable(root));
    }

    /**
     * This method loads all missing properties on the tree path as defined in <code>fromPath</code> and <code>toPath</code> for type <code>root</code>.
     * Please note that property <code>fromPath</code> should be loaded itself (perhaps without its children).
     *
     * @param root
     * @param fromPath
     * @param toPath
     */
    protected final boolean warmUp(final Class<?> root, final String fromPath, final String toPath) {
	// System.out.println("Warm up => from = " + fromPath + "; to = " + toPath);
	if (includedPropertiesMutable(root).contains(fromPath)) { // the property itself exists in "included properties" cache
	    final String dummyMarker = createDummyMarker(fromPath);
	    final boolean shouldBeLoaded = includedPropertiesMutable(root).contains(dummyMarker);
	    if (shouldBeLoaded) { // the property is circular and has no children loaded -- it has to be done now
		final int index = includedPropertiesMutable(root).indexOf(dummyMarker);
		includedPropertiesMutable(root).remove(dummyMarker); // remove dummy property
		includedPropertiesMutable(root).addAll(index, constructProperties(root, fromPath, constructKeysAndProperties(PropertyTypeDeterminator.determinePropertyType(root, fromPath))));
	    }
	    if (!EntityUtils.equalsEx(fromPath, toPath)) { // not the leaf is trying to be warmed up
		final String part = "".equals(fromPath) ? toPath : toPath.replaceFirst(fromPath + ".", "");
		final String part2 = part.indexOf(".") > 0 ? part.substring(0, part.indexOf(".")) : part;
		final String part3 = "".equals(fromPath) ? part2 : (fromPath + "." + part2);
		final boolean hasBeenWarmedUp = warmUp(root, part3, toPath);
		return shouldBeLoaded || hasBeenWarmedUp;
	    } else {
		return shouldBeLoaded;
	    }
	} else {
	    throw new IllegalArgumentException("The property [" + fromPath + "] in root [" + root.getSimpleName() + "] should be already loaded into 'included properties'.");
	}
    }

    @Override
    public void warmUp(final Class<?> root, final String property) {
	final Date st = new Date();
	illegalExcludedProperties(this, root, reflectionProperty(property), "Could not 'warm up' an 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "]. Only properties that are not excluded can be 'warmed up'.");
	includedPropertiesMutable(root); // ensure "included properties" to be loaded
	if (warmUp(root, "", property)) {
	    logger().info("Warmed up root's [" + root.getSimpleName() + "] property [" + property + "] within " + (new Date().getTime() - st.getTime()) + "ms.");
	}
    }

    /**
     * Throws an {@link IllegalArgumentException} if the property is excluded.
     *
     * @param dtr
     * @param root
     * @param property
     * @param message
     */
    protected static void illegalExcludedProperties(final IDomainTreeRepresentation dtr, final Class<?> root, final String property, final String message) {
	if (dtr.isExcludedImmutably(root, property)) {
	    throw new IllegalArgumentException(message);
	}
    }

    /**
     * An abstract tick representation. <br><br>
     *
     * Includes default implementations of "disabling/immutable checking", that contain: <br>
     * a) manual state management; <br>
     * b) resolution of conflicts with excluded properties; <br>
     * c) automatic disabling of "immutably checked" properties.
     *
     * @author TG Team
     *
     */
    protected static abstract class AbstractTickRepresentation implements ITickRepresentation {
	private final EnhancementSet disabledProperties;
	private final Set<Pair<Class<?>, String>> checkedProperties;
	private final transient AbstractDomainTreeRepresentation dtr;
	private final transient TickManager tickManager;

	private final transient List<IPropertyDisablementListener> propertyDisablementListeners;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor and then into manager constructor, which should initialise "dtr" and "tickManager" fields.
	 */
	protected AbstractTickRepresentation() {
	    this.disabledProperties = createSet();
	    this.checkedProperties = createSet();

	    this.propertyDisablementListeners = new ArrayList<IPropertyDisablementListener>();

	    this.dtr = null; // IMPORTANT : to use this tick it should be passed into representation constructor, which should initialise "dtr" field.
	    this.tickManager = null; // IMPORTANT : to use this tick it should be passed into manager constructor, which should initialise "tickManager" field.
	}

	@Override
	public boolean isDisabledImmutably(final Class<?> root, final String property) {
	    illegalExcludedProperties(dtr, root, property, "Could not ask a 'disabled' state for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (disabledProperties.contains(key(root, property))) || // disable manually disabled properties
	    		(isCheckedImmutably(root, property)); // the checked by default properties should be disabled (immutable checking)
	}

	@Override
	public final void disableImmutably(final Class<?> root, final String property) {
	    illegalExcludedProperties(dtr, root, property, "Could not disable already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    disabledProperties.add(key(root, property));

	    fireDisablingEvent(root, property);
	}

	@Override
	public boolean addPropertyDisablementListener(final IPropertyDisablementListener listener) {
	    return propertyDisablementListeners.add(listener);
	}

	@Override
	public boolean removePropertyDisablementListener(final IPropertyDisablementListener listener) {
	    return propertyDisablementListeners.remove(listener);
	}

	/**
	 * Fires disablement event for specified property (the property has been disabled successfully).
	 *
	 * @param root
	 * @param property
	 */
	private void fireDisablingEvent(final Class<?> root, final String property) {
	    // fire DISABLED event after successful "disabled" action
	    for (final IPropertyDisablementListener listener : propertyDisablementListeners) {
		listener.propertyStateChanged(root, property, true, null);
	    }
	}

	@Override
	public boolean isCheckedImmutably(final Class<?> root, final String property) {
	    illegalExcludedProperties(dtr, root, property, "Could not ask a 'checked' state for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    return (checkedProperties.contains(key(root, property))); // check+disable manually checked properties
	}

	@Override
	public final void checkImmutably(final Class<?> root, final String property) {
	    illegalExcludedProperties(dtr, root, property, "Could not check immutably already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    tickManager.checkSimply(root, property, true);
	    checkedProperties.add(key(root, property));

	    fireDisablingEvent(root, property);
	}

	public AbstractDomainTreeRepresentation getDtr() {
	    return dtr;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((checkedProperties == null) ? 0 : checkedProperties.hashCode());
	    result = prime * result + ((disabledProperties == null) ? 0 : disabledProperties.hashCode());
	    return result;
	}

	@Override
	public boolean equals(final Object obj) {
	    if (this == obj)
		return true;
	    if (obj == null)
		return false;
	    if (getClass() != obj.getClass())
		return false;
	    final AbstractTickRepresentation other = (AbstractTickRepresentation) obj;
	    if (checkedProperties == null) {
		if (other.checkedProperties != null)
		    return false;
	    } else if (!checkedProperties.equals(other.checkedProperties))
		return false;
	    if (disabledProperties == null) {
		if (other.disabledProperties != null)
		    return false;
	    } else if (!disabledProperties.equals(other.disabledProperties))
		return false;
	    return true;
	}
    }

    @Override
    public ITickRepresentation getFirstTick() {
	return firstTick;
    }

    @Override
    public ITickRepresentation getSecondTick() {
        return secondTick;
    }

    @Override
    public Set<Class<?>> rootTypes() {
        return rootTypes;
    }

    @Override
    public Set<Function> availableFunctions(final Class<?> root, final String property) {
	illegalExcludedProperties(this, root, property, "Could not ask for 'available functions' for already 'excluded' property [" + property + "] in type [" + root.getSimpleName() + "].");
	final boolean isEntityItself = "".equals(property); // empty property means "entity itself"
	final Class<?> propertyType = isEntityItself ? root : PropertyTypeDeterminator.determinePropertyType(root, property);
	final Set<Function> availableFunctions = FunctionUtils.functionsFor(propertyType);

	if (!isEntityItself && isCalculatedAndOfTypes(root, property, CalculatedPropertyCategory.AGGREGATED_EXPRESSION, CalculatedPropertyCategory.ATTRIBUTED_COLLECTIONAL_EXPRESSION)) {
	    final Set<Function> functions = new HashSet<Function>();
	    if (availableFunctions.contains(Function.SELF)) {
		functions.add(Function.SELF);
	    }
	    return functions;
	}
	if (isEntityItself) {
	    availableFunctions.remove(Function.SELF);
	}
	if (!isInCollectionHierarchy(root, property)) {
	    availableFunctions.remove(Function.ALL);
	    availableFunctions.remove(Function.ANY);
	}
	if (!isEntityItself && Integer.class.isAssignableFrom(propertyType) && !isCalculatedAndOriginatedFromNotIntegerType(root, property)) {
	    availableFunctions.remove(Function.COUNT_DISTINCT);
	}
	return availableFunctions;
    }

    /**
     * Returns <code>true</code> if the property is calculated.
     *
     * @param root
     * @param property
     * @return
     */
    public static boolean isCalculated(final Class<?> root, final String property) {
	return AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property) != null;
    }

    /**
     * Returns <code>true</code> if the property is calculated with one of the specified categories.
     *
     * @param root
     * @param property
     * @param types
     * @return
     */
    protected static boolean isCalculatedAndOfTypes(final Class<?> root, final String property, final CalculatedPropertyCategory ... types) {
	final Calculated ca = AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);
	if (ca != null) {
	    for (final CalculatedPropertyCategory type : types) {
		if (type.equals(ca.category())) {
		    return true;
		}
	    }
	}
	return false;
    }

    protected static boolean isCalculatedAndOriginatedFromDateType(final Class<?> root, final String property) {
	final Calculated calculatedAnnotation = AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);
	return calculatedAnnotation != null && EntityUtils.isDate(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination()));
    }

    private static boolean isCalculatedAndOriginatedFromNotIntegerType(final Class<?> root, final String property) {
	final Calculated calculatedAnnotation = AnnotationReflector.getPropertyAnnotation(Calculated.class, root, property);
	return  calculatedAnnotation != null && !Integer.class.isAssignableFrom(PropertyTypeDeterminator.determinePropertyType(root, calculatedAnnotation.origination()));
    }

//    public static class Tuple <K, L, M, N> {
//	private final K first;
//	private final L second;
//	private final M third;
//	private final N fourth;
//
//	public Tuple(final K first, final L second, final M third, final N fourth) {
//	    super();
//	    this.first = first;
//	    this.second = second;
//	    this.third = third;
//	    this.fourth = fourth;
//	}
//
//	public K first() {
//	    return first;
//	}
//
//	public L second() {
//	    return second;
//	}
//
//	public M third() {
//	    return third;
//	}
//
//	public N fourth() {
//	    return fourth;
//	}
//    }
//
    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractDomainTreeRepresentationSerialiser<T extends AbstractDomainTreeRepresentation> extends AbstractDomainTreeSerialiser<T> {
	public AbstractDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public void write(final ByteBuffer buffer, final T representation) {
	    writeValue(buffer, representation.rootTypes);
	    writeValue(buffer, representation.excludedProperties);
	    writeValue(buffer, representation.firstTick);
	    writeValue(buffer, representation.secondTick);
	    writeValue(buffer, representation.includedProperties);
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((excludedProperties == null) ? 0 : excludedProperties.hashCode());
	result = prime * result + ((firstTick == null) ? 0 : firstTick.hashCode());
	result = prime * result + ((includedProperties == null) ? 0 : includedProperties.hashCode());
	result = prime * result + ((rootTypes == null) ? 0 : rootTypes.hashCode());
	result = prime * result + ((secondTick == null) ? 0 : secondTick.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final AbstractDomainTreeRepresentation other = (AbstractDomainTreeRepresentation) obj;
	if (excludedProperties == null) {
	    if (other.excludedProperties != null)
		return false;
	} else if (!excludedProperties.equals(other.excludedProperties))
	    return false;
	if (firstTick == null) {
	    if (other.firstTick != null)
		return false;
	} else if (!firstTick.equals(other.firstTick))
	    return false;
	if (includedProperties == null) {
	    if (other.includedProperties != null)
		return false;
	} else if (!includedProperties.equals(other.includedProperties))
	    return false;
	if (rootTypes == null) {
	    if (other.rootTypes != null)
		return false;
	} else if (!rootTypes.equals(other.rootTypes))
	    return false;
	if (secondTick == null) {
	    if (other.secondTick != null)
		return false;
	} else if (!secondTick.equals(other.secondTick))
	    return false;
	return true;
    }

    /** Please do not use this directly, use {@link #includedPropertiesMutable(Class)} lazy getter instead. */
    protected EnhancementRootsMap<ListenedArrayList> includedProperties() {
        return includedProperties;
    }

    public AbstractDomainTreeManager dtm() {
        return dtm;
    }
}
