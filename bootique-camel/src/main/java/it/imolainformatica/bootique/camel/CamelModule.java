package it.imolainformatica.bootique.camel;

import io.bootique.BQCoreModule;
import io.bootique.ConfigModule;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Binder;
import io.bootique.di.Provides;
import io.bootique.di.TypeLiteral;
import io.bootique.jetty.JettyModule;
import io.bootique.jetty.MappedServlet;
import io.bootique.log.BootLogger;
import io.bootique.shutdown.ShutdownManager;
import it.imolainformatica.bootique.camel.command.StartCommand;
import org.apache.camel.CamelContext;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.StartupListener;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.HashSet;
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
        JettyModule.extend(binder).addMappedServlet(new TypeLiteral<MappedServlet<CamelHttpTransportServlet>>() {
        });
        CamelModule.extend(binder)
                .initAllExtensions();

    }

    @Singleton
    @Provides
    CamelFactory comelConfig(ConfigurationFactory configFactory) {
        return config(CamelFactory.class, configFactory);
    }

    @Singleton
    @Provides
    CamelContext createCamelContext(Set<RoutesBuilder> routesBuilder,
                                    Set<StartupListener> startupListeners, Set<MappedEndpoint> mapperEndpoints,
                                    ConfigurationFactory configFactory,
                                    BootLogger bootLogger, ShutdownManager shutdownManager) {
        logger.debug("createCamelContext start");
        CamelContext camelContext= new DefaultCamelContext();
        CamelFactory camelFactory=config(CamelFactory.class,configFactory);
        camelContext.setAllowUseOriginalMessage(camelFactory.getAllowUseOriginalMessage());
        camelContext.setUseMDCLogging(camelFactory.getUseMDCLogging());
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

        return camelContext;
    }

    @Singleton
    @Provides
    MappedServlet<CamelHttpTransportServlet> camelHttpServlet(ConfigurationFactory configFactory) {
        CamelFactory camelFactory= config(CamelFactory.class, configFactory);
        if (camelFactory.isRequiresHttpTransportServlet()) {
            return new MappedServlet<>(new CamelHttpTransportServlet(), camelFactory.getServletUrlPatterns(),camelFactory.getServletName());
        } else {
            return new MappedServlet<>(new CamelHttpTransportServlet(),new HashSet<>(),camelFactory.getServletName());
        }
    }






}
