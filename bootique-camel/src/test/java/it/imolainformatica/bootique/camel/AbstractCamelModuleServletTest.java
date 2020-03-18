package it.imolainformatica.bootique.camel;

import io.bootique.BQRuntime;
import io.bootique.di.BQModule;
import io.bootique.test.junit.BQTestFactory;
import okhttp3.*;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Assert;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCamelModuleServletTest {

    private static final Logger logger= LoggerFactory.getLogger(AbstractCamelModuleServletTest.class.getName());

    private static final String CAMEL_ECHO="CamelEcho:";

    @Rule
    public final BQTestFactory testFactory = new BQTestFactory().autoLoadModules();

    protected abstract BQRuntime startApp(BQModule module);

    protected BQRuntime getCamelRuntime() {
        return startApp(b ->CamelModule.extend(b).addRouteBuilder(new RouteBuilder() {
            public void configure() {
                from("servlet:echo?matchOnUriPrefix=true").process(exchange -> {
                    String contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
                    String bodyText=exchange.getIn().getBody(String.class);
                    String path = exchange.getIn().getHeader(Exchange.HTTP_URI, String.class);
                    path = path.substring(path.lastIndexOf("/"));
                    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, contentType + "; charset=UTF-8");
                    exchange.getMessage().setHeader("PATH", path);
                    exchange.getMessage().setBody(CAMEL_ECHO + bodyText);
                });
            }
        }));
    }



    protected void testEchoAtUrl(String url) throws Exception {
        logger.debug("Testing Echo at url {}", url);
        OkHttpClient client = new OkHttpClient();
        String message="Ciao";
        RequestBody body = RequestBody.create(message, MediaType.get("text/plain; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            Assert.assertEquals(CAMEL_ECHO+message, response.body().string());
        }
    }
}
