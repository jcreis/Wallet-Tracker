package client;

import java.net.URI;
import java.net.URL;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.*;
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


        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
        kpg.initialize(1024);
        java.security.KeyPair kp = kpg.generateKeyPair() ;
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;
        String pubTry = "ab123";
        String privTry = "cd456";




        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);
      /*  URL url = new URL("https://localhost:8080/users");
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();*/







        Double value = 0.0;

        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());

        Long nonce = random.nextLong();

        String msg = publicString + value + nonce;

        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);

        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);


        /*con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");
        */Response response = target.path(pubTry)
                .queryParam("value", value)
                .queryParam("nonce", nonce)
                .queryParam("msg", msgHashStr)
                .request()
                .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));

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




