package container;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import hj.mlib.HomoOpeInt;
import hj.mlib.PaillierKey;
import model.ReplyCondUpdate;
import model.ReplySGX;
import model.TypeAmount;
import model.UpdateKeyValue;
import security.PrivateKey;
import security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
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
    public synchronized ReplySGX getLowHigh(@QueryParam("higher") String high, @QueryParam("lower") String low, @QueryParam("nonce") Long nonce,
                                            @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                                            @QueryParam("db") String db, @QueryParam("sgxKey") String sgxKey,
                                            @QueryParam("aesKey") String aesKey) throws Exception {


        HashMap<String, TypeAmount> db_filtered = turnDbBackToHashMap(db);
        List<String> returnList = new ArrayList<String>();

        PrivateKey sgx_privateKey = getPrivKey();
        System.out.println("AES ENCRIPTED WITH RSA : " + aesKey);
        System.out.println("PaillierEnc WITH AES : " + sgxKey);
        // Decrypt the key to do the operation
        byte[] decodedAES = Base64.getDecoder().decode(aesKey);
        byte[] aes = sgx_privateKey.decrypt(decodedAES);
        String homo_add_AESKey = Base64.getEncoder().encodeToString(aes);
        System.out.println("AES : " + homo_add_AESKey);
        byte[] decodedSgxKey = Base64.getDecoder().decode(sgxKey);
        SecretKey AESKey = new SecretKeySpec(aes, 0, aes.length, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, AESKey);
        byte[] PaillierByte = aesCipher.doFinal(decodedSgxKey);
        PaillierKey pk = (PaillierKey) HelpSerial.fromString(new String(PaillierByte));


        //String utfString = URLDecoder.decode(decrypted, "UTF-8");

        //PaillierKey sgxFinalKey = (PaillierKey)HelpSerial.fromString(decrypted);


        db_filtered.forEach((String key, TypeAmount value) -> {

            System.out.println("HIGHER: " + high);
            System.out.println("LOWER: " + low);
            BigInteger valueToDecrypt = new BigInteger(value.getAmount());
            BigInteger loww = (BigInteger) (HelpSerial.fromString(low));
            BigInteger highh = (BigInteger) (HelpSerial.fromString(high));

                /*BigInteger loww = BigInteger.valueOf(low);
                BigInteger highh = BigInteger.valueOf(high);*/


            int valueToCheck = 0;
                /*BigInteger low_b = new BigInteger("0");
                BigInteger high_b = new BigInteger("0");*/

            try {
                BigInteger decriptedBigInt = HomoAdd.decrypt(valueToDecrypt, pk);
                BigInteger low_b = HomoAdd.decrypt(loww, pk);
                BigInteger high_b = HomoAdd.decrypt(highh, pk);

                System.out.println("BIGHIGHER: " + high_b);
                System.out.println("BIGLOWER: " + low_b);

                valueToCheck = decriptedBigInt.intValue();

                System.out.println("VALUE TO CHECK: " + valueToCheck);
                System.out.println("LOW: " + low_b.intValue());
                System.out.println("HIGH: " + high_b.intValue());


                if (valueToCheck >= low_b.intValue() && valueToCheck <= high_b.intValue()) {
                    returnList.add(key);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        System.out.println("type; " + type);
        System.out.println("encript type: " + encryptType);
        System.out.println("list: " + returnList);
        ReplySGX response = new ReplySGX(type, encryptType, nonce + 1, returnList, 0l);
        return response;


    }


    @SuppressWarnings("Duplicates")
    @POST
    //@Path("")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ReplySGX sum(@QueryParam("balance") Long balance, @QueryParam("value") Long value, @QueryParam("nonce") Long nonce,
                                     @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                                     @QueryParam("sgxKey") String sgxKey, @QueryParam("aesKey") String aesKey) throws Exception {


        PrivateKey sgx_privateKey = getPrivKey();


        byte[] decodedAES = Base64.getDecoder().decode(aesKey);
        byte[] aes = sgx_privateKey.decrypt(decodedAES);
        //String homo_ope_int_AESkey = Base64.getEncoder().encodeToString(aes);
        byte[] decodedSGXkey = Base64.getDecoder().decode(sgxKey);
        SecretKey AESKey = new SecretKeySpec(aes, 0, aes.length, "AES");
        Cipher aesCipher = Cipher.getInstance("AES");
        aesCipher.init(Cipher.DECRYPT_MODE, AESKey);
        byte[] opeKeyBytes = aesCipher.doFinal(decodedSGXkey);
        String opeStr = HelpSerial.toString(opeKeyBytes);
        HomoOpeInt ope = new HomoOpeInt(opeStr);
        int decriptedValueToAdd = ope.decrypt(value);
        int decriptedBalance = ope.decrypt(balance);

        int addedValue = decriptedValueToAdd + decriptedBalance;

        long returnValueEncrypted = ope.encrypt(addedValue);


        ReplySGX reply = new ReplySGX(type, encryptType, nonce, null, returnValueEncrypted);
        System.out.println("Balance: " + decriptedBalance);
        System.out.println("Value to sum: " + decriptedValueToAdd);
        System.out.println("Encrypted final value: " + addedValue);
        return reply;
    }

    @SuppressWarnings("Duplicates")
    @POST
    @Path("/op")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ReplyCondUpdate cond_upd(@QueryParam("type") String type, @QueryParam("cond_key") String cond_key, @QueryParam("cond_value") String cond_val,
                                          @QueryParam("cond_number") int cond_number, @QueryParam("op_list") String op_list,
                                          @QueryParam("nonce") Long nonce, @QueryParam("amountToCompare") String amountToCompare, @QueryParam("key_value_list") List<String> key_value_list,
                                          @QueryParam("sgxKey") String sgxKey, @QueryParam("aesKey") String aesKey) throws Exception {

        System.out.println("ENTREI");
        PaillierKey pk = null;
        HomoOpeInt ope = null;
        ReplyCondUpdate reply;
        int value_to_reply_decript = 0;
        String value_to_reply_encript = "";

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gsonObject = gsonBuilder.create();
        String list_D = URLDecoder.decode(op_list, "UTF-8");
        System.out.println("db_GSON : " + list_D);
        Type empMapType = new TypeToken<List<UpdateKeyValue>>() {
        }.getType();
        ArrayList<UpdateKeyValue> list = gsonObject.fromJson(list_D, empMapType);

        PrivateKey sgx_privateKey = getPrivKey();
        int valueToCheck_HOMO_ADD = 0;
        int valueToCheck_HOMO_OPE_INT = 0;

        HashMap<String,String> return_encripted_values_map = new HashMap<>();

        if (type.equals("HOMO_ADD")) {
            System.out.println("AES ENCRIPTED WITH RSA : " + aesKey);
            System.out.println("PaillierEnc WITH AES : " + sgxKey);
            // Decrypt the key to do the operation
            byte[] decodedAES = Base64.getDecoder().decode(aesKey);
            byte[] aes = sgx_privateKey.decrypt(decodedAES);
            String homo_add_AESKey = Base64.getEncoder().encodeToString(aes);
            System.out.println("AES : " + homo_add_AESKey);
            byte[] decodedSgxKey = Base64.getDecoder().decode(sgxKey);
            SecretKey AESKey = new SecretKeySpec(aes, 0, aes.length, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, AESKey);
            byte[] PaillierByte = aesCipher.doFinal(decodedSgxKey);
            pk = (PaillierKey) HelpSerial.fromString(new String(PaillierByte));

            BigInteger valueToDecrypt = new BigInteger(amountToCompare);
            BigInteger decriptedBigInt = HomoAdd.decrypt(valueToDecrypt, pk);
            valueToCheck_HOMO_ADD = decriptedBigInt.intValue();


        } else {
            byte[] decodedAES = Base64.getDecoder().decode(aesKey);
            byte[] aes = sgx_privateKey.decrypt(decodedAES);
            //String homo_ope_int_AESkey = Base64.getEncoder().encodeToString(aes);
            byte[] decodedSGXkey = Base64.getDecoder().decode(sgxKey);
            SecretKey AESKey = new SecretKeySpec(aes, 0, aes.length, "AES");
            Cipher aesCipher = Cipher.getInstance("AES");
            aesCipher.init(Cipher.DECRYPT_MODE, AESKey);
            byte[] opeKeyBytes = aesCipher.doFinal(decodedSGXkey);
            String opeStr = HelpSerial.toString(opeKeyBytes);
            ope = new HomoOpeInt(opeStr);
            valueToCheck_HOMO_OPE_INT = ope.decrypt(Long.parseLong(amountToCompare));
        }


        switch (cond_number) {

            case 0:
                if (type.equals("HOMO_ADD")) {

                    if (valueToCheck_HOMO_ADD == Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    BigInteger valueToDecrypt = new BigInteger(dbAmount);
                                    BigInteger valueDecrypted = HomoAdd.decrypt(valueToDecrypt, pk);
                                    int key_value = valueDecrypted.intValue();
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;

                            }
                            BigInteger v = new BigInteger(HelpSerial.toString(value_to_reply_decript));
                            BigInteger vEncripted = HomoAdd.encrypt(v, pk);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);

                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    System.out.println("COND_VALUE: " + Integer.parseInt(cond_val));
                    System.out.println("valuetocheck: " + valueToCheck_HOMO_OPE_INT);
                    if (valueToCheck_HOMO_OPE_INT == Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    int key_value = ope.decrypt(Long.parseLong(dbAmount));
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;
                            }
                            long vEncripted = ope.encrypt(value_to_reply_decript);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);

                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }

                } else {
                    System.out.println("Not a valid Type");
                }

                break;

            // NOT EQUALS !=
            case 1:

                if (type.equals("HOMO_ADD")) {
                    if (valueToCheck_HOMO_ADD != Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    BigInteger valueToDecrypt = new BigInteger(dbAmount);
                                    BigInteger valueDecrypted = HomoAdd.decrypt(valueToDecrypt, pk);
                                    int key_value = valueDecrypted.intValue();
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;

                            }
                            BigInteger v = new BigInteger(HelpSerial.toString(value_to_reply_decript));
                            BigInteger vEncripted = HomoAdd.encrypt(v, pk);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (valueToCheck_HOMO_OPE_INT != Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    int key_value = ope.decrypt(Long.parseLong(dbAmount));
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;
                            }
                            long vEncripted = ope.encrypt(value_to_reply_decript);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }

                } else {
                    System.out.println("Not a valid Type");
                }


                break;

            // GREATER THAN >
            case 2:
                if (type.equals("HOMO_ADD")) {
                    if (valueToCheck_HOMO_ADD > Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    BigInteger valueToDecrypt = new BigInteger(dbAmount);
                                    BigInteger valueDecrypted = HomoAdd.decrypt(valueToDecrypt, pk);
                                    int key_value = valueDecrypted.intValue();
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;

                            }
                            BigInteger v = new BigInteger(HelpSerial.toString(value_to_reply_decript));
                            BigInteger vEncripted = HomoAdd.encrypt(v, pk);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (valueToCheck_HOMO_OPE_INT > Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    int key_value = ope.decrypt(Long.parseLong(dbAmount));
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;
                            }
                            long vEncripted = ope.encrypt(value_to_reply_decript);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }

                } else {
                    System.out.println("Not a valid Type");
                }


                break;

            // GREATER OR EQUAL THAN >=
            case 3:

                if (type.equals("HOMO_ADD")) {
                    if (valueToCheck_HOMO_ADD >= Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    BigInteger valueToDecrypt = new BigInteger(dbAmount);
                                    BigInteger valueDecrypted = HomoAdd.decrypt(valueToDecrypt, pk);
                                    int key_value = valueDecrypted.intValue();
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;

                            }
                            BigInteger v = new BigInteger(HelpSerial.toString(value_to_reply_decript));
                            BigInteger vEncripted = HomoAdd.encrypt(v, pk);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (valueToCheck_HOMO_OPE_INT >= Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {

                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;

                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    int key_value = ope.decrypt(Long.parseLong(dbAmount));
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;
                            }
                            long vEncripted = ope.encrypt(value_to_reply_decript);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }

                } else {
                    System.out.println("Not a valid Type");
                }


                break;

            // SMALLER THAN <
            case 4:

                if (type.equals("HOMO_ADD")) {
                    if (valueToCheck_HOMO_ADD < Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {
                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    BigInteger valueToDecrypt = new BigInteger(dbAmount);
                                    BigInteger valueDecrypted = HomoAdd.decrypt(valueToDecrypt, pk);
                                    int key_value = valueDecrypted.intValue();
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;

                            }
                            BigInteger v = new BigInteger(HelpSerial.toString(value_to_reply_decript));
                            BigInteger vEncripted = HomoAdd.encrypt(v, pk);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (valueToCheck_HOMO_OPE_INT < Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {

                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;

                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    int key_value = ope.decrypt(Long.parseLong(dbAmount));
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;
                            }
                            long vEncripted = ope.encrypt(value_to_reply_decript);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }

                } else {
                    System.out.println("Not a valid Type");
                }


                break;

            // SMALLER OR EQUAL THAN <=
            case 5:

                if (type.equals("HOMO_ADD")) {
                    if (valueToCheck_HOMO_ADD <= Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {

                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;
                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    BigInteger valueToDecrypt = new BigInteger(dbAmount);
                                    BigInteger valueDecrypted = HomoAdd.decrypt(valueToDecrypt, pk);
                                    int key_value = valueDecrypted.intValue();
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;

                            }
                            BigInteger v = new BigInteger(HelpSerial.toString(value_to_reply_decript));
                            BigInteger vEncripted = HomoAdd.encrypt(v, pk);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (valueToCheck_HOMO_OPE_INT <= Integer.parseInt(cond_val)) {
                        for (int i = 0; i < list.size(); i++) {

                            UpdateKeyValue currentObj = list.get(i);

                            switch (currentObj.getOp()) {

                                // SET
                                case 0:
                                    value_to_reply_decript = Integer.parseInt(currentObj.getValue());
                                    break;

                                // ADD
                                case 1:
                                    String dbAmount = key_value_list.get(i);
                                    int key_value = ope.decrypt(Long.parseLong(dbAmount));
                                    value_to_reply_decript = key_value + Integer.parseInt(currentObj.getValue());
                                    break;
                            }
                            long vEncripted = ope.encrypt(value_to_reply_decript);
                            value_to_reply_encript = HelpSerial.toString(vEncripted);
                            return_encripted_values_map.put(list.get(i).getKey(),value_to_reply_encript);
                        }
                    } else {
                        System.out.println("Condition not hold.");
                    }

                } else {
                    System.out.println("Not a valid Type");
                }
                break;


        }
        reply = new ReplyCondUpdate(return_encripted_values_map, nonce);


        return reply;
    }


    private PrivateKey getPrivKey() throws Exception {
        File sgxPrivateKey = new File("./sgxPrivateKey.txt");
        String privateKey = "";
        Scanner scanner = new Scanner(sgxPrivateKey);

        while (scanner.hasNextLine()) {
            privateKey = scanner.next();
            System.out.println("privateKey: " + privateKey);
        }

        byte[] sgxByte = Base64.getDecoder().decode(privateKey);
        PrivateKey sgxPrivate = PrivateKey.createKey(sgxByte);
        return sgxPrivate;
    }


    private HashMap<String, TypeAmount> turnDbBackToHashMap(String db) throws UnsupportedEncodingException {

        GsonBuilder gsonBuilder = new GsonBuilder();

        Gson gsonObject = gsonBuilder.create();
        //Gson gson = new Gson();

        String db_D = URLDecoder.decode(db, "UTF-8");

        System.out.println("db_GSON : " + db_D);


        Type empMapType = new TypeToken<HashMap<String, TypeAmount>>() {
        }.getType();
        HashMap<String, TypeAmount> db_filtered = gsonObject.fromJson(db_D, empMapType);
        System.out.println("OLA");
        //HashMap<String, TypeAmount> db_filtered = gsonObject.fromJson(db_D, HashMap.class);
        return db_filtered;

    }


}
