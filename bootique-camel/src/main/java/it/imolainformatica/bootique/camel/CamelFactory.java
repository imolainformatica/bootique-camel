package it.imolainformatica.bootique.camel;

import java.util.Collections;
import java.util.Set;

public class CamelFactory {
    private boolean requiresHttpTransportServlet =false;
    private Set<String> servletUrlPatterns;
    private String servletName="CamelServlet";
    private Boolean useMDCLogging;
    private Boolean allowUseOriginalMessage;

    public CamelFactory() {
        servletUrlPatterns=Collections.singleton("/*");
    }

    public boolean isRequiresHttpTransportServlet() {
        return requiresHttpTransportServlet;
    }

    public void setRequiresHttpTransportServlet(boolean requiresHttpTransportServlet) {
        this.requiresHttpTransportServlet = requiresHttpTransportServlet;
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

    public Boolean getUseMDCLogging() {
        return useMDCLogging;
    }

    public void setUseMDCLogging(Boolean useMDCLogging) {
        this.useMDCLogging = useMDCLogging;
    }

    public Boolean getAllowUseOriginalMessage() {
        return allowUseOriginalMessage;
    }

    public void setAllowUseOriginalMessage(Boolean allowUseOriginalMessage) {
        this.allowUseOriginalMessage = allowUseOriginalMessage;
    }
}
