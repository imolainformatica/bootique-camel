package it.imolainformatica.bootique.camel;

public class CamelFactory {
    private boolean requiresHttpProcessor;

    public boolean isRequiresHttpProcessor() {
        return requiresHttpProcessor;
    }

    public void setRequiresHttpProcessor(boolean requiresHttpProcessor) {
        this.requiresHttpProcessor = requiresHttpProcessor;
    }
}
