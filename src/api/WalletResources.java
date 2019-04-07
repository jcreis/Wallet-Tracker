package api;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import rest.server.CaptureMessages;
import rest.server.ReplicaServer;
import security.Digest;
import security.PrivateKey;
import security.PublicKey;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


@Path("/users")
public class WalletResources {

    int replicaNumber;

    ServiceProxy serviceProxy;

    ReplicaServer replicaServer;

    RSAKeyLoader keyLoader;

    CaptureMessages captureMessages = new CaptureMessages();

    SecureRandom random = new SecureRandom();

    public WalletResources(int replicaNumber) throws Exception {
        this.replicaNumber = replicaNumber;
        System.out.println("replica number " + replicaNumber);
        replicaServer = new ReplicaServer(replicaNumber);
        keyLoader = new RSAKeyLoader(replicaNumber, "config", false, "sha512WithRSAEncryption");
        serviceProxy = new ServiceProxy(replicaNumber, "config", null, captureMessages, keyLoader);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair() ;
        PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
        PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;


        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String privateString = Base64.getEncoder().encodeToString(priv.exportKey());


        File publicKey = new File("./publicKey.txt");
        File privateKey = new File("./privateKey.txt");



        FileWriter pb = new FileWriter(publicKey, false);
        FileWriter pr = new FileWriter(privateKey, false);
        pb.write(publicString);
        pr.write(privateString);
        pb.close();
        pr.close();

        //TODO: to test nonces comment the next 2 lines and add @Query nonce from methods bellow

    }

    public enum opType {
        TRANSFER,
        ADD_MONEY,
        GET_MONEY,
        ADD_USER
    }

    private Map<String, Double> db = new ConcurrentHashMap<String, Double>();

    @SuppressWarnings("Duplicates")
    @POST
    @Path("/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Reply addMoney(@PathParam("publicKey") String publicKey,
                          @QueryParam("value") Double value,
                          @QueryParam("nonce") Long nonce,
                          @QueryParam("msg") String msg) throws Exception {

        Long replyNonce;


        File file = new File("./publicKey.txt");
        String adminPublicString = null;
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine() ){
            adminPublicString = sc.next();
        }
        byte[] adminPublic = Base64.getDecoder().decode(adminPublicString);
        PublicKey adminPubKey = PublicKey.createKey(adminPublic);


        URLDecoder.decode(publicKey, "UTF-8");
        String verify = publicKey + value + nonce;
        byte[] hash = Digest.getDigest(verify.getBytes());

        //byte[] pubKeyArr = Base64.getDecoder().decode(publicKey);
        //PublicKey pub2 = PublicKey.createKey(pubKeyArr);

        byte[] decodedBytes = Base64.getDecoder().decode(msg);
        byte[] hashDecriptPriv = adminPubKey.decrypt(decodedBytes);






        if (Arrays.equals(hashDecriptPriv, hash)) {

            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                objOut.writeObject(opType.ADD_MONEY);
                objOut.writeObject(publicKey);
                objOut.writeObject(value);
                objOut.writeObject(nonce);

                objOut.flush();
                byteOut.flush();
                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    double money = (Double) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    System.out.println("RESPONSE FROM ADD MONEY IS:");
                    Reply r = new Reply(captureMessages.sendMessages(), publicKey, money, replyNonce + 1);
                    System.out.println(r);
                    return r;
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception putting value into map: " + e.getMessage());
            }
        }
        throw new NotAuthorizedException("Don't have permission to add money");

    }


    @SuppressWarnings("Duplicates")
    @POST
    @Path("/transfer/{fpublicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Reply transferMoney(@PathParam("fpublicKey") String fpublicKey, @QueryParam("tpublicKey") String tpublicKey, @QueryParam("value")
            Double value, @QueryParam("nonce") Long nonce, @QueryParam("msg") String msg) throws Exception {


        Long replyNonce;
        URLDecoder.decode(fpublicKey, "UTF-8");
        URLDecoder.decode(tpublicKey, "UTF-8");
        String verify = fpublicKey + tpublicKey + value + nonce;
        byte[] hash = Digest.getDigest(verify.getBytes());
        byte[] pubKeyArr = Base64.getDecoder().decode(fpublicKey);
        PublicKey pub2 = PublicKey.createKey(pubKeyArr);
        byte[] decodedBytes = Base64.getDecoder().decode(msg);
        byte[] hashDecriptPriv = pub2.decrypt(decodedBytes);

        if (Arrays.equals(hashDecriptPriv, hash)) {


            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                objOut.writeObject(opType.TRANSFER);
                objOut.writeObject(fpublicKey);
                objOut.writeObject(tpublicKey);
                objOut.writeObject(value);
                objOut.writeObject(nonce);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    double money = (Double) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    return new Reply(captureMessages.sendMessages(), fpublicKey, money, replyNonce + 1);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception putting value into map: " + e.getMessage());
            }
        }
        return null;


    }

    @SuppressWarnings("Duplicates")
    @GET
    @Path("/{publicKey}/money")
    @Produces(MediaType.APPLICATION_JSON)
    public Reply getMoney(@PathParam("publicKey") String publicKey, @QueryParam("nonce") Long nonce,
                          @QueryParam("msg") String msg) throws Exception {

        Long replyNonce;

        String verify = publicKey + nonce;
        URLDecoder.decode(publicKey, "UTF-8");
        byte[] hash = Digest.getDigest(verify.getBytes());

        byte[] pubKeyArr = Base64.getDecoder().decode(publicKey);
        PublicKey pub2 = PublicKey.createKey(pubKeyArr);

        byte[] decodedBytes = Base64.getDecoder().decode(msg);
        byte[] hashDecriptPriv = pub2.decrypt(decodedBytes);


        if (Arrays.equals(hashDecriptPriv, hash)) {

            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                objOut.writeObject(opType.GET_MONEY);
                objOut.writeObject(publicKey);
                objOut.writeObject(nonce);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    double money = (Double) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    return new Reply(captureMessages.sendMessages(), publicKey, money, replyNonce + 1);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception getting value from map: " + e.getMessage());
            }
        }
        throw new ForbiddenException();

    }


}
