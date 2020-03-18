package it.imolainformatica.bootique.camel;

import java.util.Collections;
import java.util.Set;

public class CamelFactory {
    private boolean requiresHttpTransportServlet =false;
    private Set<String> servletUrlPatterns;
    private String servletName="CamelServlet";
    private String contextName="/";

    public CamelFactory() {
        servletUrlPatterns=Collections.singleton("/*");
    }

    public boolean isRequiresHttpTransportServlet() {
        return requiresHttpTransportServlet;
    }

    public void setRequiresHttpTransportServlet(boolean requiresHttpTransportServlet) {
        this.requiresHttpTransportServlet = requiresHttpTransportServlet;
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

    public Set<String> getServletUrlPatterns() {
        return servletUrlPatterns;
    }

    public void setServletUrlPatterns(Set<String> servletUrlPatterns) {
        this.servletUrlPatterns = servletUrlPatterns;
    }
}
