package ua.com.fielden.platform.dao2;

import java.io.IOException;
import java.util.List;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.pagination.IPage2;

public interface IEntityAggregatesDao2 {

    /**
     * Username should be provided for every DAO instance in order to support data filtering and auditing.
     */
    void setUsername(final String username);
    String getUsername();

    /**
     * Returns results from running given aggregation query.
     *
     * @param aggregatesQueryModel
     * @return
     */
    List<EntityAggregates> getAggregates(final AggregatesQueryExecutionModel aggregatesQueryModel);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided query model (new EntityQuery).
     *
     * @param pageCapacity
     * @param query
     * @return
     */

    IPage2<EntityAggregates> firstPage(AggregatesQueryExecutionModel query, final int pageCapacity);

    /**
     * Should return a reference to the first page of the specified size containing entity instances retrieved using the provided <code>summaryModel</code> and the summary
     * information based on <code>summaryModel</code>.
     *
     * @param model
     * @param summaryModel
     * @param pageCapacity
     * @return
     */
    IPage2<EntityAggregates> firstPage(final AggregatesQueryExecutionModel model, final AggregatesQueryExecutionModel summaryModel, final int pageCapacity);


    /**
     * Returns a reference to a page with requested number and capacity holding entity instances matching the provided query model (new EntityQuery).
     *
     * @param model
     * @param pageNo
     * @param pageCapacity
     * @return
     */

    IPage2<EntityAggregates> getPage(final AggregatesQueryExecutionModel model, final int pageNo, final int pageCapacity);

    /**
     * The same as {@link #getPage(AggregatesQueryExecutionModel, int, int)}, but with page count information, which could be taken into account during implementation.
     *
     * @param model
     * @param pageNo
     * @param pageCount
     * @param pageCapacity
     * @return
     */
    IPage2<EntityAggregates> getPage(final AggregatesQueryExecutionModel model, final int pageNo, final int pageCount, final int pageCapacity);

    /**
     * Should return a byte array representation the exported data in a format envisaged by the specific implementation.
     * <p>
     * For example it could be a byte array of GZipped Excel data.
     *
     * @param query
     *            -- query result of which should be exported.
     * @param propertyNames
     *            -- names of properties, including dot notated properties, which should be used in the export.
     * @param propertyTitles
     *            -- titles corresponding to the properties being exported, which are used as headers of columns.
     * @return
     */
    byte[] export(final AggregatesQueryExecutionModel query, final String[] propertyNames, final String[] propertyTitles)
	    throws IOException;
}