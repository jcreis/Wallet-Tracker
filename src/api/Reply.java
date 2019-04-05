package api;

import bftsmart.tom.core.messages.TOMMessage;

import java.util.ArrayList;

public class Reply {

    ArrayList messages;
    Object user;

    public Reply(ArrayList messages, Object user){
        this.messages = messages;
        this.user = user;

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
}
