package ua.com.fielden.platform.swing.review.report.centre.configuration;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.components.blocking.BlockingIndefiniteProgressLayer;
import ua.com.fielden.platform.swing.review.report.centre.MultipleAnalysisEntityCentre;

public class MultipleAnalysisEntityCentreConfigurationView<T extends AbstractEntity<?>> extends CentreConfigurationView<T, MultipleAnalysisEntityCentre<T>> {

    private static final long serialVersionUID = -6434256458143463705L;

    public MultipleAnalysisEntityCentreConfigurationView(final CentreConfigurationModel<T> model, final BlockingIndefiniteProgressLayer progressLayer) {
	super(model, progressLayer);
    }

    @Override
    protected MultipleAnalysisEntityCentre<T> initConfigurableView(final MultipleAnalysisEntityCentre<T> configurableView) {
	return super.initConfigurableView(new MultipleAnalysisEntityCentre<T>(getModel().createEntityCentreModel(), getProgressLayer()));
    }
}
