package api;

import java.util.ArrayList;

public class Reply {

    ArrayList messages;
    String publicKey;
    double amount;
    long nonce;




    public Reply(ArrayList messages, String publicKey, double amount, long nonce){
        this.messages = messages;
        this.publicKey = publicKey;
        this.amount = amount;
        this.nonce = nonce;

    }

    public Reply() {
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public ArrayList getMessages() {
        return messages;
    }

    public void setMessages(ArrayList messages) {
        this.messages = messages;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
