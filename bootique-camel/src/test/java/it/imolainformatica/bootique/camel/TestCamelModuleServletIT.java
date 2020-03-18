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

public class TestCamelModuleServletIT {

    private static final Logger logger= LoggerFactory.getLogger(TestCamelModuleServletIT.class.getName());

    @Rule
    public BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    private BQRuntime startApp(BQModule module) {
        BQRuntime runtime = testFactory.app("-s").args("--config=classpath:bootique-jetty.yml")
                .module(module)
                .createRuntime();
        runtime.run();

        return runtime;
    }

    @Test
    public void testContextWithServlet() throws Exception {
        final String CAMEL_ECHO="CamelEcho:";
        CamelContext context= new DefaultCamelContext();
        BQRuntime runtime=startApp(b ->CamelModule.extend(b).addRouteBuilder(new RouteBuilder() {
            public void configure() {
                from("servlet:echo?matchOnUriPrefix=true").process(new Processor() {
                    public void process(Exchange exchange) throws Exception {
                        String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
                        String bodyText=exchange.getIn().getBody(String.class);
                        String path = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                        path = path.substring(path.lastIndexOf("/"));
                        exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, contentType + "; charset=UTF-8");
                        exchange.getMessage().setHeader("PATH", path);
                        exchange.getMessage().setBody(CAMEL_ECHO + bodyText);
                    }
                });
            }
        }));
        CamelContext camelContext= runtime.getInstance(CamelContext.class);
        Assert.assertNotNull(camelContext);
        OkHttpClient client = new OkHttpClient();
        String message="Ciao";
        RequestBody body = RequestBody.create(message, MediaType.get("text/plain; charset=utf-8"));
        Request request = new Request.Builder()
                .url("http://localhost:8080/camel/echo")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(CAMEL_ECHO+message, response.body().string());
        }

    }
}
