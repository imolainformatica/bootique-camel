package it.imolainformatica.bootique.camel;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import org.apache.camel.CamelContext;
import org.junit.Assert;
import org.junit.Test;

public class TestCamelModuleServletIT extends AbstractCamelModuleServletTest{


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
