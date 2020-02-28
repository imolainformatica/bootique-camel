package it.imolainformatica.bootique.camel.command;

import io.bootique.cli.Cli;
import io.bootique.command.CommandOutcome;
import io.bootique.command.CommandWithMetadata;
import io.bootique.meta.application.CommandMetadata;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class StartCommand extends CommandWithMetadata {

    private static final Logger logger= LoggerFactory.getLogger(StartCommand.class.getName());

    private Provider<CamelContext> serverProvider;

    @Inject
    public StartCommand(Provider<CamelContext> serverProvider) {
        super(createMetadata());
        this.serverProvider = serverProvider;
    }

    private static CommandMetadata createMetadata() {
        return CommandMetadata.builder(StartCommand.class).description("Starts Camel Context..").build();
    }

    @Override
    public CommandOutcome run(Cli cli) {
        logger.debug("CommandOutcome run");
        CamelContext server = serverProvider.get();
        try {
            // this blocks until a successful start or an error, then releases current thread, while Jetty
            // stays running on the background
            server.start();
            logger.debug("Camel started");
        } catch (Exception e) {
            return CommandOutcome.failed(1, e);
        }

        return CommandOutcome.succeededAndForkedToBackground();
    }


}
