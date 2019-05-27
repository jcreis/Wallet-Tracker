package model;

import java.util.ArrayList;
import java.util.HashMap;

public class ReplyCondUpd_Client {

    private HashMap<String,String> key_value;
    private Long nonce;
    private ArrayList<ReplicaResponseMessage> messages;

    public ReplyCondUpd_Client(HashMap<String, String> key_value, Long nonce, ArrayList<ReplicaResponseMessage> messages) {
        this.key_value = key_value;
        this.nonce = nonce;
        this.messages = messages;
    }

    public ReplyCondUpd_Client() {
    }

    public HashMap<String, String> getKey_value() {
        return key_value;
    }

    public void setKey_value(HashMap<String, String> key_value) {
        this.key_value = key_value;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public ArrayList<ReplicaResponseMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<ReplicaResponseMessage> messages) {
        this.messages = messages;
    }
}
