package rest.server;

import java.net.URI;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;

import api.WalletResources;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class WalletServer {

	public static void main(String[] args) throws Exception {
		int port = 8090;
		if( args.length > 0)
			port = Integer.parseInt(args[0]);
		
		URI baseUri = UriBuilder.fromUri("https://0.0.0.0/").port(port).build();

		ResourceConfig config = new ResourceConfig();
		config.register( new WalletResources() );
		
		JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

		System.err.println(" Wallet Server ready @ " + baseUri);
	}
}
