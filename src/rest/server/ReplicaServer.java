package rest.server;


import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;
import hj.mlib.HomoAdd;
import model.OpType;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaServer extends DefaultSingleRecoverable {

    private Map<String, String> db = new ConcurrentHashMap<>();


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

    @Override
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext) {
        byte[] reply = null;
        String publicKey;
        String publicKey2;
        String value;

        boolean hasReply = false;
        Long nonce;

        //PHASE 2

        String type; //HOMO_ADD or HOMO_OPE_INT
        BigInteger nSquare;

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
                    nSquare = (BigInteger) objIn.readObject();


                    switch(type) {

                        case "WALLET":

                            Double doubleValue = Double.parseDouble(value);
                            if (db.containsKey(publicKey)) {
                                if (doubleValue >= 0) {
                                    Double newValue = Double.parseDouble(db.get(publicKey)) + doubleValue;
                                    db.put(publicKey, newValue.toString());
                                    // returns updated money
                                    objOut.writeObject(db.get(publicKey));
                                    objOut.writeObject(nonce);
                                    objOut.writeObject(type);

                                    hasReply = true;
                                } else {
                                    System.out.println("Invalid amount.");
                                }
                            } else {
                                db.put(publicKey, doubleValue.toString());
                                objOut.writeObject(db.get(publicKey));
                                objOut.writeObject(nonce);

                                hasReply = true;
                            }
                            break;
                        case "HOMO_ADD":
                            BigInteger BigIntegerValue = new BigInteger(value);
                            if (db.containsKey(publicKey)) {
                                BigInteger BigIntegerValueDb = new BigInteger(db.get(publicKey));

                                BigInteger sum = HomoAdd.sum(BigIntegerValue, BigIntegerValueDb, nSquare);
                                db.put(publicKey, sum.toString());

                            } else {
                                db.put(publicKey, BigIntegerValue.toString());
                                objOut.writeObject(db.get(publicKey));
                                objOut.writeObject(nonce);

                                hasReply = true;
                            }

                            break;

                        case "HOMO_OPE_INT":

                            //Long longValue = Long.getLong(value);
                            if (db.containsKey(publicKey)) {
                                db.put(publicKey, value);


                            }
                            break;
                    }
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
                        Double doubleValueDb = Double.parseDouble(db.get(publicKey));
                        Double doubleValueDb2 = Double.parseDouble(db.get(publicKey2));
                        if (doubleValueDb >= doubleValue) {
                            Double transfSum = doubleValueDb2 + doubleValue;
                            Double transfMin = doubleValueDb - doubleValue;
                            db.put(publicKey, transfMin.toString());
                            db.put(publicKey2, transfSum.toString());
                            objOut.writeObject(db.get(publicKey2));
                            //System.out.println("User " + publicKey2 + " now has " + db.get(publicKey2) + "€");
                            objOut.writeObject(nonce);

                            hasReply = true;
                        } else {
                            System.out.println("User making the transfer does not have enough money.");
                        }
                    }
                    break;

                /*case CREATE:
                    publicKey = (String) objIn.readObject();
                    initValue = (Double) objIn.readObject();
                    type = (String) objIn.readObject();

                    break;*/

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
        int higher = Integer.MAX_VALUE;
        int lower= Integer.MIN_VALUE;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            OpType reqType = (OpType) objIn.readObject();
            switch (reqType) {


                case GET_MONEY:
                    publicKey = (String) objIn.readObject();
                    nonce = (Long) objIn.readObject();
                    higher = (Integer) objIn.readObject();
                    lower = (Integer) objIn.readObject();
                    boolean moreThanOne = false;

                    if( higher != Integer.MAX_VALUE && lower != Integer.MIN_VALUE ){
                        // HOMO_OPE_INT
                        moreThanOne = true;
                        int iterationKey = lower;
                        List<String> moneyList = new ArrayList<String>();

                        objOut.writeObject(moreThanOne);

                        // prepara output - lista de moneys de todas as contas entre low e high
                        while(iterationKey <=higher){
                            if(db.containsKey(iterationKey)){
                                moneyList.add(db.get(iterationKey));
                            }
                            iterationKey++;
                        }
                        objOut.writeObject(moneyList);
                    }
                    else {
                        // WALLET
                        if (db.containsKey(publicKey)) {
                            //System.out.println("Amount: " + db.get(publicKey));
                            objOut.writeObject(moreThanOne);
                            objOut.writeObject(db.get(publicKey));
                            objOut.writeObject(nonce);

                            hasReply = true;
                        } else {
                            System.out.println("User not found in the database.");
                        }
                    }
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

}

