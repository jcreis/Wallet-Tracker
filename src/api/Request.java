package api;

public class Request {

    long nonce;

    public Request(long nonce){
        this.nonce = nonce;

    }


    public long getNonce() {
        return nonce;
    }

    public void setNonce(long nonce) {
        this.nonce = nonce;
    }




}
