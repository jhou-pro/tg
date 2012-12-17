package ua.com.fielden.platform.swing.review.report.analysis.query.customiser;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.report.query.generation.IReportQueryGenerator;
import ua.com.fielden.platform.swing.review.report.analysis.view.AbstractAnalysisReviewModel;

/**
 * The contract that enables analysis' query customisation.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <AM>
 */
public interface IAnalysisQueryCustomiser<T extends AbstractEntity<?>, AM extends AbstractAnalysisReviewModel<T, ?, ?>> {

    /**
     * Returns the instance of {@link IReportQueryGenerator} provided by this analysis customiser.
     *
     * @param analysisModel
     * @return
     */
    IReportQueryGenerator<T> getQueryGenerator(AM analysisModel);
}