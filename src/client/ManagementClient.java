package client;

import model.EncryptOpType_ADD;
import model.Reply;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

public class ManagementClient {

    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.trustStore", "client.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "qwerty");

        launch("8080", "0");
        launch("8090", "1");
        //stop("0");
        //stop("1");




    }


    @SuppressWarnings("Duplicates")
    public static void launch(String port, String id) throws Exception {

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new AppClient.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8099/wallet/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);

        Response response = target.path("launch/").queryParam("port", port)
                .queryParam("id", id)
                .request()
                .post(Entity.json(""));


    }


    @SuppressWarnings("Duplicates")
    public static void stop(String id) throws Exception {

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new AppClient.InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8099/wallet/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);

        Response response = target.path("stop/").queryParam("id", id)
                .request()
                .post(Entity.json(""));


    }





}
