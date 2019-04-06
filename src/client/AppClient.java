package client;

import api.Reply;
import security.Digest;
import security.PrivateKey;
import security.PublicKey;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class AppClient {

    //private static  URI baseURI;
    private static SecureRandom random = new SecureRandom();

    private static List<KeyPair> keys = new ArrayList<KeyPair>();

    public static void main(String[] args) throws Exception {
        addMoney();
        addMoney();
        addMoney();
        transferMoney();


    }


    public AppClient(){

    }





   //@Override
    @SuppressWarnings("Duplicates")
    public static void transferMoney() throws Exception {
        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);


        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
        kpg.initialize(1024);

        /*KeyPair kp = kpg.generateKeyPair() ;
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;
*/
        KeyPair kp = keys.get(random.nextInt(keys.size()));
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;
        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String pathPublicKey = URLEncoder.encode(publicString, "UTF-8");

        KeyPair kp2 = keys.get(random.nextInt(keys.size()));
        PublicKey pub2 = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv2 = new PrivateKey( "RSA", kp.getPrivate() ) ;
        String publicString2 = Base64.getEncoder().encodeToString(pub2.exportKey());
        String pathPublicKey2 = URLEncoder.encode(publicString2, "UTF-8");


        Double value = 0.0;
        Long nonce = random.nextLong();

        String msg = publicString + publicString2 + value + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);




        Response response = target.path("transfer/" + pathPublicKey).queryParam("tpublicKey", pathPublicKey2)
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

    @SuppressWarnings("Duplicates")
    public static void addMoney() throws Exception {


        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);

        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair() ;
        keys.add(kp);
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;

        /*String pubTry = "ab123";
        String privTry = "cd456";*/

        Double value = 0.0;

        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        Long nonce = random.nextLong();

        String msg = publicString + value + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);
        String pathPublicKey = URLEncoder.encode(publicString, "UTF-8");

        Response response = target.path(pathPublicKey)
                .queryParam("value", value)
                .queryParam("nonce", nonce)
                .queryParam("msg", msgHashStr)
                .request()
                .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));


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




