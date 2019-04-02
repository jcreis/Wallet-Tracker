package rest.server;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RendezVousServer {

	public static void main(String[] args) throws Exception {
		int port = 8080;
		if( args.length > 0)
			port = Integer.parseInt(args[0]);
		
		URI baseUri = UriBuilder.fromUri("http://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new RendezVousResources() );
		
		JdkHttpServerFactory.createHttpServer(baseUri, config);

		System.err.println("REST RendezVous Server ready @ " + baseUri);
	}
}
