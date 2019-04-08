package model;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class CaptureMessages implements Extractor {

    public ArrayList<ReplicaResponseMessage> messages;

    public CaptureMessages(){

    }


    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {

        messages = new ArrayList<ReplicaResponseMessage>();
        for (int i = 0; i < tomMessages.length; i++) {
            if (tomMessages[i] != null) {

                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tomMessages[i].getContent());
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {


                    ReplicaResponseMessage replicaMsg = new ReplicaResponseMessage();
                    replicaMsg.setSender(tomMessages[i].getSender());
                    replicaMsg.setContent(tomMessages[i].getContent());
                    replicaMsg.setSignature(tomMessages[i].serializedMessageSignature);
                    replicaMsg.setSerializedMessage(tomMessages[i].serializedMessage);
                    messages.add(replicaMsg);

                } catch (IOException e) {


                }

            }

        }
        return tomMessages[lastReceived];
    }


    public ArrayList<ReplicaResponseMessage> getReplicaMessages(){
        return messages;
    }
}
