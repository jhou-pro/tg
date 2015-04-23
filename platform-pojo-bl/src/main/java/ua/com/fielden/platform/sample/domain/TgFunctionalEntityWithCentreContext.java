package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.sample.domain.definers.TgFunctionalEntityContextHandler;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * An example functional entity to be assigned to centre actions.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgFunctionalEntityWithCentreContext.class)
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgFunctionalEntityWithCentreContext extends AbstractFunctionalEntityWithCentreContext<String> {
    private static final long serialVersionUID = 4194736013542795762L;

    @IsProperty
    @Title(value = "Value To Insert", desc = "The string value to be inserted for stringProp of all selected entities")
    private String valueToInsert; // this property is intended to be context-dependent

    @IsProperty
    @Title(value = "With Brackets", desc = "Indicates whether the stringProp props should be wrapped by brackets after insertion")
    private boolean withBrackets;

    @IsProperty
    @Title(value = "Context", desc = "Context")
    @Invisible
    @AfterChange(TgFunctionalEntityContextHandler.class)
    private CentreContext<?, ?> context;

    @Override
    @Observable
    public TgFunctionalEntityWithCentreContext setContext(final CentreContext<?, ?> context) {
        this.context = context;
        return this;
    }

    @Override
    public CentreContext<?, ?> getContext() {
        return context;
    }

    @Observable
    public TgFunctionalEntityWithCentreContext setWithBrackets(final boolean withBrackets) {
        this.withBrackets = withBrackets;
        return this;
    }

    public boolean getWithBrackets() {
        return withBrackets;
    }

    @Observable
    public TgFunctionalEntityWithCentreContext setValueToInsert(final String valueToInsert) {
        this.valueToInsert = valueToInsert;
        return this;
    }

    public String getValueToInsert() {
        return valueToInsert;
    }
}