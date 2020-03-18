package it.imolainformatica.bootique.camel;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.test.junit.BQTestFactory;
import okhttp3.*;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCamelModuleServletIT extends AbstractCamelModuleServletTest{

    private static final Logger logger= LoggerFactory.getLogger(TestCamelModuleServletIT.class.getName());

    @Override
    protected BQRuntime startApp(BQModule module) {
        BQRuntime runtime = testFactory.app("-s").args("--config=classpath:bootique-jetty.yml")
                .module(module)
                .createRuntime();
        runtime.run();

        return runtime;
    }

    @Test
    public void testContextWithServlet() throws Exception {
        BQRuntime runtime=getCamelRuntime();
        CamelContext camelContext= runtime.getInstance(CamelContext.class);
        Assert.assertNotNull(camelContext);
        testEchoAtUrl("http://localhost:8080/camel/echo");
        testEchoAtUrl("http://localhost:8080/services/echo");
    }
}
