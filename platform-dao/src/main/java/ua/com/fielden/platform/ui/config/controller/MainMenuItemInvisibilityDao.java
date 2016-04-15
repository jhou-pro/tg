package ua.com.fielden.platform.ui.config.controller;

import java.util.Map;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.ui.config.api.IMainMenuItemInvisibility;

import com.google.inject.Inject;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * DAO implementation of {@link IMainMenuItemInvisibility}.
 * 
 * @author TG Team
 * 
 */
@EntityType(MainMenuItemInvisibility.class)
public class MainMenuItemInvisibilityDao extends CommonEntityDao<MainMenuItemInvisibility> implements IMainMenuItemInvisibility {

    private final EntityFactory factory;

    @Inject
    protected MainMenuItemInvisibilityDao(final IFilter filter, final EntityFactory factory) {
        super(filter);
        this.factory = factory;
    }

    @Override
    @SessionRequired
    public void delete(final MainMenuItemInvisibility entity) {
        super.defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<MainMenuItemInvisibility> model, final Map<String, Object> paramValues) {
        super.defaultDelete(model, paramValues);
    }

    @Override
    public void makeInvisible(final MainMenuItem menuItem, final User user) {
        final MainMenuItemInvisibility invisibility = factory.newByKey(MainMenuItemInvisibility.class, user, menuItem);
        save(invisibility);

    }

    @Override
    public void makeVisible(final MainMenuItem menuItem, final User user) {
        final EntityResultQueryModel<MainMenuItemInvisibility> model = select(MainMenuItemInvisibility.class).where()//
        .prop("owner").eq().val(user).and()//
        .prop("menuItem").eq().val(menuItem).model();

        delete(model);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(EntityResultQueryModel<MainMenuItemInvisibility> model) {
        return defaultBatchDelete(model);
    }
}