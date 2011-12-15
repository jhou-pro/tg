package ua.com.fielden.platform.entity.query.model.builders;

import java.util.Map;

import ua.com.fielden.platform.entity.query.model.elements.MonthOfModel;

public class SumOfBuilder extends AbstractFunctionBuilder {

    protected SumOfBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    Object getModel() {
	return new MonthOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
