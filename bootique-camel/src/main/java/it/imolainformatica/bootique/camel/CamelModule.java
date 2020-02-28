package it.imolainformatica.bootique.camel;

import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import it.imolainformatica.bootique.camel.command.StartCommand;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Set;

public class CamelModule extends ConfigModule {
    private static final Logger logger= LoggerFactory.getLogger(CamelModule.class.getName());

    public static CamelModuleExtender extend(Binder binder) {
        return new CamelModuleExtender(binder);
    }

    @Override
    public void configure(Binder binder) {
        BQCoreModule.extend(binder)
                .addCommand(StartCommand.class);

        CamelModule.extend(binder)
                .initAllExtensions();

    }

    @Singleton
    @Provides
    CamelContext createCamelContext(Set<RoutesBuilder> routesBuilder,
                                    Set<StartupListener> startupListeners, Set<MappedEndpoint> mapperEndpoints,
                                    ConfigurationFactory configFactory,
                                    BootLogger bootLogger, ShutdownManager shutdownManager) {
        logger.debug("createCamelContext start");
        CamelFactory camelFactory=config(CamelFactory.class, configFactory);
        logger.debug("conf httpRequired={}",camelFactory.isRequiresHttpProcessor());
        CamelContext camelContext= new DefaultCamelContext();
        startupListeners.forEach( startupListener -> {
            try {
                camelContext.addStartupListener(startupListener);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        mapperEndpoints.forEach(mappedEndpoint -> {
            try {
                camelContext.addEndpoint(mappedEndpoint.getUri(), mappedEndpoint.getEndpoint());
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        routesBuilder.forEach(routeBuilder -> {
            try {
                camelContext.addRoutes(routeBuilder);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        });
        shutdownManager.addShutdownHook(() -> {
            bootLogger.trace(() -> "stopping CamelContext...");
            camelContext.stop();
        });

        if (camelFactory.isRequiresHttpProcessor()) {

        }
        return camelContext;
    }
}
