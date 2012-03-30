package ua.com.fielden.platform.test.entities.daos;

import ua.com.fielden.platform.dao2.CommonEntityDao2;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.TgWagonClassCompatibility;
import ua.com.fielden.platform.sample.domain.controller.ITgWagonClassCompatibility;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

@EntityType(TgWagonClassCompatibility.class)
public class TgWagonClassCompatibilityDao extends CommonEntityDao2<TgWagonClassCompatibility> implements ITgWagonClassCompatibility {

    @Inject
    protected TgWagonClassCompatibilityDao(final IFilter filter) {
	super(filter);
    }
}