package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Reply implements Serializable {

    ArrayList<ReplicaResponseMessage> messages;
    String publicKey;
    String amount;
    long nonce;

    OpType operationType;




    public Reply(OpType operationType, ArrayList<ReplicaResponseMessage> messages, String publicKey, String amount, long nonce){
        this.messages = messages;
        this.publicKey = publicKey;
        this.amount = amount;
        this.nonce = nonce;
        this.operationType = operationType;

    }



    public Reply() {
    }

    public ArrayList<ReplicaResponseMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<ReplicaResponseMessage> messages) {
        this.messages = messages;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }

    public OpType getOperationType() {
        return operationType;
    }

    public void setOperationType(OpType operationType) {
        this.operationType = operationType;
    }
}
