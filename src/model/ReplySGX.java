package model;

import java.util.List;

public class ReplySGX {


    String type, encryptType;
    Long nonce;
    List<String> returnList;
    Long replyValue;

    public ReplySGX(String type, String encryptType, Long nonce, List<String> returnList, Long replyValue) {

        this.type = type;
        this.encryptType = encryptType;
        this.nonce = nonce;
        this.returnList = returnList;
        this.replyValue = replyValue;

    }

    public ReplySGX(){

    }

    public String getType() {
        return type;
    }

    public Long getReplyValue() {
        return replyValue;
    }

    public void setReplyValue(Long replyValue) {
        this.replyValue = replyValue;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(String encryptType) {
        this.encryptType = encryptType;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public List<String> getReturnList() {
        return returnList;
    }

    public void setReturnList(List<String> returnList) {
        this.returnList = returnList;
    }
}
