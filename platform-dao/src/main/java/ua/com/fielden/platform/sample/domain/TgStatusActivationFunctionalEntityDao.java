package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgStatusActivationFunctionalEntityMixin;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link ITgStatusActivationFunctionalEntity}.
 *
 * @author Developers
 *
 */
@EntityType(TgStatusActivationFunctionalEntity.class)
public class TgStatusActivationFunctionalEntityDao extends CommonEntityDao<TgStatusActivationFunctionalEntity> implements ITgStatusActivationFunctionalEntity {

    private final TgStatusActivationFunctionalEntityMixin mixin;
    private final ITgPersistentEntityWithProperties masterEntityCo;
    private final ITgPersistentStatus statusCo;

    @Inject
    public TgStatusActivationFunctionalEntityDao(final IFilter filter, final ITgPersistentEntityWithProperties masterEntityCo, final ITgPersistentStatus statusCo) {
        super(filter);

        this.masterEntityCo = masterEntityCo;
        this.statusCo = statusCo;

        mixin = new TgStatusActivationFunctionalEntityMixin(this);
    }

    @Override
    @SessionRequired
    public TgStatusActivationFunctionalEntity save(final TgStatusActivationFunctionalEntity entity) {
        for (final AbstractEntity<?> selectedEntity : entity.getContext().getSelectedEntities()) { // should be only single instance
            final TgPersistentEntityWithProperties selected = masterEntityCo.findById(selectedEntity.getId(), fetchAll(TgPersistentEntityWithProperties.class));
            selected.setStatus(statusCo.findByKeyAndFetch(fetchAll(TgPersistentStatus.class), "DR"));
            masterEntityCo.save(selected);
        }
        return super.save(entity);
    }
}