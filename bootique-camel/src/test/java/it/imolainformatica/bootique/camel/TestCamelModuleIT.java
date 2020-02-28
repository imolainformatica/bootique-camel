package it.imolainformatica.bootique.camel;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.test.junit.BQTestFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class TestCamelModuleIT {
    @Rule
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    private BQRuntime startApp(BQModule module) {
        BQRuntime runtime = testFactory.app("-s").args("--config=classpath:bootique.yml")
                .module(module)
                .createRuntime();
        runtime.run();

        return runtime;
    }

    @Test
    public void testSimpleBuilder() throws InterruptedException {
        CamelContext context= new DefaultCamelContext();
        BQRuntime runtime=startApp(b ->CamelModule.extend(b).addRouteBuilder(new RouteBuilder() {
            public void configure() {
                from("direct:start").streamCaching().to("mock:result");
            }
        }));
        CamelContext camelContext= runtime.getInstance(CamelContext.class);
        Assert.assertNotNull(camelContext);
        ProducerTemplate producerTemplate=camelContext.createProducerTemplate();

        MockEndpoint mockEndpoint = (MockEndpoint) camelContext.getEndpoint("mock:result");
        producerTemplate.sendBody("direct:start","ciao");
        mockEndpoint.setExpectedMessageCount(1);
        mockEndpoint.assertIsSatisfied();
    }
}
