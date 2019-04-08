package rest.server;

//import api.User;
import api.ReplicaResponseMessage;
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
        for(int i = 0; i < tomMessages.length ; i++){
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tomMessages[i].getContent());
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                /*byte[] x = tomMessages[i].serializedMessageSignature;
                System.out.println(x);
                System.out.println(tomMessages[i].signed);*/
                ReplicaResponseMessage replicaMsg = new ReplicaResponseMessage();
                replicaMsg.setSender(tomMessages[i].getSender());
                replicaMsg.setContent(tomMessages[i].getContent());
                messages.add(replicaMsg);

            } catch (IOException e) {
                e.printStackTrace();

            }

        }
        //System.out.println("prints");
        return tomMessages[lastReceived];
    }


    public ArrayList<ReplicaResponseMessage> getReplicaMessages(){
        return messages;
    }
}
