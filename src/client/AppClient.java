package client;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.util.KeyLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import hj.mlib.HomoOpeInt;
import hj.mlib.PaillierKey;
import model.*;
import security.Digest;
import security.PrivateKey;
import security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
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
import java.math.BigInteger;
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

    private static List<Long> transferRequestTimes = new ArrayList<Long>();

    private static List<Long> addMoneyRequestTimes = new ArrayList<Long>();

    private static List<Long> getMoneyRequestTimes = new ArrayList<Long>();

    private static PaillierKey pk = HomoAdd.generateKey();

    private static BigInteger nSquare = pk.getNsquare();

    private static long HomoOpeIntKey = HomoOpeInt.generateKey();
    private static HomoOpeInt ope = new HomoOpeInt(HomoOpeIntKey);


    public static void main(String[] args) throws Exception {

        try {

            //addMoney("HOMO_ADD", EncryptOpType_ADD.CREATE);
            //addMoney("HOMO_ADD", EncryptOpType_ADD.CREATE);
            //addMoney("HOMO_ADD", EncryptOpType_ADD.SET);
            //addMoney("HOMO_ADD", EncryptOpType_ADD.SUM);
            addMoney("HOMO_OPE_INT", EncryptOpType_ADD.CREATE);
            addMoney("HOMO_OPE_INT", EncryptOpType_ADD.SUM);
            //getMoney("HOMO_OPE_INT", EncryptOpType_GET.GET);
            //getMoney("HOMO_ADD", EncryptOpType_GET.GET);
            //getMoney_LOW_HIGH("HOMO_ADD", EncryptOpType_GET.GET_LOWER_HIGHER);
            //getMoney_LOW_HIGH("HOMO_OPE_INT", EncryptOpType_GET.GET_LOWER_HIGHER);

            //getMoney_LOW_HIGH("HOMO_OPE_INT", EncryptOpType_GET.GET_LOWER_HIGHER);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //addMoney("WALLET", EncryptOpType_ADD.SET);
        //addMoney("WALLET", EncryptOpType_ADD.CREATE);

        /*long initAddMoneyTime = System.currentTimeMillis();
        while (true) {
            //addMoney("WALLET", "HOMO_ADD");
            // TODO after tests done -> runtime = 300*60
            if (System.currentTimeMillis() - initAddMoneyTime >= 30 * 60) {
                break;
            }
        }

        @SuppressWarnings("Duplicates")
        Thread transferThread1 = new Thread() {
            @Override
            public void run() {
                long initTransferTime = System.currentTimeMillis();
                while (true) {
                    try {
                        transferMoney();
                        //getMoney("WALLET", );
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // run during 3 min
                    // TODO after tests done -> runtime = 3000*60
                    if (System.currentTimeMillis() - initTransferTime >= 300 * 60)
                        break;
                }


            }
        };

        @SuppressWarnings("Duplicates")
        Thread transferThread2 = new Thread() {
            @Override
            public void run() {
                long initTransferTime = System.currentTimeMillis();
                while (true) {
                    try {
                        //getMoney("WALLET", "HOMO_ADD");
                        transferMoney();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    // TODO after tests done -> runtime = 3000*60
                    if (System.currentTimeMillis() - initTransferTime >= 300 * 60)
                        break;
                }
            }
        };


        transferThread1.start();
        transferThread2.start();
        transferThread2.join();

        System.out.println("#####################################");
        System.out.println("###### AVERAGE REQUEST TIMES ########");
        System.out.println("#####################################");
        System.out.println("Average time of transfer requests: " + getTransferAvgTime() + "ms");
        System.out.println("Average time of getMoney requests: " + getGetMoneyAvgTime() + "ms");
        System.out.println("Average time of addMoney requests: " + getAddMoneyAvgTime() + "ms");
*/
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
        if (kp2.getPublic().equals(kp.getPublic())) {
            kp2 = keys.get(random.nextInt(keys.size()));
        }
        //kp = keys.get(random.nextInt(keys.size()));
        PublicKey pub2 = new PublicKey("RSA", kp2.getPublic());
        PrivateKey priv2 = new PrivateKey("RSA", kp2.getPrivate());
        String publicString2 = Base64.getEncoder().encodeToString(pub2.exportKey());
        String pathPublicKey2 = URLEncoder.encode(publicString2, "UTF-8");


        Random randomm = new Random();
        Double value = randomm.nextInt(89) + 10.0;

        Long nonce = random.nextLong();

        String msg = publicString + publicString2 + value + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);

        // Calculate time for request
        long initRequestTime = System.currentTimeMillis();

        Response response = target.path("transfer/" + pathPublicKey).queryParam("tpublicKey", pathPublicKey2)
                .queryParam("value", value)
                .queryParam("nonce", nonce)
                .queryParam("msg", msgHashStr)
                .request()
                .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));

        long finalRequestTime = System.currentTimeMillis() - initRequestTime;
        transferRequestTimes.add(finalRequestTime);

        Reply r = response.readEntity(Reply.class);

        ArrayList<Double> amounts = new ArrayList<Double>();
        ArrayList<Long> lNonces = new ArrayList<Long>();

        for (ReplicaResponseMessage currentReplicaMsg : r.getMessages()) {
            if (currentReplicaMsg != null) {
                ByteArrayInputStream byteIn = new ByteArrayInputStream(currentReplicaMsg.getContent());
                ObjectInput objIn = new ObjectInputStream(byteIn);
                Double replicaMsgAmount = (Double) objIn.readObject();
                //System.out.println("replica amount: "+ replicaMsgAmount);
                Long replicaNonce = (Long) objIn.readObject();
                //System.out.println("replica nonce: " + replicaNonce);


                amounts.add(replicaMsgAmount);
                lNonces.add(replicaNonce);

                KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
                java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
                Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
                sig.initVerify(pk);
                sig.update(currentReplicaMsg.getSerializedMessage());


                if (sig.verify(currentReplicaMsg.getSignature())) {
                    System.out.println("Replica message coming from replica " + currentReplicaMsg.getSender() + " is authentic");
                } else {
                    System.out.println("Signature of message is invalid");
                }
            }
        }

        int majority = 0;
        int numbNonces = 0;
        for (Double amount : amounts) {
            if (amount == Double.parseDouble(r.getAmount()))
                majority++;
        }
        for (Long n : lNonces) {
            if (n + 1 == r.getNonce())
                numbNonces++;
        }

        // Verify majority of nonces of replicas
        if (numbNonces >= (lNonces.size() / 2) + 1) {
            System.out.println("majority of replicas returns the right nonce");
        } else {
            System.out.println("No majority reached for nonce");

        }

        // Verify majority from message replies of replicas
        if ((majority >= (amounts.size() / 2) + 1)) {
            System.out.println("majority of replicas returns the right value");
        } else {
            System.out.println("No majority reached");
        }

        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.TRANSFER) {
            System.out.println("Nonces dont match, reject message from server");
        } else {

            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("From pubKey: " + publicString.substring(0, 50));
            System.out.println("To pubKey: " + publicString2.substring(0, 50));
            System.out.println("Transferring amount : " + value);
            System.out.println(publicString2.substring(0, 50) + " now has " + Double.parseDouble(r.getAmount()));
            if (nonce + 1 == r.getNonce()) {
                System.out.println("Nonces match");
            }
            System.out.println();

        }
    }


    @SuppressWarnings("Duplicates")
    public static void addMoney(String type, EncryptOpType_ADD encryptType) throws Exception {
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


        Random randomm = new Random();

        //Adicionar um valor random
        Double value = randomm.nextInt(899) + 100.0;

        System.out.println("Valor gerado (desencriptado) = " + value);

        File file = new File("./publicKey.txt");
        File file2 = new File("./privateKey.txt");
        File sgxPublicKey = new File("./sgxPublicKey.txt");
        String sgxPublicString = null;
        String adminPublicString = null;
        String adminPrivateString = null;

        Scanner sc = new Scanner(file);
        Scanner sc2 = new Scanner(file2);
        Scanner sc3 = new Scanner(sgxPublicKey);


        while (sc.hasNextLine() && sc2.hasNextLine() && sc3.hasNextLine()) {
            adminPublicString = sc.next();
            adminPrivateString = sc2.next();
            sgxPublicString = sc3.next();
        }

        byte[] sgxByte = Base64.getDecoder().decode(sgxPublicString);
        PublicKey sgxPublic = PublicKey.createKey(sgxByte);

        //HOMO_ADD KEY

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256); // The AES key size in number of bits
        SecretKey secKey = generator.generateKey();

        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.ENCRYPT_MODE, secKey);

        // Encrypt PaillierKey with AES
        byte[] aes_pk = aesCipher.doFinal(HelpSerial.toString(pk).getBytes());
        // Encrypt AES with pubKeyRSA
        byte[] rsa_pk = sgxPublic.encrypt(secKey.getEncoded());

        String homo_add_PaillierKeyWithAES = Base64.getEncoder().encodeToString(aes_pk);
        String homo_add_AESkeyWithRSA = Base64.getEncoder().encodeToString(rsa_pk);


        System.out.println("Pailier Key Encrypted with AES :" + homo_add_PaillierKeyWithAES);
        System.out.println("AES Encripted RSA : " + homo_add_AESkeyWithRSA);


        //HOMO_OPE_INT
        /*byte[] intKey = sgxPublic.encrypt(Long.toString(HomoOpeIntKey).getBytes());
        String homo_ope_int_Key = Base64.getEncoder().encodeToString(intKey);*/

        //KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(256); // The AES key size in number of bits
        SecretKey secKey_homo_ope_int = generator.generateKey();
        //String homo_ope_int_Key = URLEncoder.encode(homo_ope_int_Key_S, "UTF-8");
        Cipher aesCipher2 = Cipher.getInstance("AES");
        aesCipher2.init(Cipher.ENCRYPT_MODE, secKey_homo_ope_int);

        // Encrypt HOMO_OPE_INT_KEY with AES
        byte[] ope_aes = aesCipher2.doFinal(HelpSerial.toString(HomoOpeIntKey).getBytes());
        // Encrypt AES with pubKeyRSA
        byte[] aes_pubKey_rsa = sgxPublic.encrypt(secKey_homo_ope_int.getEncoded());

        String homo_ope_int_OPEkeyWithAES = Base64.getEncoder().encodeToString(ope_aes);
        String homo_ope_int_AESkeyWithPubKeyRSA = Base64.getEncoder().encodeToString(aes_pubKey_rsa);



        /*System.out.println("AdminPublicKey : " + adminPublicString.substring(0,50));
        System.out.println("AdminPrivateKey : " + adminPrivateString.substring(0,50));*/

        byte[] pubByte = Base64.getDecoder().decode(adminPublicString);
        PublicKey adminPub = PublicKey.createKey(pubByte);
        byte[] privByte = Base64.getDecoder().decode(adminPrivateString);
        PrivateKey adminPriv = PrivateKey.createKey(privByte);


        String adminPubString = Base64.getEncoder().encodeToString(adminPub.exportKey());
        String adminPathPublicKey = URLEncoder.encode(adminPubString, "UTF-8");


        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        keys.add(kp);
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());

        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());


        Long nonce = random.nextLong();

        Reply r;

        String msg = publicString + value + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = adminPriv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);
        String pathPublicKey = URLEncoder.encode(publicString, "UTF-8");


        // Calculate time for request
        long initRequestTime = System.currentTimeMillis();

        Response response;


        switch (type) {

            case "WALLET":
                response = target.path(pathPublicKey)
                        .queryParam("value", value.toString())
                        .queryParam("nonce", nonce)
                        .queryParam("msg", msgHashStr)
                        .queryParam("type", type)
                        .queryParam("encryptType", encryptType)
                        .request()
                        .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));

                r = response.readEntity(Reply.class);
                break;

            case "HOMO_ADD":

                BigInteger big1 = BigInteger.valueOf(2);
                BigInteger encryptValue = HomoAdd.encrypt(big1, pk);

                // Value changed, it's now encrypted
                // therefore we needed to update all hashes before sending it to the server
                msg = publicString + encryptValue + nonce;
                hash = Digest.getDigest(msg.getBytes());
                hashEncriptPriv = adminPriv.encrypt(hash);
                msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);

                System.out.println("value: " + value);
                System.out.println("encrypt Value: " + encryptValue);
                System.out.println("send encrypt value to server");

                response = target.path(pathPublicKey)
                        .queryParam("value", encryptValue.toString())
                        .queryParam("nonce", nonce)
                        .queryParam("msg", msgHashStr)
                        .queryParam("type", type)
                        .queryParam("encryptType", encryptType)
                        .queryParam("nSquare", nSquare)
                        .queryParam("homoAddKey", homo_add_PaillierKeyWithAES)
                        .queryParam("aesKey", homo_add_AESkeyWithRSA)
                        .request()
                        .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));

                r = response.readEntity(Reply.class);

                System.out.println("recebi a conta com o valor (encriptado)" + r.getAmount());
                if (r.getAmount().equals("-1")) {
                    System.out.println("Something went wrong.");
                } else {
                    BigInteger BigIntegerValue = new BigInteger(r.getAmount());
                    int addValue = HomoAdd.decrypt(BigIntegerValue, pk).intValue();
                    System.out.println("vou desencriptar o valor. Deu isto -> " + addValue);
                }
                break;

            case "HOMO_OPE_INT":

                //Long openValue = ope.encrypt(value.intValue());
                Long openValue = ope.encrypt(1100);
                msg = adminPubString + openValue + nonce;
                hash = Digest.getDigest(msg.getBytes());
                hashEncriptPriv = adminPriv.encrypt(hash);
                msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);

                System.out.println("Vou enviar o valor encriptado po server -> " + openValue);

                System.out.println("PUBLIC KEY -> " + pathPublicKey);
                response = target.path(adminPathPublicKey)
                        .queryParam("value", openValue.toString())
                        .queryParam("nonce", nonce)
                        .queryParam("msg", msgHashStr)
                        .queryParam("type", type)
                        .queryParam("encryptType", encryptType)
                        .queryParam("homoOpeIntKey", homo_ope_int_OPEkeyWithAES)
                        .queryParam("aesKey", homo_ope_int_AESkeyWithPubKeyRSA)
                        .request()
                        .post(Entity.entity(Reply.class, MediaType.APPLICATION_JSON));

                r = response.readEntity(Reply.class);
                System.out.println("recebi a conta com o valor (encriptado)" + r.getAmount());
                if (r.getAmount().equals("-1")) {
                    System.out.println("Something went wrong.");
                } else {
                    System.out.println("amount before decrypting" + r.getAmount());
                    int addValue = ope.decrypt(Long.parseLong(r.getAmount()));
                    System.out.println("vou desencriptar o valor. Deu isto -> " + addValue);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected type: " + type);
        }

        long finalRequestTime = System.currentTimeMillis() - initRequestTime;
        addMoneyRequestTimes.add(finalRequestTime);


        ArrayList<Double> amounts = new ArrayList<Double>();
        ArrayList<Long> lNonces = new ArrayList<Long>();

        for (int i = 0; i < r.getMessages().size(); i++) {
            ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(currentReplicaMsg.getContent());
            ObjectInput objIn = new ObjectInputStream(byteIn);
            String msgStringAmount = (String) objIn.readObject();

            Double replicaMsgAmount = Double.parseDouble(msgStringAmount);
            if (replicaMsgAmount == -1) {
                System.out.println("Account does not exist in the database");
            }

            // System.out.println("replica amount: "+ replicaMsgAmount);
            Long replicaNonce = (Long) objIn.readObject();
            // System.out.println("replica nonce: " + replicaNonce);


            amounts.add(replicaMsgAmount);
            lNonces.add(replicaNonce);

            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(currentReplicaMsg.getSerializedMessage());


            if (sig.verify(currentReplicaMsg.getSignature())) {
                System.out.println("Replica message coming from replica " + currentReplicaMsg.getSender() + " is authentic");
            } else {

                System.out.println("Signature of message is invalid");
            }

        }
        int majority = 0;
        int numbNonces = 0;
        for (Double amount : amounts) {
            if (amount == Double.parseDouble(r.getAmount()))
                majority++;
        }
        for (Long n : lNonces) {
            if (n + 1 == r.getNonce())
                numbNonces++;
        }

        // Verify majority of nonces of replicas
        if (numbNonces >= (lNonces.size() / 2) + 1) {
            System.out.println("majority of replicas returns the right nonce");
        } else {
            System.out.println("No majority reached for nonce");

        }

        // Verify majority from message replies of replicas
        if ((majority >= (amounts.size() / 2) + 1)) {
            System.out.println("majority of replicas returns the right value");
        } else {
            System.out.println("No majority reached");
        }


        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.ADD_MONEY) {
            System.out.println("Nonces dont match, reject message from server");
        } else {

            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("Add money to pubKey: " + publicString.substring(0, 50));
            /*int addValue = HomoAdd.decrypt(new BigInteger(r.getAmount()), pk).intValue();
            System.out.println("Amount: " + addValue);*/
            if (nonce + 1 == r.getNonce()) {
                System.out.println("Nonces match");
            }
            System.out.println();


        }
    }


    @SuppressWarnings("Duplicates")
    public static void getMoney(String type, EncryptOpType_GET encryptType) throws Exception {
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

        Reply r;

        String msg = publicString + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);

        long initRequestTime = System.currentTimeMillis();

        Response response;

        ArrayList<Double> amounts = new ArrayList<Double>();
        ArrayList<Integer> amounts_add = new ArrayList<Integer>();
        ArrayList<Integer> amounts_int = new ArrayList<Integer>();
        ArrayList<Long> lNonces = new ArrayList<Long>();

        int majority = 0;
        int numbNonces = 0;

        switch (type) {
            case "WALLET":
                System.out.println("A fazer pedido WALLET - GET");

                response = target.path(pathPublicKey + "/money")
                        .queryParam("nonce", nonce)
                        .queryParam("msg", msgHashStr)
                        .queryParam("type", type)
                        .queryParam("encryptType", encryptType)
                        .request()
                        .get();
                r = response.readEntity(Reply.class);

                System.out.println("replica amount: " + r.getAmount());


                break;

            case "HOMO_ADD":
                System.out.println("A fazer pedido HOMO ADD - GET");
                response = target.path(pathPublicKey + "/money")
                        .queryParam("nonce", nonce)
                        .queryParam("msg", msgHashStr)
                        .queryParam("type", type)
                        .queryParam("encryptType", encryptType)
                        .request()
                        .get();
                r = response.readEntity(Reply.class);

                if (r.getAmount().equals("-1")) {
                    System.out.println("Something went wrong.");
                } else {
                    BigInteger BigIntegerValue = new BigInteger(r.getAmount());
                    int addValue = HomoAdd.decrypt(BigIntegerValue, pk).intValue();
                    System.out.println("value = " + addValue);
                }
                break;

            case "HOMO_OPE_INT":
                System.out.println("A fazer pedido HOMO OPE INT - GET");

                response = target.path(pathPublicKey + "/money")
                        .queryParam("nonce", nonce)
                        .queryParam("msg", msgHashStr)
                        .queryParam("type", type)
                        .queryParam("encryptType", encryptType)
                        .request()
                        .get();
                r = response.readEntity(Reply.class);

                if (r.getAmount().equals("-1")) {
                    System.out.println("Something went wrong.");
                } else {
                    Long val = Long.parseLong(r.getAmount());
                    int recVal = ope.decrypt(val);
                    System.out.println("AMOUNT = " + recVal);

                }

                break;


            default:
                throw new IllegalStateException("Unexpected type: " + type);
        }


        long finalRequestTime = System.currentTimeMillis() - initRequestTime;
        getMoneyRequestTimes.add(finalRequestTime);

        for (int i = 0; i < r.getMessages().size(); i++) {
            ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(currentReplicaMsg.getContent());
            ObjectInput objIn = new ObjectInputStream(byteIn);

            String msgStringAmount = (String) objIn.readObject();
            switch (type) {
                case "WALLET":
                    Double replicaMsgAmount = Double.parseDouble(msgStringAmount);
                    amounts.add(replicaMsgAmount);

                    for (Double amount : amounts) {
                        if (amount == Double.parseDouble(r.getAmount()))
                            majority++;
                    }
                    break;
                case "HOMO_ADD":
                    if (msgStringAmount.equals("-1")) {
                        System.out.println("User does not exist");
                    } else {
                        BigInteger bigIntegerValue = new BigInteger(msgStringAmount);
                        int addValue = HomoAdd.decrypt(bigIntegerValue, pk).intValue();
                        amounts_add.add(addValue);
                        for (Integer amount : amounts_add) {
                            BigInteger BigIntegerValue_r = new BigInteger(r.getAmount());
                            int r_addValue = HomoAdd.decrypt(BigIntegerValue_r, pk).intValue();
                            if (amount == r_addValue)
                                majority++;
                        }
                    }
                    break;


                case "HOMO_OPE_INT":
                    int openValue = ope.decrypt(Long.parseLong(msgStringAmount));
                    amounts_int.add(openValue);
                    for (Integer amount : amounts_int) {
                        int r_value = ope.decrypt(Long.parseLong(r.getAmount()));
                        if (amount == r_value)
                            majority++;
                    }

                    break;
            }


            // System.out.println("replica amount: "+ replicaMsgAmount);
            Long replicaNonce = (Long) objIn.readObject();
            // System.out.println("replica nonce: " + replicaNonce);


            lNonces.add(replicaNonce);

            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(currentReplicaMsg.getSerializedMessage());


            if (sig.verify(currentReplicaMsg.getSignature())) {
                System.out.println("Replica message coming from replica " + currentReplicaMsg.getSender() + " is authentic");
            } else {

                System.out.println("Signature of message is invalid");
            }

        }


        for (Long n : lNonces) {
            if (n + 1 == r.getNonce())
                numbNonces++;
        }

        // Verify majority of nonces of replicas
        if (numbNonces >= (lNonces.size() / 2) + 1) {
            System.out.println("majority of replicas returns the right nonce");
        } else {
            System.out.println("No majority reached for nonce");

        }

        // Verify majority from message replies of replicas
        if ((majority >= (amounts.size() / 2) + 1)) {
            System.out.println("majority of replicas returns the right value");
        } else {
            System.out.println("No majority reached");
        }

        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.GET_MONEY) {
            System.out.println("Nonces dont match, reject message from server");
        } else {

            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("From pubKey: " + publicString.substring(0, 50));
            //TODO Verificar este print está mal tratar pa cada caso nao e sempre double
            //System.out.println("New amount: " + Double.parseDouble(r.getAmount()));
            if (nonce + 1 == r.getNonce()) {
                System.out.println("Nonces match");
            }
            System.out.println();

        }


    }


    @SuppressWarnings("Duplicates")
    public static void getMoney_LOW_HIGH(String type, EncryptOpType_GET encryptType) throws Exception {
        System.out.println("#################################");
        System.out.println("####### Get Low High #######");
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

        Reply_OPE r;

        String msg = publicString + nonce;
        byte[] hash = Digest.getDigest(msg.getBytes());

        byte[] hashEncriptPriv = priv.encrypt(hash);
        String msgHashStr = Base64.getEncoder().encodeToString(hashEncriptPriv);

        long initRequestTime = System.currentTimeMillis();

        Response response;


        ArrayList<String> amounts = new ArrayList<>();
        ArrayList<Long> lNonces = new ArrayList<Long>();

        int majority = 0;
        int numbNonces = 0;

        // Prepare HTTP Request

        Double randValue1 = random.nextInt(899) + 100.0;
        Double randValue2 = random.nextInt(899) + 100.0;

        // higher is less than lower
        if (randValue1 < randValue2) {
            Double temp = randValue2;
            // lower becomes higher
            randValue2 = randValue1;
            // higher becomes lower
            randValue1 = randValue2;
        }
        //Long higher = ope.encrypt(randValue1.intValue());
        //Long lower = ope.encrypt(randValue2.intValue());
        String higher = "1200";
        String lower = "1";
        /*int higherr = 1200;
        int lowerr = 1000;*/
        if (type.equals("HOMO_OPE_INT")) {
            higher = HelpSerial.toString(ope.encrypt(1200));
            lower = HelpSerial.toString(ope.encrypt(1000));
        } else if (type.equals("HOMO_ADD")) {


            BigInteger bigIntHigher = new BigInteger(higher);
            BigInteger bigIntLower = new BigInteger(lower);

            higher = HelpSerial.toString(HomoAdd.encrypt(bigIntHigher, pk));
            lower = HelpSerial.toString(HomoAdd.encrypt(bigIntLower, pk));


            System.out.println("BIGHIGHER: " + bigIntHigher);
            System.out.println("BIGLOWER: " + bigIntLower);
            System.out.println("HIGHER: " + higher);
            System.out.println("LOWER: " + lower);

        } else {
            higher = "1200";
            lower = "1000";
        }


        response = target.path("/money")
                .queryParam("publicKey", pathPublicKey)
                .queryParam("higher", higher)
                .queryParam("lower", lower)
                .queryParam("nonce", nonce)
                .queryParam("type", type)
                .queryParam("encryptType", encryptType)
                .request()
                .get();
        r = response.readEntity(Reply_OPE.class);

        System.out.println(response.getStatus());
        for (String key : r.getListAmounts()) {

            System.out.println(key);
        }


        long finalRequestTime = System.currentTimeMillis() - initRequestTime;
        getMoneyRequestTimes.add(finalRequestTime);

        for (int i = 0; i < r.getMessages().size(); i++) {
            ReplicaResponseMessage currentReplicaMsg = r.getMessages().get(i);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(currentReplicaMsg.getContent());
            ObjectInput objIn = new ObjectInputStream(byteIn);
            List<String> msgStringKey = (List<String>) objIn.readObject();
            for (String key : msgStringKey) {

                amounts.add(key);
            }


            // System.out.println("replica amount: "+ replicaMsgAmount);
            Long replicaNonce = (Long) objIn.readObject();
            // System.out.println("replica nonce: " + replicaNonce);


            lNonces.add(replicaNonce);

            KeyLoader keyLoader = new RSAKeyLoader(0, "config", false, "SHA256withRSA");
            java.security.PublicKey pk = keyLoader.loadPublicKey(currentReplicaMsg.getSender());
            Signature sig = Signature.getInstance("SHA512withRSA", "SunRsaSign");
            sig.initVerify(pk);
            sig.update(currentReplicaMsg.getSerializedMessage());


            if (sig.verify(currentReplicaMsg.getSignature())) {
                System.out.println("Replica message coming from replica " + currentReplicaMsg.getSender() + " is authentic");
            } else {

                System.out.println("Signature of message is invalid");
            }

        }

        for (String key : amounts) {

            for (String k : r.getListAmounts()) {

                if (key.equals(k))
                    majority++;
            }

        }

        for (Long n : lNonces) {
            if (n + 1 == r.getNonce())
                numbNonces++;
        }

        // Verify majority of nonces of replicas
        if (numbNonces >= (lNonces.size() / 2) + 1) {
            System.out.println("majority of replicas returns the right nonce");
        } else {
            System.out.println("No majority reached for nonce");

        }

        // Verify majority from message replies of replicas
        if ((majority >= (amounts.size() / 2) + 1)) {
            System.out.println("majority of replicas returns the right value");
        } else {
            System.out.println("No majority reached");
        }

        // Check if response nonce(which is nonce+1) is equals to original nonce + 1
        if (r.getNonce() != nonce + 1 && r.getOperationType() == OpType.GET_MONEY) {
            System.out.println("Nonces dont match, reject message from server");
        } else {
            System.out.println();
            System.out.println("Status: " + response.getStatusInfo());
            System.out.println("From pubKey: " + publicString.substring(0, 50));
            if (nonce + 1 == r.getNonce()) {
                System.out.println("Nonces match");
            }
            System.out.println("The keys are: " + r.getListAmounts());
            System.out.println();
        }

    }

    @SuppressWarnings("Duplicates")
    public static void conditional_upd(String cond_key, String value, int cond, ArrayList<UpdateKeyValue> list) throws Exception {
        System.out.println("#################################");
        System.out.println("####### Conditional Update #######");
        System.out.println("#################################");
        System.out.println();

        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new InsecureHostnameVerifier())
                .build();

        URI baseURI = UriBuilder.fromUri("https://localhost:8080/users/").build();
        WebTarget target = client.target(baseURI);
        System.out.println("URI: " + baseURI);

        Response response;

        Long nonce = random.nextLong();

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gsonObject = gsonBuilder.create();
        String listJson_S = gsonObject.toJson(list);

        String listJson = URLEncoder.encode(listJson_S, "UTF-8");

        response = target.path("/update")
                .queryParam("cond_key",cond_key )
                .queryParam("cond_value", value)
                .queryParam("cond_number", cond)
                .queryParam("op_list", listJson)
                .queryParam("nonce", nonce)
                .request()
                .post(Entity.entity(Reply_OPE.class, MediaType.APPLICATION_JSON));


        Reply_OPE r = response.readEntity(Reply_OPE.class);

    }


    public static long getTransferAvgTime() {
        long totalTimeCounter = 0;
        for (int i = 0; i < transferRequestTimes.size(); i++) {
            totalTimeCounter += transferRequestTimes.get(i);
        }
        return totalTimeCounter / transferRequestTimes.size();
    }

    public static long getGetMoneyAvgTime() {
        long totalTimeCounter = 0;
        for (int i = 0; i < getMoneyRequestTimes.size(); i++) {
            totalTimeCounter += getMoneyRequestTimes.get(i);
        }
        return totalTimeCounter / getMoneyRequestTimes.size();
    }

    public static long getAddMoneyAvgTime() {
        long totalTimeCounter = 0;
        for (int i = 0; i < addMoneyRequestTimes.size(); i++) {
            totalTimeCounter += addMoneyRequestTimes.get(i);
        }
        return totalTimeCounter / addMoneyRequestTimes.size();
    }


    static public class InsecureHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }


}
