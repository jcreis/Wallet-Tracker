package rest.server;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import hj.mlib.HomoAdd;
import hj.mlib.HomoOpeInt;
import model.OpType;
import model.TypeAmount;

import java.io.*;
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
                        nSquare = (BigInteger) objIn.readObject();
                        replyR = selectionOfType_ADD(type, encryptType, publicKey, value, nSquare);

                    } else {
                        System.out.println("selection_ADD");
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

        } catch (IOException | ClassNotFoundException e) {
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
        String msg;
        Double higher;
        Double lower;
        String type;
        String encryptType;


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

                    hasReply = selectionOfType_GET(objOut, type, encryptType, publicKey, nonce, 0.0, 0.0);

                case GET_MONEY_OPE:
                    higher = (Double) objIn.readObject();
                    lower = (Double) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    type = (String) objIn.readObject();
                    encryptType = (String) objIn.readObject();

                    hasReply = selectionOfType_GET(objOut, type, encryptType, "", nonce, higher, lower);

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

    private String selectionOfType_ADD(String type, String encryptType, String publicKey, String value, BigInteger nsquare) throws IOException {
        String ret = "";

        switch (type) {
            case "WALLET":
                if (encryptType.equals("SUM")) {
                    System.out.println("Não pode fazer a operação");
                } else {
                    Double doubleValue = Double.parseDouble(value);
                    System.out.println("doubleValue = " + doubleValue);
                    if (db.containsKey(publicKey)) {
                        if (doubleValue >= 0) {
                            Double newValue = Double.parseDouble(db.get(publicKey).getAmount()) + doubleValue;
                            db.put(publicKey, new TypeAmount(type, newValue.toString()));
                            ret = db.get(publicKey).getAmount();
                            // returns updated money


                        } else {
                            System.out.println("Account doesnt exists in the database");
                        }
                    } else {
                        db.put(publicKey, new TypeAmount("WALLET", value));
                        ret = db.get(publicKey).getAmount();
                    }
                }
                break;

            case "HOMO_ADD":
                ret = selectionOfEncryptType(type, encryptType, publicKey, value, nsquare);
                break;

            case "HOMO_OPE_INT":
                ret = selectionOfEncryptType(type, encryptType, publicKey, value, nsquare);
                break;

            default:
                System.out.println("Type not valid.");
                break;

        }

        return ret;
    }

    private String selectionOfEncryptType(String type, String encryptType, String publicKey, String value, BigInteger nSquare) throws IOException {
        boolean hasReply = false;
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
                    }

                } else {
                    System.out.println("This type of encryption is not supported to HOMO_OPE_INT");
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
                    }
                } else if (type.equals("HOMO_OPE_INT")) {
                    if (db.containsKey(publicKey)) {
                        db.put(publicKey, new TypeAmount(type, value));

                    } else {
                        System.out.println("Account doenst exists in the database");
                    }
                } else {
                    System.out.println("Type not supported.");
                }

                break;

            case "CREATE":

                    db.put(publicKey, new TypeAmount(type, value));



                break;

            default:
                break;
        }
        System.out.println("going to return " + db.get(publicKey).getAmount());
        return db.get(publicKey).getAmount();

    }

    private boolean selectionOfType_GET(ObjectOutput objOut, String type, String encryptType, String publicKey, Long nonce, Double higher, Double lower) throws IOException {
        boolean hasReply = false;

        if (encryptType.equals("GET_LOWER_HIGHER")) {
            if (type.equals("HOMO_OPE_INT")) {

                List<String> keyList = new ArrayList<>();
                for (String key : db.keySet()) {
                    keyList.add(key);
                }
                List<String> returnList = new ArrayList<>();
                for (int i = 0; i < keyList.size(); i++) {
                    Double doubleValue = Double.parseDouble(db.get(i).getAmount());
                    if (doubleValue >= lower && doubleValue <= higher) {
                        returnList.add(keyList.get(i));
                    }
                }
                objOut.writeObject(returnList);
                objOut.writeObject(nonce);

                hasReply = true;
            } else {
                System.out.println("Operation not supported in that type.");
            }
        } else {

            if (db.containsKey(publicKey)) {
                //System.out.println("Amount: " + db.get(publicKey));
                objOut.writeObject(db.get(publicKey).getAmount());
                objOut.writeObject(nonce);

                hasReply = true;
            } else {
                System.out.println("User not found in the database.");
            }

        }

        return hasReply;
    }

}
