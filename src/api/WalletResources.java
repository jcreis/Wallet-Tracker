package api;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import hj.mlib.PaillierKey;
import model.Reply;
import model.CaptureMessages;
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
import java.util.concurrent.ConcurrentHashMap;

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


    private Map<String, Double> db = new ConcurrentHashMap<String, Double>();

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
                          @QueryParam("nSquare") BigInteger nSquare)

            throws Exception {


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

                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                    objOut.writeObject(ADD_MONEY);
                    objOut.writeObject(publicKey);
                    objOut.writeObject(value);
                    objOut.writeObject(nonce);
                    objOut.writeObject(type);
                    objOut.writeObject(nSquare);

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
                        Reply r = new Reply(ADD_MONEY, captureMessages.getReplicaMessages(), publicKey, money, replyNonce + 1);
                        System.out.println("User: " + publicKey.substring(0, 50) + " has now " + money + "€");
                        return r;
                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Exception putting value into map: " + e.getMessage());
                }

        }
        throw new NotAuthorizedException("Don't have permission to add money.");


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

                    double money = (Double) objIn.readObject();
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
                          @QueryParam("higher") int higher,
                          @QueryParam("lower") int lower)
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
                objOut.writeObject(higher);
                objOut.writeObject(lower);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {

                    // ve se vem lista de resultados - HOMO_OPE_INT
                    boolean moreThanOne = (Boolean) objIn.readObject();
                    if(moreThanOne){
                        // HOMO_OPE_INT
                        List<String> moneyList = (List<String>) objIn.readObject();
                        replyNonce = (Long) objIn.readObject();

                        // agora manda money em lista em vez de double
                        return new Reply(GET_MONEY, captureMessages.getReplicaMessages(), publicKey, moneyList, replyNonce + 1);
                    }
                    else {
                        // WALLET
                        double money = (Double) objIn.readObject();
                        replyNonce = (Long) objIn.readObject();
                        System.out.println("RESPONSE FROM GET MONEY IS:");
                        System.out.println("User " + publicKey.substring(0, 50) + " has " + money + "€ in the his account");

                        return new Reply(GET_MONEY, captureMessages.getReplicaMessages(), publicKey, money, replyNonce + 1);
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception getting value from map: " + e.getMessage());
            }
        }
        return null;
    }



    /*// key, initial_value, “HOMO_ADD”
    @SuppressWarnings("Duplicates")
    @POST
    @Path("/create/{publicKey}/")
    public Reply create(@PathParam("publicKey") String publicKey,
                        @QueryParam("initValue") Double initValue,
                        @QueryParam("type") String type)
            throws Exception {


        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            objOut.writeObject(CREATE);
            objOut.writeObject(publicKey);
            objOut.writeObject(initValue);
            objOut.writeObject(type);
        }catch (Exception e){

        }

        PaillierKey key = HomoAdd.generateKey();
        key.printValues();

        BigInteger value = new BigInteger(""+ initValue.intValue());
        BigInteger dbValue = new BigInteger("0");
        *//*
        BigInteger big1 = new BigInteger("11");
        BigInteger big2 = new BigInteger("22");
        *//*
        BigInteger valueCode = HomoAdd.encrypt(value, key);
        BigInteger dbValueCode = HomoAdd.encrypt(dbValue, key);
        *//*
        BigInteger big1Code = HomoAdd.encrypt(big1, pk);
        BigInteger big2Code = HomoAdd.encrypt(big2, pk);
        *//*
        *//*System.out.println("big1:     " + big1);
        System.out.println("big2:     " + big2);
        System.out.println("big1Code: " + big1Code);
        System.out.println("big2Code: " + big2Code);*//*

        BigInteger valuePlusDbValueCode = HomoAdd.sum(valueCode, dbValueCode, key.getNsquare());
        //BigInteger big1plus2Code = HomoAdd.sum(big1Code, big2Code, pk.getNsquare());
        //System.out.println("big1+big2 Code: " + big1plus2Code);

        BigInteger valuePlusDbValue = HomoAdd.decrypt(valuePlusDbValueCode, key);
        //BigInteger big1plus2 = HomoAdd.decrypt(big1plus2Code, pk);
        //System.out.println("Resultado = " + big1plus2.intValue());

        *//*System.out.println("Teste de subtracao");
        BigInteger big1minus2Code = HomoAdd.dif(big1Code, big2Code, pk.getNsquare());
        System.out.println("big1-big2 Code: " + big1minus2Code);
        BigInteger big1minus2 = HomoAdd.decrypt(big1minus2Code, pk);
        System.out.println("Resultado = " + big1minus2.intValue());*//*

        // Test key serialization
        String chaveGuardada = "";

        chaveGuardada = HelpSerial.toString(key);

        *//*System.out.println("Chave guardada: " + chaveGuardada);
        // Test with saved key
        PaillierKey key2 = null;
        BigInteger op3 = null;
        key2 = (PaillierKey) HelpSerial.fromString(chaveGuardada);
        op3 = HomoAdd.decrypt(big1minus2, pk2);
        System.out.println("Subtracao: " + op3);*//*


        return null;
    }*/

}
