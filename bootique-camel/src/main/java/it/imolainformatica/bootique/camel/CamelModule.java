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
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
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
                                    Provider<Server> jettyProvider,
                                    BootLogger bootLogger, ShutdownManager shutdownManager) {
        logger.debug("createCamelContext start");
        CamelFactory camelFactory=config(CamelFactory.class, configFactory);
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

        logger.debug("conf requiresHttpTrasnsportServlet={}",camelFactory.isRequiresHttpTransportServlet());
        if (camelFactory.isRequiresHttpTransportServlet()) {
            Server jettyServer=jettyProvider.get();
            configureJetty(jettyServer, camelFactory);
            startJetty(jettyServer);
        }
        return camelContext;
    }

    private void configureJetty(Server jettyServer, CamelFactory camelFactory) {
        logger.debug("conf contextName={}",camelFactory.getContextName());
        Handler handler=jettyServer.getHandler();
        ServletContextHandler servletHandler=null;
        boolean multipleHandlers=false;
        if (handler!=null) {
            if (handler instanceof ServletContextHandler) {
                logger.debug("a jetty handler already exists");
                ServletContextHandler existingServletHandler=(ServletContextHandler)handler;
                if (canReuseHandler(existingServletHandler, camelFactory.getContextName())) {
                    logger.debug("reusing existing servletHandler");
                    servletHandler=existingServletHandler;
                    if (existingServletHandler.getServletHandler()!=null && existingServletHandler.getServletHandler().getServletMappings()!=null) {
                        ServletMapping[] servletMappings = existingServletHandler.getServletHandler().getServletMappings();
                        for (ServletMapping servletMapping : servletMappings) {
                            camelFactory.getServletUrlPatterns().forEach(servletPath -> {
                                if (servletMapping.containsPathSpec(servletPath)) {
                                    logger.warn("Camel servlet is overriding an existing servlet path {}", servletMapping.toString());
                                }
                            });
                        }
                    }
                } else {
                    multipleHandlers=true;
                }
            } else {
                throw new RuntimeException("unable to configure Jetty for Camel jetty.handler is not instance of ServletContextHandler");
            }
        }
        if (servletHandler==null) {
            logger.debug("creating a new servletHandler");
            int options = 0;
            boolean sessions=true;
            if (sessions) {
                options |= ServletContextHandler.SESSIONS;
            }
            servletHandler= new ServletContextHandler(options);
            servletHandler.setContextPath(camelFactory.getContextName());
            servletHandler.setCompactPath(false);
        }

        ServletHolder holder = new ServletHolder(camelFactory.getServletName(), CamelHttpTransportServlet.class);
        ServletContextHandler servletHandlerLocal=servletHandler;
        camelFactory.getServletUrlPatterns().forEach(servletUrlPattern -> servletHandlerLocal.addServlet(holder, servletUrlPattern));

        logger.debug("adding Handler to Jetty");
        if (multipleHandlers) {
            ContextHandlerCollection contexts= new ContextHandlerCollection((ContextHandler) handler, servletHandler);
            jettyServer.setHandler(contexts);
        } else {
            jettyServer.setHandler(servletHandler);
        }
    }

    private boolean canReuseHandler(ServletContextHandler existingServletHandler, String contextName) {
        String existingContextPath=existingServletHandler.getContextPath();
        if (existingContextPath==null) {
            existingContextPath="";
        }
        if (contextName==null) {
            contextName="";
        }
        logger.debug("existingContextPath={}, contextName={}", existingContextPath, contextName);
        return (existingContextPath!=null && contextName!=null &&
                contextName.equals(existingContextPath));

    }

    public void startJetty(Server jettyServer) {
        try {
            // this blocks until a successful start or an error, then releases current thread, while Jetty
            // stays running on the background
            jettyServer.start();
        } catch (Exception e) {
            throw  new RuntimeException(e.getMessage(), e);
        }
    }
}
