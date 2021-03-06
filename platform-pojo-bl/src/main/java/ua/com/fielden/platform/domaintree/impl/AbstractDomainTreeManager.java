package ua.com.fielden.platform.domaintree.impl;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager.IPropertyCheckingListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.IPropertyListener;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.serialisation.api.ISerialiser;

/**
 * Abstract domain tree manager for all TG trees. Includes support for checking and functions managing. <br>
 * <br>
 * 
 * Includes implementation of "mutable checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 * 
 * @author TG Team
 * 
 */
public abstract class AbstractDomainTreeManager extends AbstractDomainTree implements IDomainTreeManager {
    private final AbstractDomainTreeRepresentation dtr;
    private final TickManager firstTick;
    private final TickManager secondTick;
    private final transient IPropertyListener includedPropertiesSynchronisationListener;

    /**
     * A <i>manager</i> constructor.
     * 
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AbstractDomainTreeManager(final ISerialiser serialiser, final AbstractDomainTreeRepresentation dtr, final TickManager firstTick, final TickManager secondTick) {
        super(serialiser);
        this.dtr = dtr;
        this.firstTick = firstTick;
        this.secondTick = secondTick;

        // initialise the references on "dtr" instance in "firstTick" and "secondTick" fields
        // and initialise the references on "firstTick" and "secondTick" instances in "dtr.firstTick" and "dtr.secondTick" fields
        try {
            final Field dtrField = Finder.findFieldByName(TickManager.class, "dtr");
            boolean isAccessible = dtrField.isAccessible();
            dtrField.setAccessible(true);
            dtrField.set(this.firstTick, this.dtr);
            dtrField.set(this.secondTick, this.dtr);
            dtrField.setAccessible(isAccessible);

            final Field trField = Finder.findFieldByName(TickManager.class, "tr");
            isAccessible = trField.isAccessible();
            trField.setAccessible(true);
            trField.set(this.firstTick, this.dtr.getFirstTick());
            trField.set(this.secondTick, this.dtr.getSecondTick());
            trField.setAccessible(isAccessible);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }

        // TODO
        //	for (final Entry<Class<?>, ListenedArrayList> entry : this.dtr.includedProperties().entrySet()) {
        //	    // initialise the references on this instance in "included properties" lists
        //	    try {
        //		final Field parentDtrField = Finder.findFieldByName(ListenedArrayList.class, "parentDtr");
        //		final boolean isAccessible = parentDtrField.isAccessible();
        //		parentDtrField.setAccessible(true);
        //		parentDtrField.set(entry.getValue(), this.getRepresentation());
        //		parentDtrField.setAccessible(isAccessible);
        //	    } catch (final Exception e) {
        //		e.printStackTrace();
        //		throw new IllegalStateException(e);
        //	    }
        //	}

        // the below listener is intended to update checked properties for both ticks when the skeleton of included properties has been changed
        includedPropertiesSynchronisationListener = new IncludedAndCheckedPropertiesSynchronisationListener(this.firstTick, this.secondTick, (ITickRepresentationWithMutability) this.getRepresentation().getFirstTick(), (ITickRepresentationWithMutability) this.getRepresentation().getSecondTick(), dtr);
        this.getRepresentation().addPropertyListener(includedPropertiesSynchronisationListener);
    }

    /**
     * This interface is just a wrapper for {@link ITickManager} with accessor to mutable "checked properties".
     * 
     * @author TG Team
     * 
     */
    public interface ITickManagerWithMutability extends ITickManager {
        /**
         * Getter of mutable "checked properties" cache for internal purposes.
         * <p>
         * These properties are fully lazy. If some "root" has not been used -- it will not be loaded. This partly initialised stuff could be even persisted. After deserialisation
         * lazy mechanism can simply load missing stuff well.
         * 
         * @param root
         * @return
         */
        List<String> checkedPropertiesMutable(final Class<?> root);

        /**
         * TODO
         * 
         * @param root
         * @param property
         * @return
         */
        boolean isCheckedNaturally(final Class<?> root, final String property);

        /**
         * TODO
         * 
         * @param root
         * @param property
         * @return
         */
        boolean isCheckedLightweight(final Class<?> root, final String property);
    }

    /**
     * This interface is just a wrapper for {@link ITickManager} with accessor to mutable "checked properties".
     * 
     * @author TG Team
     * 
     */
    public interface ITickRepresentationWithMutability extends ITickRepresentation {
        /**
         * Getter of mutable "disabled manually properties" cache for internal purposes.
         * 
         * @param root
         * @return
         */
        EnhancementSet disabledManuallyPropertiesMutable();

