package it.imolainformatica.bootique.camel;

import org.apache.camel.Endpoint;

public class MappedEndpoint {
    private final String uri;
    private final Endpoint endpoint;

    public MappedEndpoint(String uri, Endpoint endpoint) {
        this.uri = uri;
        this.endpoint = endpoint;
    }

    public String getUri() {
        return uri;
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

}
