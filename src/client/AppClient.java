package client;

import model.OpType;
import model.ReplicaResponseMessage;

import model.Reply;
import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;
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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Signature;
import java.util.*;

public class AppClient {

    //private static  URI baseURI;
    private static SecureRandom random = new SecureRandom();

    private static List<KeyPair> keys = new ArrayList<KeyPair>();
    public static void main(String[] args) throws Exception {

        boolean timeout = false;
        // Makes addMoney() every 2s
        Timer t = new Timer();
        TimerTask addMoneyThread = new TimerTask() {
            @Override
            public void run() {
                try {
                    addMoney();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.schedule(addMoneyThread, 0L, 2000L);

    }


    public AppClient() {

    }


    @SuppressWarnings("Duplicates")
    public static void transferMoney() throws Exception {
        System.out.println("#################################");
        System.out.println("####### T R A N S F E R #########");
        System.out.println("#################################");
        System.out.println();

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);


        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);


        KeyPair kp = keys.get(random.nextInt(keys.size()));

        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());
        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String pathPublicKey = URLEncoder.encode(publicString, "UTF-8");

        KeyPair kp2 = keys.get(random.nextInt(keys.size()));
        if(kp2.getPublic().equals(kp.getPublic())){
            kp2 = keys.get(random.nextInt(keys.size()));
        }
        //kp = keys.get(random.nextInt(keys.size()));
        PublicKey pub2 = new PublicKey("RSA", kp2.getPublic());
        PrivateKey priv2 = new PrivateKey("RSA", kp2.getPrivate());
        String publicString2 = Base64.getEncoder().encodeToString(pub2.exportKey());
        String pathPublicKey2 = URLEncoder.encode(publicString2, "UTF-8");


        Double value = 1.0;
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

        Reply r = response.readEntity(Reply.class);

        for (int i = 0; i < r.getMessages().size(); i++) {
            ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);

            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(currentReplicaMsg.getSerializedMessage());


            if (sig.verify(currentReplicaMsg.getSignature())) {
                System.out.println("Replica message coming from replica "+currentReplicaMsg.getSender()+" is authentic");
            } else {
                System.out.println("Signature of message is invalid");
            }

        }

        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.TRANSFER) {
            System.out.println("Nonces dont match, reject message from server");
        } else {

            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("From pubKey: " + publicString.substring(0,50));
            System.out.println("To pubKey: " + publicString2.substring(0,50));
            System.out.println("Transferring amount : " + value);
            System.out.println(publicString2.substring(0,50) + " now has " + r.getAmount());
            if(nonce+1 == r.getNonce()){
                System.out.println("Nonces match");
            }
            System.out.println();

        }
    }

    @SuppressWarnings("Duplicates")
    public static void addMoneyWNoPermission() throws Exception {

        System.out.println("#############################################");
        System.out.println("####### A D D - M O N E Y (NOT ADMIN) #######");
        System.out.println("#############################################");
        System.out.println();

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);

        // TODO generate random public/private key
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        //keys.add(kp);
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());


        Double value = 50.5;

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

        if (response.getStatus() == 401) {
            System.out.println("Sorry, you dont have permissions to add Money");
            System.out.println();
        } else {
            Reply r = response.readEntity(Reply.class);
            for (int i = 0; i < r.getMessages().size(); i++) {

                ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);


                KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
                java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
                Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
                sig.initVerify(pk);
                sig.update(currentReplicaMsg.getSerializedMessage());
                if (sig.verify(currentReplicaMsg.getSignature())) {
                    System.out.println("Replica message coming from replica "+currentReplicaMsg.getSender()+" is authentic");
                } else {
                    System.out.println("Signature of message is invalid");
                }

            }

            // Check if response nonce(which is nonce+1) is equals to original nonce + 1
            if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.ADD_MONEY) {
                System.out.println("Nonces dont match, reject message from server");
            } else {


                System.out.println();
                System.out.println("Status: " + response.getStatusInfo());
                System.out.println("From pubKey: " + publicString.substring(0,50));
                System.out.println("To pubKey: " + r.getPublicKey().substring(0,50));
                System.out.println("New amount: " + r.getAmount());
                if(nonce+1 == r.getNonce()){
                    System.out.println("Nonces match");
                }
                System.out.println();


            }
        }
    }

    @SuppressWarnings("Duplicates")
    public static void addMoney() throws Exception {
        System.out.println("#################################");
        System.out.println("####### A D D - M O N E Y #######");
        System.out.println("#################################");
        System.out.println();

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);


        Double value = 50.5;


        File file = new File("./publicKey.txt");
        File file2 = new File("./privateKey.txt");
        String adminPublicString = null;
        String adminPrivateString = null;

        Scanner sc = new Scanner(file);
        Scanner sc2 = new Scanner(file2);

        while (sc.hasNextLine() && sc2.hasNextLine()) {
            adminPublicString = sc.next();
            adminPrivateString = sc2.next();
        }


        /*System.out.println("AdminPublicKey : " + adminPublicString.substring(0,50));
        System.out.println("AdminPrivateKey : " + adminPrivateString.substring(0,50));*/

        byte[] pubByte = Base64.getDecoder().decode(adminPublicString);
        PublicKey adminPub = PublicKey.createKey(pubByte);
        byte[] privByte = Base64.getDecoder().decode(adminPrivateString);
        PrivateKey adminPriv = PrivateKey.createKey(privByte);


        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        keys.add(kp);
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());

        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());


        Long nonce = random.nextLong();

        String msg = publicString + value + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = adminPriv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);
        String pathPublicKey = URLEncoder.encode(publicString, "UTF-8");

        Response response = target.path(pathPublicKey)
                .queryParam("value", value)
                .queryParam("nonce", nonce)
                .queryParam("msg", msgHashStr)
                .request()
                .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));

        Reply r = response.readEntity(Reply.class);

        // TODO

        for (int i = 0; i < r.getMessages().size(); i++){
            ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(currentReplicaMsg.getContent());
            ObjectInput objIn = new ObjectInputStream(byteIn);
            Double replicaMsgAmount = (Double) objIn.readObject();
            System.out.println("replica amount: "+ replicaMsgAmount);
            Long replicaNonce = (Long) objIn.readObject();
            System.out.println("replica nonce: " + replicaNonce);
            //TODO FAZER POS OUTROS METODOS

            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(currentReplicaMsg.getSerializedMessage());
            if(sig.verify(currentReplicaMsg.getSignature())){
                System.out.println("Replica message coming from replica "+currentReplicaMsg.getSender()+" is authentic");
            }
            else{

                System.out.println("Signature of message is invalid");
            }

        }


        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.ADD_MONEY) {
            System.out.println("Nonces dont match, reject message from server");
        } else {

            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("Add money to pubKey: " + publicString.substring(0,50));
            System.out.println("Amount: " + r.getAmount());
            if(nonce+1 == r.getNonce()){
                System.out.println("Nonces match");
            }
            System.out.println();


        }
    }

    @SuppressWarnings("Duplicates")
    public static void getMoney() throws Exception {
        System.out.println("#################################");
        System.out.println("####### G E T - M O N E Y #######");
        System.out.println("#################################");
        System.out.println();

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);

        KeyPair kp = keys.get(random.nextInt(keys.size()));
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());
        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String pathPublicKey = URLEncoder.encode(publicString, "UTF-8");


        Long nonce = random.nextLong();

        String msg = publicString + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);

        Response response = target.path(pathPublicKey + "/money")
                .queryParam("nonce", nonce)
                .queryParam("msg", msgHashStr)
                .request()
                .get();
        Reply r = response.readEntity(Reply.class);

        for (int i = 0; i < r.getMessages().size(); i++) {
            ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);

            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(currentReplicaMsg.getSerializedMessage());
            if (sig.verify(currentReplicaMsg.getSignature())) {
                System.out.println("Replica message coming from replica "+currentReplicaMsg.getSender()+" is authentic");
            } else {
                System.out.println("Signature of message is invalid");
            }

        }

        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.GET_MONEY) {
            System.out.println("Nonces dont match, reject message from server");
        } else {

            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("From pubKey: " + publicString.substring(0,50));
            System.out.println("New amount: " + r.getAmount());
            if(nonce+1 == r.getNonce()){
                System.out.println("Nonces match");
            }
            System.out.println();

        }
    }


    /* READS CORRESPONDENT PUBKEY FROM config/keys

    private static String readFromFile(Integer replicaID){
        try {
            BufferedReader br = new BufferedReader(new FileReader("./config/keys/publickey"+replicaID));
            StringBuilder b = new StringBuilder();


                String line = br.readLine();
                while (line != null) {
                    b.append(line);
                    line = br.readLine();
                }

            System.out.println(b.toString());
            return b.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
*/
    static public class InsecureHostnameVerifier implements HostnameVerifier{

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }


}
