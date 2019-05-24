package rest.server;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import hj.mlib.HomoAdd;
import hj.mlib.HomoOpeInt;
import javassist.NotFoundException;
import model.OpType;
import model.TypeAmount;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaServer extends DefaultSingleRecoverable {

    private Map<String, TypeAmount> db = new ConcurrentHashMap<>();


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

        boolean hasReply = false;
        String replyR;
        Long nonce;

        //PHASE 2

        String type; //HOMO_ADD or HOMO_OPE_INT
        BigInteger nSquare;
        String encryptType;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            OpType reqType = (OpType) objIn.readObject();

            switch (reqType) {
                case ADD_MONEY:
                    System.out.println("cheguei a replica");
                    publicKey = (String) objIn.readObject();
                    value = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    type = (String) objIn.readObject();
                    encryptType = (String) objIn.readObject();
                    if (type.equals("HOMO_ADD")) {
                        System.out.println("Entrei no Homo_add");;
                        nSquare = (BigInteger) objIn.readObject();
                        replyR = selectionOfType_ADD(type, encryptType, publicKey, value, nSquare);

                    } else {
                        System.out.println("Entrei no Homo_ope ou Wallet");
                        replyR = selectionOfType_ADD(type, encryptType, publicKey, value, null);
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
                    value = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();

                    Double doubleValue = Double.parseDouble(value);

                    if (!db.containsKey(publicKey) || !db.containsKey(publicKey2)) {
                        System.out.println("User not found.");
                    } else if (doubleValue < 0) {
                        System.out.println("Invalid amount.");
                    } else {
                        Double doubleValueDb = Double.parseDouble(db.get(publicKey).getAmount());
                        Double doubleValueDb2 = Double.parseDouble(db.get(publicKey2).getAmount());
                        if (doubleValueDb >= doubleValue) {
                            Double transfSum = doubleValueDb2 + doubleValue;
                            Double transfMin = doubleValueDb - doubleValue;

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
                    break;

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
        Long higher;
        Long lower;
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
                    higher = (Long) objIn.readObject();
                    lower = (Long) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    type = (String) objIn.readObject();
                    encryptType = (String) objIn.readObject();

                    rec_val = selectionOfType_GET_LOWER_HIGHER(type, encryptType, "", higher, lower);
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





    private String selectionOfType_ADD(String type, String encryptType, String publicKey, String value, BigInteger nsquare) throws Exception {
        String ret = "";

        switch (type) {
            case "WALLET":
                if (encryptType.equals("SUM")) {
                    Double doubleValue = Double.parseDouble(value);

                    Double newValue = Double.parseDouble(db.get(publicKey).getAmount() + doubleValue);

                    TypeAmount aux = db.get(publicKey);
                    aux.setAmount(newValue.toString());
                    db.put(publicKey, aux);

                    ret = db.get(publicKey).getAmount();


                } else if(encryptType.equals("SET")) {
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
                    }
                }

                // CREATE
                else {
                    db.put(publicKey, new TypeAmount("WALLET", value));

                    ret = db.get(publicKey).getAmount();
                }
                break;

            case "HOMO_ADD":
                System.out.println("Tou no selectionType HOMO_ADD, vou reencaminhar po Encrypt Type "+encryptType);
                ret = selectionOfEncryptType(type, encryptType, publicKey, value, nsquare);
                break;

            case "HOMO_OPE_INT":
                System.out.println("Tou no selectionType HOMO_OPE_INT, vou reencaminhar po encrypt type "+encryptType);
                ret = selectionOfEncryptType(type, encryptType, publicKey, value, nsquare);
                break;

            default:
                System.out.println("Type not valid.");
                break;
        }




        return ret;
    }

    private String selectionOfEncryptType(String type, String encryptType, String publicKey, String value, BigInteger nSquare) throws Exception {

        switch (encryptType) {

            case "SUM":
                System.out.println("Entrei no SUM!!!");
                System.out.println("Vou dar sum de "+value);
                BigInteger BigIntegerValue = new BigInteger(value);
                if (type.equals("HOMO_ADD")) {
                    System.out.println("Confere, tipo homo_add");
                    //valid
                    if (db.containsKey(publicKey)) {
                        System.out.println("Database tem o user");
                        System.out.println("posso entao dar add do valor "+value);
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
                    Long valueDb = Long.parseLong(db.get(publicKey).getAmount());
                    //TODO send to sgx to process and return to client after


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
                System.out.println("Vou criar com o valor encriptado: " + value);
                db.put(publicKey, new TypeAmount(type, value));

                break;

            default:
                break;
        }
        System.out.println("going to return the value: " + db.get(publicKey).getAmount() + " back to the server");
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
            return "";
        }


        return reply;
    }

    @SuppressWarnings("Duplicates")
    private List<String> selectionOfType_GET_LOWER_HIGHER(String type, String encryptType, String publicKey, Long higher, Long lower) throws IOException {
        List<String> reply = new ArrayList<>();
        if (encryptType.equals("GET_LOWER_HIGHER")) {
            if (type.equals("HOMO_OPE_INT")) {


                List<String> returnList = new ArrayList<>();
                db.forEach((String key, TypeAmount value) -> {
                    Long res = Long.parseLong(value.getAmount());
                    if (res >= lower && res <= higher) {
                        returnList.add(key);
                    }
                });

                reply = returnList;

            } else if(type.equals("WALLET")){
                List<String> returnList= new ArrayList<>();
                db.forEach((String key, TypeAmount value) -> {
                    Long longValue = Long.parseLong(value.getAmount());

                    if(longValue >= lower && longValue <= higher){
                        returnList.add(key);
                    }
                });

                reply = returnList;
            }
            // HOMO_ADD
            else{
                //TODO send to be processed into SGX
            }
        }

        return reply;
    }

}
