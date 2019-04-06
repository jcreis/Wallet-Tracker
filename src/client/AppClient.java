package client;

import java.net.URI;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import api.Reply;
import api.WalletResources;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import security.Digest;
import security.PrivateKey;
import security.PublicKey;

public class AppClient {

    //private static  URI baseURI;
    private static SecureRandom random = new SecureRandom();

    public static void main(String[] args) throws Exception {
        addMoney();
    }


    public AppClient(){

    }





   //@Override
    public void transferMoney() throws Exception {
        Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();
        URI baseURI = UriBuilder.fromUri("https://" + "0.0.0.0" + "/").build();
        WebTarget target = client.target(baseURI);


        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
        kpg.initialize(1024);
        java.security.KeyPair kp = kpg.generateKeyPair() ;
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;



        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String privateString = Base64.getEncoder().encodeToString(priv.exportKey());





        Response response3 = target.path("users/transfer" + publicString).queryParam("toPublicKey", publicString)
                .request()
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
        if (response3.getStatusInfo().equals(Response.Status.NO_CONTENT)) {
            System.out.println("deleted block resource...");
        } else
            System.err.println(Response.Status.NO_CONTENT);
    }

    public static void addMoney() throws Exception {

        Client client = ClientBuilder.newBuilder().hostnameVerifier(new InsecureHostnameVerifier()).build();
        URI baseURI = UriBuilder.fromUri("https://" + "localhost:8080" + "/").build();
        WebTarget target = client.target(baseURI);

        System.out.println("URI: " + baseURI);


        Double value = 0.0;


        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
        kpg.initialize(1024);
        java.security.KeyPair kp = kpg.generateKeyPair() ;
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;



        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());

        Long nonce = random.nextLong();

        String msg = publicString + value + nonce;

        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);


        String msgHash = Base64.getEncoder().encodeToString(hashEncriptPriv);



        Response response = target.path("users/" + publicString)
                .queryParam("value", value)
                .queryParam("nonce", nonce)
                .queryParam("msg", msgHash)
                .request()
                .post(Entity.entity("", MediaType.APPLICATION_JSON));

        System.out.println(response.getStatusInfo());
        if (response.hasEntity()) {
            System.out.println(response.readEntity(Reply.class));
        }
    }

    static public class InsecureHostnameVerifier implements HostnameVerifier{

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }
}




