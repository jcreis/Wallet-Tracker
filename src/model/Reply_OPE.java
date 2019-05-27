package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Reply_OPE implements Serializable {

    ArrayList<ReplicaResponseMessage> messages;
    String publicKey;
    String amount;
    List<String> listAmounts;
    long nonce;

    OpType operationType;


    public Reply_OPE(){

    }


    // For OPE_INT - without pubKey and a List<String> amounts instead of a single amount
    public Reply_OPE(OpType operationType, ArrayList<ReplicaResponseMessage> messages, List<String> listAmounts, long nonce){
        this.messages = messages;
        this.listAmounts = listAmounts;
        this.nonce = nonce;
        this.operationType = operationType;

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


    public List<String> getListAmounts() {
        return listAmounts;
    }

    public void setListAmounts(List<String> listAmounts) {
        this.listAmounts = listAmounts;
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
