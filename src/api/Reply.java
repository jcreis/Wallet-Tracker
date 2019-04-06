package api;

import java.util.ArrayList;

public class Reply {

    ArrayList messages;
    Object user;
    long nonce;



    public Reply(ArrayList messages, Object user, long nonce){
        this.messages = messages;
        this.user = user;
        this.nonce = nonce;

    }

    public Reply() {
    }

    public ArrayList getMessages() {
        return messages;
    }

    public void setMessages(ArrayList messages) {
        this.messages = messages;
    }

    public Object getUser() {
        return user;
    }

    public void setUser(Object user) {
        this.user = user;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
