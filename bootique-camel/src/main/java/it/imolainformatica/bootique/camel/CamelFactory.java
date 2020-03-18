package it.imolainformatica.bootique.camel;

public class CamelFactory {
    private boolean requiresHttpProcessor=false;
    private String servletMapping="/*";
    private String servletName="CamelServlet";
    private String contextName="";

    public boolean isRequiresHttpProcessor() {
        return requiresHttpProcessor;
    }

    public void setRequiresHttpProcessor(boolean requiresHttpProcessor) {
        this.requiresHttpProcessor = requiresHttpProcessor;
    }

    public String getServletMapping() {
        return servletMapping;
    }

    public void setServletMapping(String servletMapping) {
        this.servletMapping = servletMapping;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }
}
