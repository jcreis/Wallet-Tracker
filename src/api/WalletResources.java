package api;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import model.CaptureMessages;
import model.Reply;
import model.ReplyCondUpd_Client;
import model.Reply_OPE;
import rest.server.ReplicaServer;
import security.Digest;
import security.PrivateKey;
import security.PublicKey;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.*;

import static model.OpType.*;


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
        replicaServer = new ReplicaServer(replicaNumber);
        keyLoader = new RSAKeyLoader(replicaNumber, "config", false, "sha512WithRSAEncryption");
        serviceProxy = new ServiceProxy(replicaNumber, "config", null, captureMessages, keyLoader);

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());


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


    }

    public WalletResources(){


    }


    @SuppressWarnings("Duplicates")
    @POST
    @Path("/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Reply addMoney(@PathParam("publicKey") String publicKey,
                          @QueryParam("value") String value,
                          @QueryParam("nonce") Long nonce,
                          @QueryParam("msg") String msg,
                          @QueryParam("type") String type,
                          @QueryParam("encryptType") String encryptType,
                          @QueryParam("nSquare") BigInteger nSquare,
                          @QueryParam("homoAddKey") String homo_add_Key,
                          @QueryParam("aesKey") String encodedAESKey,
                          @QueryParam("homoOpeIntKey") String homo_ope_int_Key)

            throws Exception {

        System.out.println("Server received value (encrypted) "+value);

        Long replyNonce;

        // Reads admin pubKey
        File file = new File("./publicKey.txt");
        String adminPublicString = null;
        Scanner sc = new Scanner(file);
        while (sc.hasNextLine()) {
            adminPublicString = sc.next();
        }
        byte[] adminPublic = Base64.getDecoder().decode(adminPublicString);
        PublicKey adminPubKey = PublicKey.createKey(adminPublic);
        // Prepares Hash of message H(N), N = (pubKey, value, nonce)
        URLDecoder.decode(publicKey, "UTF-8");
        String verify = publicKey + value + nonce;
        byte[] hash = Digest.getDigest(verify.getBytes());

        // UnHash H(N)
        byte[] decodedBytes = Base64.getDecoder().decode(msg);
        byte[] hashDecriptPriv = adminPubKey.decrypt(decodedBytes);

        // Checks if Hashes match
        if (Arrays.equals(hashDecriptPriv, hash)) {
            System.out.println("hashes match!! ");

            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                System.out.println("Gonna send value "+value+" to replica");
                objOut.writeObject(ADD_MONEY);
                objOut.writeObject(publicKey);
                objOut.writeObject(value);
                objOut.writeObject(nonce);
                objOut.writeObject(type);
                objOut.writeObject(encryptType);
                if(type.equals("HOMO_ADD")){
                    objOut.writeObject(nSquare);
                    objOut.writeObject(homo_add_Key);
                    objOut.writeObject(encodedAESKey);
                }else if(type.equals("HOMO_OPE_INT")){
                    objOut.writeObject(homo_ope_int_Key);
                    objOut.writeObject(encodedAESKey);
                }


                objOut.flush();
                byteOut.flush();
                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());

                if (reply.length == 0)
                    return null;

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {

                    String money = (String) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    System.out.println("Recebi o valor "+ money +" da replica.");
                    Reply r = new Reply(ADD_MONEY, captureMessages.getReplicaMessages(), publicKey, money, replyNonce + 1);
                    System.out.println("Retornei uma reply com o valor "+money+" para o cliente.");
                    //System.out.println("User: " + publicKey.substring(0, 50) + " has now " + money + "€");
                    return r;
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception putting value into map: " + e.getMessage());
            }

        }else{
            throw new NotAuthorizedException("Don't have permission to add money.");

        }

        return null;
    }


    @SuppressWarnings("Duplicates")
    @POST
    @Path("/transfer/{fpublicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Reply transferMoney(@PathParam("fpublicKey") String fpublicKey,
                               @QueryParam("tpublicKey") String tpublicKey,
                               @QueryParam("value") Double value,
                               @QueryParam("nonce") Long nonce,
                               @QueryParam("msg") String msg)
            throws Exception {


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

                objOut.writeObject(TRANSFER);
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

                    String money = (String) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    System.out.println("RESPONSE FROM TRANSFER MONEY IS:");
                    System.out.println("User " + fpublicKey.substring(0, 50) + " transfered " + value + "€" + " to user " + tpublicKey.substring(0, 50));
                    return new Reply(TRANSFER, captureMessages.getReplicaMessages(), fpublicKey, money, replyNonce + 1);
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
    public Reply getMoney(@PathParam("publicKey") String publicKey,
                          @QueryParam("nonce") Long nonce,
                          @QueryParam("msg") String msg,
                          @QueryParam("type") String type,
                          @QueryParam("encryptType") String encryptType)
            throws Exception {

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

                objOut.writeObject(GET_MONEY);
                objOut.writeObject(publicKey);
                objOut.writeObject(nonce);
                objOut.writeObject(type);
                objOut.writeObject(encryptType);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {


                    String money = (String) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    System.out.println("RESPONSE FROM GET MONEY IS:");
                    System.out.println("User " + publicKey.substring(0, 50) + " has " + money + "€ in the his account");

                    Reply rep = new Reply(GET_MONEY, captureMessages.getReplicaMessages(), publicKey, money, replyNonce+1);
                    System.out.println("I GOT + " + captureMessages.getReplicaMessages().size() + " MESSAGES From Rep");
                    return rep;

                    //return new Reply(GET_MONEY, captureMessages.getReplicaMessages(), publicKey, money, replyNonce + 1);

                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception getting value from map: " + e.getMessage());
            }
        }
        return null;
    }



    @SuppressWarnings("Duplicates")
    @GET
    @Path("/money")
    @Produces(MediaType.APPLICATION_JSON)
    public Reply_OPE getHOMO_OPE(@QueryParam("publicKey") String publicKey,
                             @QueryParam("higher") String higher,
                             @QueryParam("lower") String lower,
                             @QueryParam("nonce") Long nonce,
                             @QueryParam("type") String type,
                             @QueryParam("encryptType") String encryptType)
            throws Exception {


        System.out.println("TYPE RECEIVED - " + type);

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {


                objOut.writeObject(GET_LOW_HIGH);
                objOut.writeObject(publicKey);
                objOut.writeObject(higher);
                objOut.writeObject(lower);
                objOut.writeObject(nonce);
                objOut.writeObject(type);
                objOut.writeObject(encryptType);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {

                    List<String> keyList = (List<String>) objIn.readObject();
                    Long replyNonce = (Long) objIn.readObject();

                    System.out.println("GET_OPE within the interval of amounts " + lower + " -> " + higher + " got the keys:");
                    for (String key : keyList) {
                        System.out.println(key);
                    }


                    return new Reply_OPE(GET_LOW_HIGH, captureMessages.getReplicaMessages(), keyList, replyNonce + 1);

                }

        } catch (Exception e) {
            System.out.println("Exception getting values from map: " + e.getMessage());
        }





        return null;
    }

    @SuppressWarnings("Duplicates")
    @POST
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ReplyCondUpd_Client cond_upd(@QueryParam("cond_key") String cond_key,
                                        @QueryParam("cond_value") String cond_value,
                                        @QueryParam("cond_number") Integer cond_number,
                                        @QueryParam("op_list") String op_list,
                                        @QueryParam("nonce") Long nonce)
            throws Exception {

        HashMap<String,String> map = new HashMap<>();
        Long replyNonce = null;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {


            objOut.writeObject(COND_UPD);
            objOut.writeObject(cond_key);
            objOut.writeObject(cond_value);
            objOut.writeObject(cond_number);
            objOut.writeObject(op_list);
            objOut.writeObject(nonce);

            objOut.flush();
            byteOut.flush();


            byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
            if (reply.length == 0)
                return null;
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {

                map = (HashMap<String, String>) objIn.readObject();
                replyNonce = (Long) objIn.readObject();

                // DO THINGS

                return new ReplyCondUpd_Client(map, replyNonce+1, captureMessages.getReplicaMessages());

            }
            catch (Exception e){
                System.out.println("Exception from the inside.");
            }
        }
        catch (Exception e) {
            System.out.println("Exception from the outside." + e.getMessage());
        }


        return null;
    }

}
