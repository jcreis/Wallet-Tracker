package model;

public class TypeKey {


    private String type;
    private String key;

    public TypeKey(String type, String key) {
        this.type = type;
        this.key = key;
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

}
