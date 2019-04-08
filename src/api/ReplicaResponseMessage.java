package api;

public class ReplicaResponseMessage {
    int sender;
    byte[] content;

    public ReplicaResponseMessage(int sender, byte[] content) {
        this.sender = sender;
        this.content = content;
    }

    public ReplicaResponseMessage() {
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
