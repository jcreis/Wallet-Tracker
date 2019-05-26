package rest.server;

import api.LoaderResources;
import api.WalletResources;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

public class LoaderServer {


    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.keyStore","server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","qwerty");

        int port = 8099;
        int replicaNum = 0;
        if( args.length > 0) {
            port = Integer.parseInt(args[0]);
            replicaNum = Integer.parseInt(args[1]);
        }

        URI baseUri = UriBuilder.fromUri("https://localhost/").port(port).build();

        ResourceConfig config = new ResourceConfig();
        config.register( new LoaderResources() );
        JdkHttpServerFactory.createHttpServer(baseUri, config, SSLContext.getDefault());

        System.err.println(" Loader Server ready @ " + baseUri);
    }



}










