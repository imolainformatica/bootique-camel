package it.imolainformatica.bootique.camel;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import org.apache.camel.CamelContext;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCamelModuleServletWithContextNameIT extends AbstractCamelModuleServletTest{

    private static final Logger logger= LoggerFactory.getLogger(TestCamelModuleServletWithContextNameIT.class.getName());

    @Override
    protected BQRuntime startApp(BQModule module) {
        BQRuntime runtime = testFactory.app("-s").args("--config=classpath:bootique-jetty-context.yml")
                .module(module)
                .createRuntime();
        runtime.run();

        return runtime;
    }

    @Test
    public void testContextWithServletAndContextName() throws Exception {
        BQRuntime runtime=getCamelRuntime();
        CamelContext camelContext= runtime.getInstance(CamelContext.class);
        Assert.assertNotNull(camelContext);
        testEchoAtUrl("http://localhost:8080/camelcontext/camel/echo");
        testEchoAtUrl("http://localhost:8080/camelcontext/services/echo");
    }
}
