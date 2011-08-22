package ua.com.fielden.platform.entity.query.model.builders;

import ua.com.fielden.platform.entity.query.model.elements.MonthOfModel;

public class MonthOfBuilder extends AbstractFunctionBuilder {

    protected MonthOfBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	super(parent, dbVersion);
    }

    @Override
    Object getModel() {
	return new MonthOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
