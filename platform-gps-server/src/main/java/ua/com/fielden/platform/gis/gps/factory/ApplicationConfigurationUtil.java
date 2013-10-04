package ua.com.fielden.platform.gis.gps.factory;

import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.gis.gps.AbstractAvlMachine;
import ua.com.fielden.platform.gis.gps.AbstractAvlMessage;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.AbstractAvlMachineActor;
import ua.com.fielden.platform.gis.gps.monitoring.DefaultMachineMonitoringProvider;
import ua.com.fielden.platform.gis.gps.monitoring.IMachineMonitoringProvider;

import com.google.inject.Injector;

/**
 * An utility class to start GPS server services (like Netty GPS server and Machine actors).
 *
 * @author TG Team
 *
 * @param <T>
 * @param <M>
 * @param <N>
 */
public abstract class ApplicationConfigurationUtil<T extends AbstractAvlMessage, M extends AbstractAvlMachine<T>, N extends AbstractAvlMachineActor<T, M>> {
    private final static Logger logger = Logger.getLogger(ApplicationConfigurationUtil.class);

    /** An utility class to start GPS server services (like Netty GPS server and Machine actors). */
    public final void startGpsServices(final Properties props, final Injector injector) {
	// get all vehicles with their latest GPS message
	// the resultant map is used by both the GPS message handler for updating and CurrentMachinesState resource for read
	// thus a concurrent map is used to synchronize read/write operations

	// create and start all actors responsible for message handling
	final AbstractActors<T, M, N> actors = createActors(fetchMachinesToTrack(injector));
	actors.startMachines(injector);

	final DefaultMachineMonitoringProvider<T, M, N> mmProvider = (DefaultMachineMonitoringProvider<T, M, N>) injector.getInstance(IMachineMonitoringProvider.class);
	mmProvider.setActors(actors);
    }

    /** Creates specific {@link AbstractActors} implementation. */
    protected abstract AbstractActors<T, M, N> createActors(final Map<String, M> machines);

    /** Fetches all machines from the database that should be used for message processing. */
    protected abstract Map<String, M> fetchMachinesToTrack(final Injector injector);

    protected Logger getLogger() {
	return logger;
    }
}
