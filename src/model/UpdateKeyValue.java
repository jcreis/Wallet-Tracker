package model;

public class UpdateKeyValue {

    private Integer op;
    private String key;
    private String value;

    public UpdateKeyValue(Integer op, String key, String value) {
        this.op = op;
        this.key = key;
        this.value = value;
    }

    public UpdateKeyValue() {

    }

    public Integer getOp() {
        return op;
    }

    public void setOp(Integer op) {
        this.op = op;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
