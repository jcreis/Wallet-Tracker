package model;

public class ReplicaResponseMessage {
    int sender;
    byte[] content;
    byte[] signature;
    byte[] serializedMessage;




    public ReplicaResponseMessage(int sender, byte[] content, byte[] signature, byte[] serializedMessage) {
        this.sender = sender;
        this.content = content;
        this.signature = signature;
        this.serializedMessage = serializedMessage;
    }

    public ReplicaResponseMessage() {
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getSerializedMessage() {
        return serializedMessage;
    }

    public void setSerializedMessage(byte[] serializedMessage) {
        this.serializedMessage = serializedMessage;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }
}
