package it.imolainformatica.bootique.camel;

import io.bootique.ModuleExtender;
import io.bootique.di.Binder;
import io.bootique.di.SetBuilder;
import org.apache.camel.Endpoint;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.StartupListener;

public class CamelModuleExtender extends ModuleExtender<CamelModuleExtender> {


    private SetBuilder<RoutesBuilder> routesBuilder;
    private SetBuilder<StartupListener> startupListeners;
    private SetBuilder<MappedEndpoint> mapperEndpoints;



    public CamelModuleExtender(Binder binder) {
        super(binder);
    }

    @Override
    public CamelModuleExtender initAllExtensions() {
        contributeRouteBuilders();
        contributeStartupListeners();
        contributeMappedEndpoints();
        return this;
    }

    public CamelModuleExtender addRouteBuilder(RoutesBuilder routeBuilder) {
        contributeRouteBuilders().add(routeBuilder);
        return this;
    }

    public CamelModuleExtender addRouteBuilder(Class<? extends RoutesBuilder> routeBuilderType) {
        contributeRouteBuilders().add(routeBuilderType);
        return this;
    }

    public CamelModuleExtender addStartupListener(StartupListener startupListener) {
        contributeStartupListeners().add(startupListener);
        return this;
    }

    public CamelModuleExtender addStartupListener(Class<? extends StartupListener> startupListenerType) {
        contributeStartupListeners().add(startupListenerType);
        return this;
    }

    public CamelModuleExtender addMappedEndpoint(String  uri, Endpoint endpoint) {
        contributeMappedEndpoints().add(new MappedEndpoint(uri,endpoint));
        return this;
    }

    protected SetBuilder<RoutesBuilder> contributeRouteBuilders() {
        return routesBuilder != null ? routesBuilder : (routesBuilder = newSet(RoutesBuilder.class));
    }


    protected SetBuilder<StartupListener> contributeStartupListeners() {
        return startupListeners != null ? startupListeners : (startupListeners = newSet(StartupListener.class));
    }

    protected SetBuilder<MappedEndpoint> contributeMappedEndpoints() {
        return mapperEndpoints != null ? mapperEndpoints : (mapperEndpoints = newSet(MappedEndpoint.class));
    }
}
