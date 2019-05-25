package container;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class sconeServer {

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.keyStore","sgx.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","qwerty");

        int port = 8000;

        if( args.length > 0) {
            port = Integer.parseInt(args[0]);

        }
        System.out.println("Server running at port "+ port);
        URI baseUri = UriBuilder.fromUri("https://localhost/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register( new sconeApi() );

        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println(" Scone Server ready @ " + baseUri);
    }


}
