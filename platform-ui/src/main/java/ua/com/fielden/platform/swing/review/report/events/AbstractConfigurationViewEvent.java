package ua.com.fielden.platform.swing.review.report.events;

import java.util.EventObject;

/**
 * An {@link EventObject} that represents abstract configuration view event.
 * 
 * @author TG Team
 * 
 */
public class AbstractConfigurationViewEvent extends EventObject {

    private static final long serialVersionUID = 863396228823661496L;

    public enum AbstractConfigurationViewEventAction {
        PRE_OPEN,
        OPEN,
        POST_OPEN,
        OPEN_FAILED,
        PRE_BUILD,
        BUILD,
        POST_BUILD,
        BUILD_FAILED,
        PRE_CANCEL,
        CANCEL,
        POST_CANCEL,
        CANCEL_FAILED,
        PRE_CONFIGURE,
        CONFIGURE,
        POST_CONFIGURE,
        CONFIGURE_FAILED;
    }

    private final AbstractConfigurationViewEventAction eventAction;

    public AbstractConfigurationViewEvent(final Object source, final AbstractConfigurationViewEventAction eventAction) {
        super(source);
        this.eventAction = eventAction;
    }

    public AbstractConfigurationViewEventAction getEventAction() {
        return eventAction;
    }
}
