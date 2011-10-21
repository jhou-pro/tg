package ua.com.fielden.platform.swing.review.analysis;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.pagination.IPage;

public class AnalysisReportQueryCriteriaExtender<T extends AbstractEntity, DAO extends IEntityDao<T>> extends GroupReportQueryCriteriaExtender<T, DAO, IPage<EntityAggregates>> {


    @Override
    public IPage<EntityAggregates> runExtendedQuery(final int pageSize) {
	final IQueryOrderedModel<EntityAggregates> queryModel = createExtendedQuery().model(EntityAggregates.class);
	return getBaseCriteria().getEntityAggregatesDao().firstPage(queryModel, createExtendedFetchModel(), pageSize);
    }

    @Override
    public IPage<EntityAggregates> exportExtendedQueryData() {
	throw new UnsupportedOperationException("The exporting is not supported yet for analysis reports.");
    }

}
