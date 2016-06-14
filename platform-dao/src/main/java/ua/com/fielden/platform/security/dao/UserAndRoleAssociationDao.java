package ua.com.fielden.platform.security.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IUserAndRoleAssociation;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;

import com.google.inject.Inject;

/**
 * DbDriven implementation of the {@link IUserAndRoleAssociation}
 * 
 * @author TG Team
 * 
 */
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements IUserAndRoleAssociation {

    @Inject
    protected UserAndRoleAssociationDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void removeAssociation(final Set<UserAndRoleAssociation> associations) {
        if (associations.size() == 0) {
            return;
        }
        String query = "delete from " + UserAndRoleAssociation.class.getName() + " where ";
        final List<String> querySubstr = new ArrayList<>();
        for (final UserAndRoleAssociation assoc : associations) {
            querySubstr.add("(user.id=" + assoc.getUser().getId() + " and userRole.id=" + //
                    assoc.getUserRole().getId() + ")");
        }
        query += StringUtils.join(querySubstr, " or ");
        getSession().createQuery(query).executeUpdate();
    }
    
    @Override
    @SessionRequired
    public int batchDelete(EntityResultQueryModel<UserAndRoleAssociation> model) {
        return defaultBatchDelete(model);
    }
}