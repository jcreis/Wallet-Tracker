package rest.server;

import api.*;
import bftsmart.tom.MessageContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.server.defaultservices.DefaultSingleRecoverable;

import java.io.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReplicaServer extends DefaultSingleRecoverable {

    private Map<String, User> db = new ConcurrentHashMap<String, User>();



    public ReplicaServer(int id){
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
    public byte[] appExecuteOrdered(byte[] bytes, MessageContext messageContext){
        byte[] reply = null;
        String key1;
        String key2;
        Double value;
        User user;
        boolean hasReply = false;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

            WalletResources.opType reqType = (WalletResources.opType)objIn.readObject();

            switch (reqType) {
                case ADD_MONEY:
                    key1 = (String)objIn.readObject();
                    value = (Double)objIn.readObject();

                    if(db.containsKey(key1)) {
                        if (value >= 0){
                            db.get(key1).addMoney(value);
                            // returns updated money
                            objOut.writeObject(db.get(key1).getMoney());
                            hasReply = true;
                        }
                        else {
                            System.out.println("Invalid amount.");
                        }
                    }
                    else{
                        System.out.println("User not found.");
                    }
                    break;



                case ADD_USER:
                    user = (User)objIn.readObject();

                    if(!db.containsKey(user.getId())) {
                        db.put(user.getId(), user);
                        System.out.println("User " + user.getId() + " added to Database.");

                        objOut.writeObject(user);
                        hasReply = true;

                    }
                    else{
                        System.out.println("User already exists in the database.");
                    }

                    break;


                case TRANSFER:
                    key1 = (String)objIn.readObject();
                    key2 = (String)objIn.readObject();
                    value = (Double)objIn.readObject();


                    if(!db.containsKey(key1) || !db.containsKey(key2)){
                        System.out.println("User not found.");
                    }
                    else if(value < 0){
                        System.out.println("Invalid amount.");
                    }
                    else{
                        User u1 = db.get(key1);
                        User u2 = db.get(key2);
                        if(u1.getMoney()>=value){
                            u1.setMoney(u1.getMoney() - value);
                            u2.setMoney(u2.getMoney() + value);
                            db.put(u1.getId(), u1);
                            db.put(u2.getId(), u2);

                            objOut.writeObject(u2.getMoney());
                            System.out.println("User "+u2.getId() + " now has "+u2.getMoney()+"€");
                            hasReply = true;
                        }
                        else{
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
            System.out.println("dadsasdadadad");
        }
        return reply;
    }

    @SuppressWarnings("unchecked")
    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        byte[] reply = null;
        boolean hasReply = false;
        String key1;
        Long nonce;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(command);
             ObjectInput objIn = new ObjectInputStream(byteIn);
             ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutput objOut = new ObjectOutputStream(byteOut);) {
            WalletResources.opType reqType = (WalletResources.opType)objIn.readObject();
            nonce = (Long)objIn.readObject();
            switch (reqType) {
                case GET_USERS:
                    System.out.println("List of users: "+ db.values().toArray(new User[db.size()]).toString());

                    // Prepares output
                    objOut.writeObject(db.values().toArray(new User[db.size()]));

                    objOut.writeObject(nonce);
                    // Allows output to be set to ByteArray in the final phase of the method
                    hasReply = true;
                    break;

                case GET_MONEY:
                    key1 = (String)objIn.readObject();

                    if(db.containsKey(key1)) {
                        System.out.println("Amount: " + db.get(key1).getMoney());

                        objOut.writeObject(db.get(key1).getMoney());
                        hasReply = true;
                    }
                    else {
                        System.out.println("User not found in the database.");
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