        /**
         * TODO
         * 
         * @return
         */
        boolean isDisabledImmutablyLightweight(final Class<?> root, final String property);
    }

    /**
     * The "structure changed" listener that takes care about synchronisation of "included properties" with "checked / disabled properties" for both ticks.
     * 
     * @author TG Team
     * 
     */
    protected static class IncludedAndCheckedPropertiesSynchronisationListener implements IPropertyListener {
        private final TickManager firstTickManager, secondTickManager;
        private final ITickRepresentationWithMutability firstTickRepresentation, secondTickRepresentation;
        private final IDomainTreeRepresentationWithMutability domainTreeRepresentation;

        @Override
        public boolean isInternal() {
            return true;
        }

        /**
         * A constructor that requires two ticks and two tick representations for synchronisation.
         * 
         * @param firstTick
         * @param secondTick
         */
        protected IncludedAndCheckedPropertiesSynchronisationListener(final TickManager firstTick, final TickManager secondTick, final ITickRepresentationWithMutability firstTickRepresentation, final ITickRepresentationWithMutability secondTickRepresentation, final IDomainTreeRepresentationWithMutability domainTreeRepresentation) {
            this.firstTickManager = firstTick;
            this.secondTickManager = secondTick;
            this.firstTickRepresentation = firstTickRepresentation;
            this.secondTickRepresentation = secondTickRepresentation;
            this.domainTreeRepresentation = domainTreeRepresentation;
        }

