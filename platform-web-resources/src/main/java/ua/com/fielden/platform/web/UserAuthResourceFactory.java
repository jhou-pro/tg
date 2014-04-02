package ua.com.fielden.platform.web;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

import ua.com.fielden.platform.security.provider.IUserController;
import ua.com.fielden.platform.web.resources.RestServerUtil;
import ua.com.fielden.platform.web.resources.UserAuthResource;

import com.google.inject.Injector;

/**
 * A restlet responsible for handling user authentication requests.
 * 
 * @author TG Team
 * 
 */
public class UserAuthResourceFactory extends Restlet {
    private final Injector injector;
    private final RestServerUtil restUtil;

    public UserAuthResourceFactory(final Injector injector, final RestServerUtil restUtil) {
        this.injector = injector;
        this.restUtil = restUtil;
    }

    @Override
    public void handle(final Request request, final Response response) {
        super.handle(request, response);

        if (Method.GET.equals(request.getMethod())) {
            handleGet(request, response);
        }
    }

    /**
     * Handles get request. Could be overridden to perform specific user-related tasks.
     * 
     * @param request
     * @param response
     */
    protected void handleGet(final Request request, final Response response) {
        final IUserController controller = getController();
        new UserAuthResource(controller, restUtil, getContext(), request, response).handle();
    }

    protected IUserController getController() {
        return injector.getInstance(IUserController.class);
    }

    protected RestServerUtil getRestUtil() {
        return restUtil;
    }

    protected Injector getInjector() {
        return injector;
    }
}
