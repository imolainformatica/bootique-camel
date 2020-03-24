package it.imolainformatica.bootique.camel.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.config.ConfigurationFactory;
import io.bootique.di.Provides;
import io.bootique.meta.application.CommandMetadata;
import it.imolainformatica.bootique.camel.CamelFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class StartCommand extends CommandWithMetadata {

    private static final Logger logger= LoggerFactory.getLogger(StartCommand.class.getName());

    private final Provider<CamelContext> serverProvider;
    private final Provider<CamelFactory> factoryProvider;
    private final Provider<Server> jettyProvider;

    @Inject
    public StartCommand(Provider<CamelContext> serverProvider,Provider<CamelFactory> factoryProvider, Provider<Server> jettyProvider) {
        super(createMetadata());
        this.serverProvider = serverProvider;
        this.factoryProvider = factoryProvider;
        this.jettyProvider = jettyProvider;
    }


    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(StartCommand.class).description("Starts Camel Context..").build();
    }

    @Override
    public CommandOutcome run(Cli cli) {
        CamelFactory camelFactory=factoryProvider.get();
        logger.debug("CommandOutcome run requiresHttpProcessor={} ",camelFactory.isRequiresHttpTransportServlet());
        CamelContext server = serverProvider.get();
        try {
            // this blocks until a successful start or an error, then releases current thread, while Jetty
            // stays running on the background
            server.start();
            logger.debug("Camel started");
        } catch (Exception e) {
            return CommandOutcome.failed(1, e);
        }
        logger.debug("conf requiresHttpTrasnsportServlet={}",camelFactory.isRequiresHttpTransportServlet());
        if (factoryProvider.get().isRequiresHttpTransportServlet()) {
            Server jettyServer=jettyProvider.get();
            //configureJetty(jettyServer, camelFactory);
            startJetty(jettyServer);
        }

        return CommandOutcome.succeededAndForkedToBackground();
    }

    private void startJetty(Server jettyServer) {
        try {
            // this blocks until a successful start or an error, then releases current thread, while Jetty
            // stays running on the background
            jettyServer.start();
        } catch (Exception e) {
            throw  new RuntimeException(e.getMessage(), e);
        }
    }

/*    private void configureJetty(Server jettyServer, CamelFactory camelFactory) {
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
            servletHandler= new ServletContextHandler(ServletContextHandler.SESSIONS);
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
    }*/

    private boolean canReuseHandler(ServletContextHandler existingServletHandler, String contextName) {
        String existingContextPath=existingServletHandler.getContextPath();
        if (existingContextPath==null) {
            existingContextPath="";
        }
        if (contextName==null) {
            contextName="";
        }
        logger.debug("existingContextPath={}, contextName={}", existingContextPath, contextName);
        return (contextName.equals(existingContextPath));

    }


}