        @Override
        public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenAdded, final Boolean oldState) {
            if (hasBeenAdded == null) {
                throw new IllegalArgumentException("'hasBeenAdded' cannot be 'null'.");
            }
            if (hasBeenAdded) { // property has been ADDED
                if (!isDummyMarker(property)) {
                    final String reflectionProperty = reflectionProperty(property);
                    // update checked properties
                    // logger().info("Started isCheckedNaturallyFirst for property [" + reflectionProperty + "].");
                    final boolean isCheckedNaturallyFirst = firstTickManager.isCheckedNaturally(root, reflectionProperty);
                    // logger().info("Ended isCheckedNaturallyFirst for property [" + reflectionProperty + "].");

                    if (isCheckedNaturallyFirst && !firstTickManager.checkedPropertiesMutable(root).contains(reflectionProperty)) {
                        firstTickManager.insertCheckedProperty(root, reflectionProperty, firstTickManager.checkedPropertiesMutable(root).size()); // add it to the end of list
                    }

                    // logger().info("Started isCheckedNaturallySecond for property [" + reflectionProperty + "].");
                    final boolean isCheckedNaturallySecond = secondTickManager.isCheckedNaturally(root, reflectionProperty);
                    // logger().info("Ended isCheckedNaturallySecond for property [" + reflectionProperty + "].");

                    if (isCheckedNaturallySecond && !secondTickManager.checkedPropertiesMutable(root).contains(reflectionProperty)) {
                        secondTickManager.checkedPropertiesMutable(root).add(reflectionProperty); // add it to the end of list
                    }
                }
            } else { // property has been REMOVED
                if (!isDummyMarker(property)) {
                    final String reflectionProperty = reflectionProperty(property);
                    // update checked properties
                    if (firstTickManager.checkedPropertiesMutable(root).contains(reflectionProperty)) {
                        firstTickManager.removeCheckedProperty(root, reflectionProperty);
                    }
                    if (secondTickManager.checkedPropertiesMutable(root).contains(reflectionProperty)) {
                        secondTickManager.checkedPropertiesMutable(root).remove(reflectionProperty);
                    }

                    // update manually disabled properties
                    if (firstTickRepresentation.disabledManuallyPropertiesMutable().contains(key(root, reflectionProperty))) {
                        firstTickRepresentation.disabledManuallyPropertiesMutable().remove(key(root, reflectionProperty));
                    }
                    if (secondTickRepresentation.disabledManuallyPropertiesMutable().contains(key(root, reflectionProperty))) {
                        secondTickRepresentation.disabledManuallyPropertiesMutable().remove(key(root, reflectionProperty));
                    }
                }
            }
        }
    }

    protected IPropertyListener listener() {
        return includedPropertiesSynchronisationListener;
    }

    /**
     * The weak wrapper for {@link IPropertyCheckingListener} instance.
     * 
     * @author TG Team
     * 
     */
    private static class WeakPropertyCheckingListener implements IPropertyCheckingListener {

        private final WeakReference<IPropertyCheckingListener> ref;
        private final ITickManager tickManager;

        /**
         * Creates weak wrapper for {@link IPropertyCheckingListener} instance and tick manager. (Please note that tickManager doesn't registers specified listener. It must be done
         * manually!).
         * 
         * @param listener
         * @param tickManager
         */
        public WeakPropertyCheckingListener(final IPropertyCheckingListener listener, final ITickManager tickManager) {
            this.ref = new WeakReference<IDomainTreeManager.ITickManager.IPropertyCheckingListener>(listener);
            this.tickManager = tickManager;
        }

        public void propertyStateChanged(final Class<?> root, final String property, final Boolean hasBeenChecked, final Boolean oldState, final int index) {
            if (ref.get() != null) {
                ref.get().propertyStateChanged(root, property, hasBeenChecked, oldState, index);
            } else {
                tickManager.removePropertyCheckingListener(this);
            }
        }

        /**
         * Returns the weak reference of {@link IPropertyCheckingListener} on which this instance is referenced to.
         * 
         * @return
         */
        public IPropertyCheckingListener getRef() {
            return ref.get();
        }
    }

    /**
     * A tick manager with all sufficient logic. <br>
     * <br>
     * 
     * Includes implementation of "checking" logic, that contain: <br>
     * a) default mutable state management; <br>
     * a) manual state management; <br>
     * b) resolution of conflicts with excluded etc. properties; <br>
     * 
     * @author TG Team
     * 
     */
    public static class TickManager implements ITickManagerWithMutability {
        private final EnhancementRootsMap<List<String>> checkedProperties;
        private final transient AbstractDomainTreeRepresentation dtr;
        private final transient ITickRepresentation tr;

        private final transient EventListenerList propertyCheckingListeners;

        /**
         * Used for the first time instantiation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr" fields.
         */
        public TickManager() {
            this(AbstractDomainTree.<List<String>> createRootsMap());
        }

        /**
         * Used for serialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr" fields.
         */
        protected TickManager(final Map<Class<?>, List<String>> checkedProperties) {
            this.checkedProperties = createRootsMap();
            this.checkedProperties.putAll(checkedProperties);

            this.propertyCheckingListeners = new EventListenerList();

            this.dtr = null;
            this.tr = null;
        }

        /**
         * This method is designed to be overridden in descendants to provide custom "mutable checking" logic.
         * 
         * @param root
         * @param property
         * @return
         */
        protected boolean isCheckedMutably(final Class<?> root, final String property) {
            return false;
        }

        @Override
        public boolean isCheckedNaturally(final Class<?> root, final String property) {
            AbstractDomainTreeRepresentation.illegalExcludedProperties(dtr, root, property, "Could not ask a 'checked' state for already 'excluded' property [" + property
                    + "] in type [" + root.getSimpleName() + "].");
            return isCheckedNaturallyLightweight(root, property);
        }

        public boolean isCheckedNaturallyLightweight(final Class<?> root, final String property) {
            return isCheckedMutably(root, property) || // checked properties by a "contract"
                    tr.isCheckedImmutably(root, property); // the checked by default properties should be checked (immutable checking)
        }

        @Override
        public boolean isChecked(final Class<?> root, final String property) {
            loadParent(root, property);
            if (checkedProperties.get(root) == null) { // not yet loaded
                return isCheckedNaturally(root, property);
            } else {
                AbstractDomainTreeRepresentation.illegalExcludedProperties(dtr, root, property, "Could not ask a 'checked' state for already 'excluded' property [" + property
                        + "] in type [" + root.getSimpleName() + "].");
                return checkedPropertiesMutable(root).contains(property);
            }
        }

        @Override
        public boolean isCheckedLightweight(final Class<?> root, final String property) {
            // I know that the parent is already loaded.
            if (checkedProperties.get(root) == null) { // not yet loaded
                return isCheckedNaturallyLightweight(root, property);
            } else {
                // I also know that this property is not excluded. So there is no need to invoke heavy-weight method "illegalExcludedProperties"
                return checkedPropertiesMutable(root).contains(property);
            }
        }

        /**
         * Loads parent property to ensure that working with this property is safe.
         * 
         * @param root
         * @param property
         */
        private void loadParent(final Class<?> root, final String property) {
            dtr.warmUp(root, PropertyTypeDeterminator.isDotNotation(property) ? PropertyTypeDeterminator.penultAndLast(property).getKey() : "");
        }

        @Override
        public ITickManager check(final Class<?> root, final String property, final boolean check) {
            AbstractDomainTreeRepresentation.illegalExcludedProperties(dtr, root, property, "Could not [un]check already 'excluded' property [" + property + "] in type ["
                    + root.getSimpleName() + "].");
            if (tr.isDisabledImmutably(root, property)) {
                throw new IllegalArgumentException("Could not [un]check 'disabled' property [" + property + "] in type [" + root.getSimpleName() + "].");
            }
            checkSimply(root, property, check);
            return this;
        }

        protected void checkSimply(final Class<?> root, final String property, final boolean check) {
            loadParent(root, property);
            final boolean contains = checkedPropertiesMutable(root).contains(property);
            if (check) {
                if (!contains) {
                    insertCheckedProperty(root, property, checkedPropertiesMutable(root).size());
                } else {
                    logger().warn("Could not check already checked property [" + property + "] in type [" + root.getSimpleName() + "].");
                }
            } else {
                if (contains) {
                    removeCheckedProperty(root, property);
                } else {
                    logger().warn("Could not uncheck already unchecked property [" + property + "] in type [" + root.getSimpleName() + "].");
                }
            }
        }

        protected void removeCheckedProperty(final Class<?> root, final String property) {
            final int index = checkedPropertiesMutable(root).indexOf(property);
            checkedPropertiesMutable(root).remove(property);

            // fire UNCHECKED event after successful "unchecked" action
            for (final IPropertyCheckingListener listener : propertyCheckingListeners.getListeners(IPropertyCheckingListener.class)) {
                listener.propertyStateChanged(root, property, false, null, index);
            }
        }

        protected void insertCheckedProperty(final Class<?> root, final String property, final int index) {
            checkedPropertiesMutable(root).add(index, property);

            // fire CHECKED event after successful "checked" action
            for (final IPropertyCheckingListener listener : propertyCheckingListeners.getListeners(IPropertyCheckingListener.class)) {
                listener.propertyStateChanged(root, property, true, null, index);
            }
        }

        @Override
        public void addPropertyCheckingListener(final IPropertyCheckingListener listener) {
            removeEmptyWeakPropertyCheckingListener();
            propertyCheckingListeners.add(IPropertyCheckingListener.class, listener);
        }

        @Override
        public void addWeakPropertyCheckingListener(final IPropertyCheckingListener listener) {
            removeEmptyWeakPropertyCheckingListener();
            propertyCheckingListeners.add(IPropertyCheckingListener.class, new WeakPropertyCheckingListener(listener, this));
        }

        @Override
        public void removePropertyCheckingListener(final IPropertyCheckingListener listener) {
            for (final IPropertyCheckingListener obj : propertyCheckingListeners.getListeners(IPropertyCheckingListener.class)) {
                if (listener == obj) {
                    propertyCheckingListeners.remove(IPropertyCheckingListener.class, listener);
                } else if (obj instanceof WeakPropertyCheckingListener) {
                    final IPropertyCheckingListener weakRef = ((WeakPropertyCheckingListener) obj).getRef();
                    if (weakRef == listener || weakRef == null) {
                        propertyCheckingListeners.remove(IPropertyCheckingListener.class, obj);
                    }
                }
            }
        }

        /**
         * Removes the {@link WeakPropertyCheckingListener} which has empty weak reference to the {@link IPropertyCheckingListener} instance.
         */
        private void removeEmptyWeakPropertyCheckingListener() {
            for (final IPropertyCheckingListener obj : propertyCheckingListeners.getListeners(IPropertyCheckingListener.class)) {
                if (obj instanceof WeakPropertyCheckingListener && ((WeakPropertyCheckingListener) obj).getRef() == null) {
                    propertyCheckingListeners.remove(IPropertyCheckingListener.class, obj);
                }
            }
        }

        @Override
        public List<String> checkedProperties(final Class<?> root) {
            return Collections.unmodifiableList(checkedPropertiesMutable(root));
        }

        @Override
        public synchronized List<String> checkedPropertiesMutable(final Class<?> rootPossiblyEnhanced) {
            final Class<?> root = DynamicEntityClassLoader.getOriginalType(rootPossiblyEnhanced);
            if (checkedProperties.get(root) == null) { // not yet loaded
                final Date st = new Date();
                // initialise checked properties using isChecked contract and "included properties" cache
                final List<String> includedProps = dtr.includedProperties(root);
                final List<String> checkedProps = new ArrayList<String>();
                checkedProperties.put(root, checkedProps);
                // the original order of "included properties" will be used for "checked properties" at first
                for (final String includedProperty : includedProps) {
                    if (!isDummyMarker(includedProperty)) {
                        if (isCheckedNaturally(rootPossiblyEnhanced, reflectionProperty(includedProperty))) {
                            insertCheckedProperty(root, includedProperty, checkedProperties.get(root).size());
                        }
                    }
                }
                logger().debug("Root [" + root.getSimpleName() + "] has been processed within " + (new Date().getTime() - st.getTime()) + "ms with " + checkedProps.size()
                        + " checked properties."); //  => [" + checkedProps + "]
            }
            return checkedProperties.get(root);
        }

        private void checkPropertyExistence(final List<String> props, final String property, final String message) {
            if (!props.contains(property)) {
                throw new IllegalArgumentException(message);
            }
        }

        @Override
        public ITickManager swap(final Class<?> root, final String property1, final String property2) {
            final List<String> props = checkedPropertiesMutable(root);
            checkPropertyExistence(props, property1, "'Swap' operation for 'checked properties' failed. The property [" + property1 + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            checkPropertyExistence(props, property2, "'Swap' operation for 'checked properties' failed. The property [" + property2 + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            Collections.swap(props, props.indexOf(property1), props.indexOf(property2));
            return this;
        }

        @Override
        public ITickManager move(final Class<?> root, final String what, final String beforeWhat) {
            final List<String> props = checkedPropertiesMutable(root);
            checkPropertyExistence(props, what, "'Move' operation for 'checked properties' failed. The property [" + what + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            checkPropertyExistence(props, beforeWhat, "'Move' operation for 'checked properties' failed. The property [" + beforeWhat + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            props.remove(what);
            props.add(props.indexOf(beforeWhat), what);
            return this;
        }

        @Override
        public ITickManager moveToTheEnd(final Class<?> root, final String what) {
            final List<String> props = checkedPropertiesMutable(root);
            checkPropertyExistence(props, what, "'Move to the end' operation for 'checked properties' failed. The property [" + what + "] in type [" + root.getSimpleName()
                    + "] is not checked.");
            props.remove(what);
            props.add(what);
            return this;
        }

        protected ITickRepresentation tr() {
            return tr;
        }

        protected IDomainTreeRepresentation dtr() {
            return dtr;
        }

        protected Map<Class<?>, List<String>> checkedProperties() {
            return checkedProperties;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (checkedProperties == null ? 0 : checkedProperties.hashCode());
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
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TickManager other = (TickManager) obj;
            if (checkedProperties == null) {
                if (other.checkedProperties != null) {
                    return false;
                }
            } else if (!checkedProperties.equals(other.checkedProperties)) {
                return false;
            }
            return true;
        }
    }

    @Override
    public ITickManager getFirstTick() {
        return firstTick;
    }

    @Override
    public ITickManager getSecondTick() {
        return secondTick;
    }

    @Override
    public IDomainTreeRepresentation getRepresentation() {
        return dtr;
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManager}.
     * 
     * @author TG Team
     * 
     */
    protected abstract static class AbstractDomainTreeManagerSerialiser<T extends AbstractDomainTreeManager> extends AbstractDomainTreeSerialiser<T> {
        public AbstractDomainTreeManagerSerialiser(final ISerialiser serialiser) {
            super(serialiser);
        }

        @Override
        public void write(final ByteBuffer buffer, final T manager) {
            writeValue(buffer, manager.getDtr());
            writeValue(buffer, manager.getFirstTick());
            writeValue(buffer, manager.getSecondTick());
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (dtr == null ? 0 : dtr.hashCode());
        result = prime * result + (firstTick == null ? 0 : firstTick.hashCode());
        result = prime * result + (secondTick == null ? 0 : secondTick.hashCode());
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractDomainTreeManager other = (AbstractDomainTreeManager) obj;
        if (dtr == null) {
            if (other.dtr != null) {
                return false;
            }
        } else if (!dtr.equals(other.dtr)) {
            return false;
        }
        if (firstTick == null) {
            if (other.firstTick != null) {
                return false;
            }
        } else if (!firstTick.equals(other.firstTick)) {
            return false;
        }
        if (secondTick == null) {
            if (other.secondTick != null) {
                return false;
            }
        } else if (!secondTick.equals(other.secondTick)) {
            return false;
        }
        return true;
    }

    public AbstractDomainTreeRepresentation getDtr() {
        return dtr;
    }
}
