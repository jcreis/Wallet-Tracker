package model;

public class TypeAmount {


    public TypeAmount(String type, String amount){

        this.type = type;

        this.amount = amount;

    }

    public TypeAmount(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    private String amount;

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }


}
