package model;

import java.util.List;

public class ReplySGX {


    String type, encryptType;
    Long nonce;
    List<String> returnList;

    public ReplySGX(String type, String encryptType, Long nonce, List<String> returnList) {

        this.type = type;
        this.encryptType = encryptType;
        this.nonce = nonce;
        this.returnList = returnList;

    }
}
