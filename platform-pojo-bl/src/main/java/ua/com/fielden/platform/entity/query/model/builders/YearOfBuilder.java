package ua.com.fielden.platform.entity.query.model.builders;

import java.util.Map;

import ua.com.fielden.platform.entity.query.model.elements.YearOfModel;

public class YearOfBuilder extends AbstractFunctionBuilder {

    protected YearOfBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion, final Map<String, Object> paramValues) {
	super(parent, dbVersion, paramValues);
    }

    @Override
    Object getModel() {
	return new YearOfModel(getModelForSingleOperand(firstCat(), firstValue()));
    }
}
