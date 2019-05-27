package model;

import java.util.HashMap;
import java.util.List;

public class ReplyCondUpdate {

    private HashMap<String,String> returnMap;
    private long nonce;

    public ReplyCondUpdate() {
    }

    public ReplyCondUpdate(HashMap<String, String> returnMap, long nonce) {
        this.returnMap = returnMap;
        this.nonce = nonce;
    }

    public HashMap<String, String> getReturnMap() {
        return returnMap;
    }

    public void setReturnMap(HashMap<String, String> returnMap) {
        this.returnMap = returnMap;
    }

    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }
}
