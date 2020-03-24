package it.imolainformatica.bootique.camel.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import it.imolainformatica.bootique.camel.CamelFactory;
import org.apache.camel.CamelContext;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
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
        CamelContext camelContext = serverProvider.get();
        try {
            camelContext.start();
            logger.debug("Camel started");
        } catch (Exception e) {
            return CommandOutcome.failed(1, e);
        }
        logger.debug("conf requiresHttpTrasnsportServlet={}",camelFactory.isRequiresHttpTransportServlet());
        if (camelFactory.isRequiresHttpTransportServlet()) {
            startJetty(jettyProvider.get());
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


}
