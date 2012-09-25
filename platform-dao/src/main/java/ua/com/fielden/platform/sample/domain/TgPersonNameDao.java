package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgPersonName}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPersonName.class)
public class TgPersonNameDao extends CommonEntityDao<TgPersonName> implements ITgPersonName {
    @Inject
    public TgPersonNameDao(final IFilter filter) {
        super(filter);
    }

}