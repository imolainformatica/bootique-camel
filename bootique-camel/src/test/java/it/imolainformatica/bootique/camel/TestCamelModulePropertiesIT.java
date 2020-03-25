package it.imolainformatica.bootique.camel;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.test.junit.BQTestFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class TestCamelModulePropertiesIT {
    @Rule
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    private BQRuntime startApp(BQModule module) {
        BQRuntime runtime = testFactory.app("-s").args("--config=classpath:bootique-props.yml")
                .module(module)
                .createRuntime();
        runtime.run();

        return runtime;
    }

    @Test
    public void testProperties() throws InterruptedException {
        BQRuntime runtime=startApp(b ->CamelModule.extend(b).addRouteBuilder(new RouteBuilder() {
            public void configure() {
                from("direct:start").streamCaching().to("mock:result");
            }
        }));
        CamelContext camelContext= runtime.getInstance(CamelContext.class);
        Assert.assertTrue(camelContext.isAllowUseOriginalMessage());
        Assert.assertTrue(camelContext.isUseMDCLogging());

    }
}
