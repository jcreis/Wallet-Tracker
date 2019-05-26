package container;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import hj.mlib.HomoOpeInt;
import hj.mlib.PaillierKey;
import model.ReplySGX;
import model.TypeAmount;
import security.PrivateKey;
import security.PublicKey;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;

@Path("/sgx")
public class sconeApi {

    public sconeApi() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());


        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String privateString = Base64.getEncoder().encodeToString(priv.exportKey());


        File publicKey = new File("./sgxPublicKey.txt");
        File privateKey = new File("./sgxPrivateKey.txt");


        FileWriter pb = new FileWriter(publicKey, false);
        FileWriter pr = new FileWriter(privateKey, false);
        pb.write(publicString);
        pr.write(privateString);
        pb.close();
        pr.close();

    }


    @SuppressWarnings("Duplicates")
    @GET
    //@Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ReplySGX getLowHigh(@QueryParam("higher") Long high, @QueryParam("lower") Long low, @QueryParam("nonce")Long nonce,
                                            @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                                            @QueryParam("db") String db, @QueryParam("sgxKey") String sgxKey) throws Exception {


        HashMap<String, TypeAmount> db_filtered = turnDbBackToHashMap(db);
        List<String> returnList = new ArrayList<String>();

        PrivateKey sgx_privateKey = getPrivKey();



        // Decrypt the key to do the operation
        /*String sgxKey_D = URLDecoder.decode(sgxKey, "UTF-8");
        byte[] decryptedPrivate = sgx_privateKey.decrypt(sgxKey_D.getBytes());*/

        //String sgxKey_D = URLDecoder.decode(sgxKey, "UTF-8");
        byte[] decodedBytes = Base64.getDecoder().decode(sgxKey);

        byte[] decryptedPrivate = sgx_privateKey.decrypt(decodedBytes);

        String decrypted = new String(decryptedPrivate);
        String utfString = URLDecoder.decode(decrypted, "UTF-8");

        PaillierKey sgxFinalKey = (PaillierKey)HelpSerial.fromString(decrypted);


        db_filtered.forEach((String key, TypeAmount value) -> {

            BigInteger valueToDecrypt = BigInteger.valueOf(Long.parseLong(value.getAmount()));
            Long valueToCheck = null;

            try {
                BigInteger decriptedBigInt = HomoAdd.decrypt(valueToDecrypt, sgxFinalKey);
                valueToCheck = decriptedBigInt.longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (valueToCheck >= low && valueToCheck <= high) {
                returnList.add(key);
            }

        });

        System.out.println("type; " + type);
        System.out.println("encript type: " + encryptType);
        System.out.println("list: " + returnList);
        ReplySGX response = new ReplySGX(type, encryptType, nonce+1, returnList, 0l);
        return response;


    }


    @SuppressWarnings("Duplicates")
    @POST
    //@Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ReplySGX sum(@QueryParam("balance") Long balance, @QueryParam("value") Long value, @QueryParam("nonce")Long nonce,
                        @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                        @QueryParam("sgxKey") String sgxKey) throws Exception {


        PrivateKey sgx_privateKey = getPrivKey();



        // Decrypt the key to do the operation
        String sgxKey_D = URLDecoder.decode(sgxKey, "UTF-8");
        byte[] decryptedPrivate = sgx_privateKey.decrypt(sgxKey_D.getBytes());


        HomoOpeInt ope = new HomoOpeInt(decryptedPrivate.toString());
        int decriptedValueToAdd = ope.decrypt(value);

        int decriptedBalance = ope.decrypt(balance);

        long returnValueEncrypted = ope.encrypt(decriptedBalance + decriptedValueToAdd);



        ReplySGX reply = new ReplySGX(type, encryptType, nonce, null, returnValueEncrypted);
        return reply;
    }







    private PrivateKey getPrivKey() throws Exception {
        File sgxPrivateKey = new File("./sgxPrivateKey.txt");
        String privateKey="";
        Scanner scanner = new Scanner(sgxPrivateKey);

        while(scanner.hasNextLine()){
            privateKey=scanner.next();
            System.out.println("privateKey: " + privateKey);
        }

        byte[] sgxByte = Base64.getDecoder().decode(privateKey);
        PrivateKey sgxPrivate = PrivateKey.createKey(sgxByte);
        return sgxPrivate;
    }



    private HashMap<String, TypeAmount> turnDbBackToHashMap(String db) throws UnsupportedEncodingException {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gsonObject = gsonBuilder.create();

        String db_D = URLDecoder.decode(db, "UTF-8");

        HashMap<String, TypeAmount> db_filtered = gsonObject.fromJson(db_D, HashMap.class);
        return db_filtered;
    }

}
