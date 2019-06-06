package rest.server;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import client.AppClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import model.*;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaServer extends DefaultSingleRecoverable {

    private Map<String, TypeAmount> db = new ConcurrentHashMap<>();
    private Map<String, TypeKey> keyData = new ConcurrentHashMap<>();


    public ReplicaServer(int id) {
        new ServiceReplica(id, this, this);
    }


    @Override
    public void installSnapshot(byte[] bytes) {

    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @SuppressWarnings("Duplicates")
    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        byte[] reply = null;
        String publicKey;
        String publicKey2;
        String value;
        Double transferValue;

        boolean hasReply = false;
        String replyR = "";
        Long nonce;

        //PHASE 2

        String type; //HOMO_ADD or HOMO_OPE_INT
        BigInteger nSquare;
        String encryptType;
        String homoAddKey;
        String homoOpeIntKey;
        String encodedSecKey;
        String cond_key;
        String cond_val;
        Integer cond_number;
        String op_list;
        ArrayList<UpdateKeyValue> list;



        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            OpType reqType = (OpType) objIn.readObject();

            switch (reqType) {
                case ADD_MONEY:

                    publicKey = (String) objIn.readObject();
                    value = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    type = (String) objIn.readObject();
                    encryptType = (String) objIn.readObject();


                    if (type.equals("HOMO_ADD")) {

                        nSquare = (BigInteger) objIn.readObject();
                        homoAddKey = (String) objIn.readObject();
                        encodedSecKey = (String) objIn.readObject();
                        keyData.put(publicKey, new TypeKey("HOMO_ADD", homoAddKey, encodedSecKey));
                        replyR = selectionOfType_ADD(type, encryptType, publicKey, value, nSquare, nonce);

                    } else if ((type.equals("HOMO_OPE_INT"))) {
                        homoOpeIntKey = (String) objIn.readObject();
                        encodedSecKey = (String) objIn.readObject();




                        keyData.put(publicKey, new TypeKey("HOMO_OPE_INT", homoOpeIntKey, encodedSecKey));
                        replyR = selectionOfType_ADD(type, encryptType, publicKey, value, null, nonce);

                    } else {

                        replyR = selectionOfType_ADD(type, encryptType, publicKey, value, null, nonce);
                    }


                    System.out.println("Replica vou devolver amount - " + replyR);
                    objOut.writeObject(replyR);
                    objOut.writeObject(nonce);
                    objOut.writeObject(type);

                    hasReply = true;
                    break;


                case TRANSFER:

                    publicKey = (String) objIn.readObject();
                    publicKey2 = (String) objIn.readObject();
                    transferValue = (Double) objIn.readObject();
                    nonce = (Long) objIn.readObject();

                    if(db.get(publicKey).getType().equals("WALLET") && db.get(publicKey2).getType().equals("WALLET")) {


                        if (!db.containsKey(publicKey) || !db.containsKey(publicKey2)) {
                            System.out.println("User not found.");
                        } else if (transferValue < 0) {
                            System.out.println("Invalid amount.");
                        } else {
                            Double doubleValueDb = Double.parseDouble(db.get(publicKey).getAmount());
                            Double doubleValueDb2 = Double.parseDouble(db.get(publicKey2).getAmount());
                            if (doubleValueDb >= transferValue) {
                                Double transfSum = doubleValueDb2 + transferValue;
                                Double transfMin = doubleValueDb - transferValue;

                                db.put(publicKey, new TypeAmount("WALLET", transfMin.toString()));
                                db.put(publicKey2, new TypeAmount("WALLET", transfSum.toString()));
                                objOut.writeObject(db.get(publicKey2).getAmount());
                                //System.out.println("User " + publicKey2 + " now has " + db.get(publicKey2) + "€");
                                objOut.writeObject(nonce);

                                hasReply = true;
                            } else {
                                System.out.println("User making the transfer does not have enough money.");
                            }
                        }
                    }else{
                        System.out.println("Type not Supported");
                    }
                    break;

                case COND_UPD:

                    cond_key = (String) objIn.readObject();
                    cond_val = (String) objIn.readObject();
                    cond_number = (Integer) objIn.readObject();
                    op_list = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gsonObject = gsonBuilder.create();
                    String list_D = URLDecoder.decode(op_list, "UTF-8");



                    Type empMapType = new TypeToken<List<UpdateKeyValue>>() {
                    }.getType();

                    list = gsonObject.fromJson(list_D, empMapType);

                    String db_type = db.get(cond_key).getType();

                    List<String> updKey_valueList = new ArrayList<>();
                    ReplyCondUpdate r_cond = null;

                    HashMap<String, String> return_wallet_map = new HashMap<>();


                    switch (cond_number) {
                        // EQUALS =
                        case 0:
                            if (db_type.equals("WALLET")) {
                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());

                                    UpdateKeyValue currentObj = list.get(i);
                                    Double db_val = Double.parseDouble(db.get(cond_key).getAmount());

                                    if (db_val == Double.parseDouble(cond_val)) {

                                        // SET
                                        if (currentObj.getOp() == 0) {
                                            db.get(currentObj.getKey()).setAmount(currentObj.getValue());
                                        }
                                        // ADD
                                        else {
                                            Double finalAddResult = db_val + Double.parseDouble(currentObj.getValue());
                                            db.get(currentObj.getKey()).setAmount(finalAddResult.toString());
                                        }
                                    } else {
                                        System.out.println("Condition not hold.");
                                    }
                                }
                            } else {

                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());
                                }

                                r_cond = condUpdateRequesttoSgx(db_type, cond_key, cond_val, cond_number, op_list, nonce, db.get(cond_key).getAmount(), updKey_valueList, keyData.get(cond_key).getKey(), keyData.get(cond_key).getAes());

                                r_cond.getReturnMap().forEach((String key, String newValue) -> {
                                    db.get(key).setAmount(newValue);
                                    return_wallet_map.put(key, db.get(key).getAmount());

                                });

                            }
                            objOut.writeObject(return_wallet_map);
                            objOut.writeObject(nonce);

                            hasReply = true;

                            break;

                        // NOT EQUALS !=
                        case 1:
                            if (db_type.equals("WALLET")) {
                                for (int i = 0; i < list.size(); i++) {

                                    UpdateKeyValue currentObj = list.get(i);
                                    Double db_val = Double.parseDouble(db.get(cond_key).getAmount());

                                    if (db_val != Double.parseDouble(cond_val)) {

                                        // SET
                                        if (currentObj.getOp() == 0) {
                                            db.get(currentObj.getKey()).setAmount(currentObj.getValue());
                                        }
                                        // ADD
                                        else {
                                            Double finalAddResult = db_val + Double.parseDouble(currentObj.getValue());
                                            db.get(currentObj.getKey()).setAmount(finalAddResult.toString());
                                        }
                                    } else {
                                        System.out.println("Condition not hold.");
                                    }
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());
                                }
                                r_cond = condUpdateRequesttoSgx(db_type, cond_key, cond_val, cond_number, op_list, nonce, db.get(cond_key).getAmount(), updKey_valueList, keyData.get(cond_key).getKey(), keyData.get(cond_key).getAes());
                                r_cond.getReturnMap().forEach((String key, String newValue) -> {
                                    db.get(key).setAmount(newValue);
                                    return_wallet_map.put(key, db.get(key).getAmount());

                                });
                            }
                            objOut.writeObject(return_wallet_map);
                            objOut.writeObject(nonce);

                            hasReply = true;
                            break;

                        // GREATER THAN >
                        case 2:
                            if (db_type.equals("WALLET")) {
                                for (int i = 0; i < list.size(); i++) {

                                    UpdateKeyValue currentObj = list.get(i);
                                    Double db_val = Double.parseDouble(db.get(cond_key).getAmount());

                                    if (db_val > Double.parseDouble(cond_val)) {

                                        // SET
                                        if (currentObj.getOp() == 0) {
                                            db.get(currentObj.getKey()).setAmount(currentObj.getValue());
                                        }
                                        // ADD
                                        else {
                                            Double finalAddResult = db_val + Double.parseDouble(currentObj.getValue());
                                            db.get(currentObj.getKey()).setAmount(finalAddResult.toString());
                                        }
                                    } else {
                                        System.out.println("Condition not hold.");
                                    }
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());
                                }
                                ReplyCondUpdate reply_S = condUpdateRequesttoSgx(db_type, cond_key, cond_val, cond_number, op_list, nonce, db.get(cond_key).getAmount(), updKey_valueList, keyData.get(cond_key).getKey(), keyData.get(cond_key).getAes());
                                reply_S.getReturnMap().forEach((String key, String newValue) -> {
                                    db.get(key).setAmount(newValue);
                                    return_wallet_map.put(key, db.get(key).getAmount());

                                });
                            }
                            objOut.writeObject(return_wallet_map);
                            objOut.writeObject(nonce);

                            hasReply = true;
                            break;

                        // GREATER OR EQUAL THAN >=
                        case 3:
                            if (db_type.equals("WALLET")) {
                                for (int i = 0; i < list.size(); i++) {

                                    UpdateKeyValue currentObj = list.get(i);
                                    Double db_val = Double.parseDouble(db.get(cond_key).getAmount());

                                    if (db_val >= Double.parseDouble(cond_val)) {

                                        // SET
                                        if (currentObj.getOp() == 0) {
                                            db.get(currentObj.getKey()).setAmount(currentObj.getValue());
                                        }
                                        // ADD
                                        else {
                                            Double finalAddResult = db_val + Double.parseDouble(currentObj.getValue());
                                            db.get(currentObj.getKey()).setAmount(finalAddResult.toString());
                                        }
                                    } else {
                                        System.out.println("Condition not hold.");
                                    }
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());
                                }
                                ReplyCondUpdate reply_S = condUpdateRequesttoSgx(db_type, cond_key, cond_val, cond_number, op_list, nonce, db.get(cond_key).getAmount(), updKey_valueList, keyData.get(cond_key).getKey(), keyData.get(cond_key).getAes());
                                reply_S.getReturnMap().forEach((String key, String newValue) -> {
                                    db.get(key).setAmount(newValue);
                                    return_wallet_map.put(key, db.get(key).getAmount());

                                });
                            }
                            objOut.writeObject(return_wallet_map);
                            objOut.writeObject(nonce);

                            hasReply = true;
                            break;

                        // SMALLER THAN <
                        case 4:

                            if (db_type.equals("WALLET")) {
                                for (int i = 0; i < list.size(); i++) {

                                    UpdateKeyValue currentObj = list.get(i);
                                    Double db_val = Double.parseDouble(db.get(cond_key).getAmount());

                                    if (db_val < Double.parseDouble(cond_val)) {

                                        // SET
                                        if (currentObj.getOp() == 0) {
                                            db.get(currentObj.getKey()).setAmount(currentObj.getValue());
                                        }
                                        // ADD
                                        else {
                                            Double finalAddResult = db_val + Double.parseDouble(currentObj.getValue());
                                            db.get(currentObj.getKey()).setAmount(finalAddResult.toString());
                                        }
                                    } else {
                                        System.out.println("Condition not hold.");
                                    }
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());
                                }
                                ReplyCondUpdate reply_S = condUpdateRequesttoSgx(db_type, cond_key, cond_val, cond_number, op_list, nonce, db.get(cond_key).getAmount(), updKey_valueList, keyData.get(cond_key).getKey(), keyData.get(cond_key).getAes());
                                reply_S.getReturnMap().forEach((String key, String newValue) -> {
                                    db.get(key).setAmount(newValue);
                                    return_wallet_map.put(key, db.get(key).getAmount());

                                });
                            }
                            objOut.writeObject(return_wallet_map);
                            objOut.writeObject(nonce);

                            hasReply = true;
                            break;

                        // SMALLER OR EQUAL THAN <=
                        case 5:
                            if (db_type.equals("WALLET")) {
                                for (int i = 0; i < list.size(); i++) {

                                    UpdateKeyValue currentObj = list.get(i);
                                    Double db_val = Double.parseDouble(db.get(cond_key).getAmount());

                                    if (db_val <= Double.parseDouble(cond_val)) {

                                        // SET
                                        if (currentObj.getOp() == 0) {
                                            db.get(currentObj.getKey()).setAmount(currentObj.getValue());
                                        }
                                        // ADD
                                        else {
                                            Double finalAddResult = db_val + Double.parseDouble(currentObj.getValue());
                                            db.get(currentObj.getKey()).setAmount(finalAddResult.toString());
                                        }
                                    } else {
                                        System.out.println("Condition not hold.");
                                    }
                                }
                            } else {
                                for (int i = 0; i < list.size(); i++) {

                                    updKey_valueList.add(db.get(list.get(i).getKey()).getAmount());
                                }
                                ReplyCondUpdate reply_S = condUpdateRequesttoSgx(db_type, cond_key, cond_val, cond_number, op_list, nonce, db.get(cond_key).getAmount(), updKey_valueList, keyData.get(cond_key).getKey(), keyData.get(cond_key).getAes());

                                reply_S.getReturnMap().forEach((String key, String newValue) -> {
                                    db.get(key).setAmount(newValue);
                                    return_wallet_map.put(key, db.get(key).getAmount());

                                });
                            }
                            objOut.writeObject(return_wallet_map);
                            objOut.writeObject(nonce);

                            hasReply = true;
                            break;

                    }


            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }

        } catch (Exception e) {
            System.out.println("Exception");
        }

        return reply;
    }


    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;

        String publicKey;
        Long nonce;
        String higher;
        String lower;
        String type;
        String encryptType;
        List<String> rec_val;
        String rec2_val;


        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            OpType reqType = (OpType) objIn.readObject();
            switch (reqType) {


                case GET_MONEY:
                    publicKey = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    type = (String) objIn.readObject();
                    encryptType = (String) objIn.readObject();

                    rec2_val = selectionOfType_GET(type, encryptType, publicKey, 0.0, 0.0);

                    objOut.writeObject(rec2_val);
                    objOut.writeObject(nonce);
                    hasReply = true;
                    break;

                case GET_LOW_HIGH:
                    publicKey = (String) objIn.readObject();
                    higher = (String) objIn.readObject();
                    lower = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    type = (String) objIn.readObject();
                    encryptType = (String) objIn.readObject();

                    rec_val = selectionOfType_GET_LOWER_HIGHER(type, encryptType, publicKey, higher, lower, nonce);
                    objOut.writeObject(rec_val);
                    objOut.writeObject(nonce);
                    hasReply = true;
                    break;

                default:
                    System.out.println("error");


            }
            if (hasReply) {
                objOut.flush();
                byteOut.flush();
                reply = byteOut.toByteArray();
            } else {
                reply = new byte[0];
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e);
        }

        return reply;
    }


    private String selectionOfType_ADD(String type, String encryptType, String publicKey, String value, BigInteger nsquare, Long nonce) throws Exception {
        String ret = "";

        switch (type) {
            case "WALLET":
                if (encryptType.equals("SUM")) {

                    if (db.containsKey(publicKey)) {
                        Double doubleValue = Double.parseDouble(value);
                        Double newValue = Double.parseDouble(db.get(publicKey).getAmount() + doubleValue);
                        TypeAmount aux = db.get(publicKey);
                        aux.setAmount(newValue.toString());
                        db.put(publicKey, aux);
                        ret = db.get(publicKey).getAmount();
                    } else {
                        System.out.println("User not found in the database");
                        ret = "-1";
                    }
                } else if (encryptType.equals("SET")) {
                    Double doubleValue = Double.parseDouble(value);

                    if (db.containsKey(publicKey)) {
                        if (doubleValue >= 0) {
                            TypeAmount aux = db.get(publicKey);
                            aux.setAmount(value);
                            db.put(publicKey, aux);

                            ret = db.get(publicKey).getAmount();
                            // returns updated money


                        } else {
                            System.out.println("Value not valid");
                        }
                    } else {
                        System.out.println("User does not exists in the database.");
                        ret = "-1";
                    }
                }

                // CREATE
                else {
                    db.put(publicKey, new TypeAmount("WALLET", value));

                    ret = db.get(publicKey).getAmount();
                }
                break;

            case "HOMO_ADD":
                ret = selectionOfEncryptType(type, encryptType, publicKey, value, nsquare, nonce);
                break;

            case "HOMO_OPE_INT":
                ret = selectionOfEncryptType(type, encryptType, publicKey, value, nsquare, nonce);
                break;

            default:
                System.out.println("Type not valid.");
                break;
        }


        return ret;
    }

    @SuppressWarnings("Duplicates")
    private String selectionOfEncryptType(String type, String encryptType, String publicKey, String value, BigInteger nSquare, Long nonce) throws Exception {

        switch (encryptType) {

            case "SUM":

                BigInteger BigIntegerValue = new BigInteger(value);
                if (type.equals("HOMO_ADD")) {

                    //valid
                    if (db.containsKey(publicKey)) {

                        BigInteger BigIntegerValueDb = new BigInteger(db.get(publicKey).getAmount());

                        BigInteger sum = HomoAdd.sum(BigIntegerValue, BigIntegerValueDb, nSquare);
                        db.put(publicKey, new TypeAmount(type, sum.toString()));

                    } else {
                        System.out.println("User not in the database.");
                        return "-1";
                    }

                }


                // HOMO OPE INT -> SUM
                else {
                    Client client = ClientBuilder.newBuilder()
                            .hostnameVerifier(new AppClient.InsecureHostnameVerifier())
                            .build();
                    URI baseURI = UriBuilder.fromUri("https://localhost:8000/").build();
                    WebTarget target = client.target(baseURI);

                    Response response;
                    ReplySGX r;

                    Long balance = 0l;

                    if (!db.containsKey(publicKey)) {
                    }

                    if (db.get(publicKey).getType().equals("HOMO_OPE_INT")) {
                        balance = Long.parseLong(db.get(publicKey).getAmount());
                    }

                    response = target.path("/sgx")
                            .queryParam("balance", balance)
                            .queryParam("value", value)
                            .queryParam("nonce", nonce)
                            .queryParam("type", type)
                            .queryParam("encryptType", encryptType)
                            .queryParam("sgxKey", keyData.get(publicKey).getKey())
                            .queryParam("aesKey", keyData.get(publicKey).getAes())
                            .request()
                            .post(Entity.entity(ReplySGX.class, MediaType.APPLICATION_JSON));
                    r = response.readEntity(ReplySGX.class);


                }
                break;
            case "SET":

                if (type.equals("HOMO_ADD")) {

                    if (db.containsKey(publicKey)) {

                        TypeAmount aux = db.get(publicKey);
                        aux.setAmount(value);
                        db.put(publicKey, aux);


                    } else {
                        System.out.println("Account doesnt exists in the database");
                        return "-1";
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (db.containsKey(publicKey)) {
                        TypeAmount aux = db.get(publicKey);
                        aux.setAmount(value);
                        db.put(publicKey, aux);

                    } else {
                        System.out.println("Account doenst exists in the database");
                        return "-1";
                    }
                } else {
                    System.out.println("Type not supported.");
                }

                break;

            case "CREATE":
                // para os 2 casos HOMO ADD && HOMO OPE INT

                db.put(publicKey, new TypeAmount(type, value));

                break;

            default:
                break;
        }
        //System.out.println("going to return the value: " + db.get(publicKey).getAmount() + " back to the server");
        return db.get(publicKey).getAmount();

    }

    @SuppressWarnings("Duplicates")
    private String selectionOfType_GET(String type, String encryptType, String publicKey, Double higher, Double lower) throws IOException {
        String reply;

        // Igual para todos os tipos WALLET && HOMO ADD && HOMO OPE INT
        // Quem desencripta quando tem que desencriptar é o client
        if (db.containsKey(publicKey)) {
            //System.out.println("Amount: " + db.get(publicKey));
            reply = db.get(publicKey).getAmount();
        } else {

            System.out.println("User not found in the database.");
            reply = "-1";
        }


        return reply;
    }

    @SuppressWarnings("Duplicates")
    private List<String> selectionOfType_GET_LOWER_HIGHER(String type, String encryptType, String publicKey, String higher, String lower, Long nonce) throws IOException {
        List<String> reply = new ArrayList<>();
        if (encryptType.equals("GET_LOWER_HIGHER")) {
            if (type.equals("HOMO_OPE_INT")) {


                List<String> returnList = new ArrayList<>();
                db.forEach((String key, TypeAmount value) -> {

                    BigInteger res = new BigInteger(value.getAmount());

                    if (    (res.compareTo((new BigInteger(lower))) == 0 ||
                            res.compareTo (new BigInteger(lower)) == 1) &&
                            (res.compareTo (new BigInteger(higher)) == 0 ||
                                    (res.compareTo (new BigInteger(higher)) == -1))
                    ) {

                        returnList.add(key);
                    }
                    else{

                    }
                });

                reply = returnList;


            } else if (type.equals("WALLET")) {
                List<String> returnList = new ArrayList<>();
                db.forEach((String key, TypeAmount value) -> {
                    Double longValue = Double.parseDouble(value.getAmount());

                    if (longValue >= Double.parseDouble(lower) && longValue <= Double.parseDouble(higher)) {
                        returnList.add(key);
                    }
                });

                reply = returnList;
            }
            // HOMO_ADD
            else {
                if (type.equals("HOMO_ADD")) {

                    HashMap<String, TypeAmount> db_filteredByType = new HashMap<String, TypeAmount>();

                    db.forEach((String key, TypeAmount value) -> {
                        System.out.println("enters homo_add get_between");
                        System.out.println("its preparing request to sgx");
                        if (value.getType().equals("HOMO_ADD")) {
                            db_filteredByType.put(key, value);
                        }
                    });

                    Client client = ClientBuilder.newBuilder()
                            .hostnameVerifier(new AppClient.InsecureHostnameVerifier())
                            .build();
                    URI baseURI = UriBuilder.fromUri("https://localhost:8000/").build();
                    WebTarget target = client.target(baseURI);

                    Response response;
                    ReplySGX r;

                    GsonBuilder gsonBuilder = new GsonBuilder();
                    Gson gsonObject = gsonBuilder.create();
                    String dbJson_S = gsonObject.toJson(db_filteredByType);

                    String dbJson = URLEncoder.encode(dbJson_S, "UTF-8");


                    /*System.out.println("SENDING GET LOW HIGH TO SGX");
                    System.out.println("target is " + target);
                    System.out.println("higher -> " + higher);
                    System.out.println("lower -> " + lower);
                    System.out.println("type -> " + type);
                    System.out.println("encrypt type -> " + encryptType);
                    System.out.println("db -> " + dbJson);
                    System.out.println("sgxKey -> " + keyData.get(publicKey).getKey());*/


                    response = target.path("/sgx")
                            .queryParam("higher", higher)
                            .queryParam("lower", lower)
                            .queryParam("nonce", nonce)
                            .queryParam("type", type)
                            .queryParam("encryptType", encryptType)
                            .queryParam("db", dbJson)
                            .queryParam("sgxKey", keyData.get(publicKey).getKey())
                            .queryParam("aesKey", keyData.get(publicKey).getAes())
                            .request()
                            .get();
                    r = response.readEntity(ReplySGX.class);


                    if (r.getReturnList().isEmpty()) {

                    } else {
                       /* System.out.println("Received from SGX the Keys : \n" + r.getReturnList());*/
                        reply = r.getReturnList();
                    }


                }
            }
        }

        return reply;
    }

    private ReplyCondUpdate condUpdateRequesttoSgx(String type, String cond_key, String cond_value, int cond_number, String op_list, Long nonce, String amountToCompare, List<String> key_value_list, String sgxKey, String aesKey) throws UnsupportedEncodingException {



        Response response;
        ReplyCondUpdate r;
        Client client = ClientBuilder.newBuilder()
                .hostnameVerifier(new AppClient.InsecureHostnameVerifier())
                .build();
        URI baseURI = UriBuilder.fromUri("https://localhost:8000/").build();
        WebTarget target = client.target(baseURI);

        /*System.out.println("VOU ENVIAR para " + target + "/update");
        System.out.println("TYPE: " + type);
        System.out.println("COND_KEY: " + cond_key);
        System.out.println("COND_VALUE: " + cond_value);
        System.out.println("NUMBER:" + cond_number);
        System.out.println("LIST:" + op_list);
        System.out.println("NONCEE: " + nonce);
        System.out.println("AMOUNT: " +amountToCompare);
        System.out.println("VALUE LIST: " + key_value_list);
        System.out.println("SGX: " + sgxKey);
        System.out.println("AES: " + aesKey);*/

        String op = URLEncoder.encode(op_list, "UTF-8");
        try {


            response = target.path("/sgx/op")
                    .queryParam("type", type)
                    .queryParam("cond_key", cond_key)
                    .queryParam("cond_value", cond_value)
                    .queryParam("cond_number", cond_number)
                    .queryParam("op_list", op)
                    .queryParam("nonce", nonce)
                    .queryParam("amountToCompare", amountToCompare)
                    .queryParam("key_value_list", key_value_list)
                    .queryParam("sgxKey", sgxKey)
                    .queryParam("aesKey", aesKey)
                    .request()
                    .post(Entity.entity(ReplyCondUpdate.class, MediaType.APPLICATION_JSON));
            r = response.readEntity(ReplyCondUpdate.class);
        }catch(Exception e){
            e.printStackTrace();
            r = null;
        }






        return r;
    }
}


