/**
 *
 */
package ua.com.fielden.platform.swing.ei.editors.development;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.swing.components.bind.development.BoundedValidationLayer;
import ua.com.fielden.platform.swing.components.bind.development.ComponentFactory;
import ua.com.fielden.platform.swing.components.smart.autocompleter.development.AutocompleterTextFieldLayer;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Property editor for entities, that functions with string binding
 *
 * @author TG Team
 */

public class StringEntityPropertyEditor extends AbstractEntityPropertyEditor {

    private final BoundedValidationLayer<AutocompleterTextFieldLayer> editor;

    public StringEntityPropertyEditor(final Class<?> lookupClass, final AbstractEntity<?> entity, final String propertyName, final IValueMatcher<?> valueMatcher) {
	super(entity, propertyName, valueMatcher);
	this.editor = createEditor(entity, entity.getProperty(propertyName), lookupClass, valueMatcher);
    }

    private BoundedValidationLayer<AutocompleterTextFieldLayer> createEditor(final AbstractEntity entity, final MetaProperty metaProperty, final Class lookupClass, final IValueMatcher valueMatcher) {
	if (!String.class.isAssignableFrom(metaProperty.getType())) {
	    throw new RuntimeException("Could not determined an editor for property " + getPropertyName() + " of type " + metaProperty.getType() + ".");
	}
	final String[] secExpressions = EntityUtils.hasDescProperty(lookupClass) ? new String[] {"desc"} : null;
	final BoundedValidationLayer<AutocompleterTextFieldLayer> component = ComponentFactory.createOnFocusLostAutocompleter(entity, getPropertyName(), "", lookupClass, "key", secExpressions, null, valueMatcher, metaProperty.getDesc(), true);
	return component;
    }

    @Override
    public BoundedValidationLayer<AutocompleterTextFieldLayer> getEditor() {
	return editor;
    }

}
