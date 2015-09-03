package ua.com.fielden.platform.web.resources.webui;

import java.io.ByteArrayInputStream;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.engine.application.EncodeRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.google.common.base.Charsets;

import ua.com.fielden.platform.web.app.ISourceController;
import ua.com.fielden.platform.web.resources.RestServerUtil;

/**
 * Represents web server resource that returns entity centre component for the specified 'miType' to the client.
 *
 * @author TG Team
 *
 */
public class CentreComponentResource extends ServerResource {
    private final ISourceController sourceController;
    private final String mitypeString;
    private final RestServerUtil restUtil;

    /**
     * Creates {@link CentreComponentResource} and initialises it with centre instance.
     *
     * @param centre
     * @param context
     * @param request
     * @param response
     */
    public CentreComponentResource(
            final RestServerUtil restUtil,
            final ISourceController sourceController,//
            final Context context, //
            final Request request, //
            final Response response) {
        init(context, request, response);
        this.restUtil = restUtil;
        this.sourceController = sourceController;
        this.mitypeString = (String) request.getAttributes().get("mitype");
    }

    @Override
    protected Representation get() {
        return EntityResourceUtils.handleUndesiredExceptions(getResponse(), () -> {
            final String source = sourceController.loadSource("/centre_ui/" + this.mitypeString);
            return new EncodeRepresentation(Encoding.GZIP, new InputRepresentation(new ByteArrayInputStream(source.getBytes(Charsets.UTF_8))));
        }, restUtil);
    }
}
