package model;

public class TypeKey {


    private String type;
    private String key;
    private String aes;

    public TypeKey(String type, String key, String aes) {
        this.type = type;
        this.key = key;
        this.aes = aes;
    }

    public TypeKey() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String amount) {
        this.key = amount;
    }

    public String getAes() {
        return aes;
    }

    public void setAes(String aes) {
        this.aes = aes;
    }
}
