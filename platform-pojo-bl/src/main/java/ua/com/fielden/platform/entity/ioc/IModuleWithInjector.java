package ua.com.fielden.platform.entity.ioc;

import com.google.inject.Injector;

/**
 * A contract to be implemented by IoC modules, which require an injector to be set after that injector was instantiated based on the module instance.
 * 
 * @author TG Team
 * 
 */
public interface IModuleWithInjector {
    /**
     * Should be implemented in order to provided interested parties with an injector.
     * 
     * @param injector
     */
    void setInjector(final Injector injector);
}
