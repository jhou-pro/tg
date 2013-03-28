package ua.com.fielden.platform.security.provider;

import java.util.Map;

import ua.com.fielden.platform.migration.AbstractRetriever;
import ua.com.fielden.platform.security.user.IUserDao;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Inject;

public class UserRetriever extends AbstractRetriever<User> {

    @Inject
    public UserRetriever(final IUserDao dao) {
	super(dao);
    }
    @Override
    public Map<String, String> resultFields() {
	return map( //
		field("key", "USER_ID") //
		);
    }

    @Override
    public String fromSql() {
	return "CRAFT";
    }
}