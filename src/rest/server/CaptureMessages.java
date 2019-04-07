package rest.server;

//import api.User;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class CaptureMessages implements Extractor {

    public ArrayList<TOMMessage> messages;

    public CaptureMessages(){

    }


    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {

        messages = new ArrayList<TOMMessage>();
        for(int i = 0; i < tomMessages.length ; i++){
            try (ByteArrayInputStream byteIn = new ByteArrayInputStream(tomMessages[i].getContent());
                 ObjectInput objIn = new ObjectInputStream(byteIn)) {
                /*byte[] x = tomMessages[i].serializedMessageSignature;
                System.out.println(x);
                System.out.println(tomMessages[i].signed);*/

                messages.add(tomMessages[i]);

            } catch (IOException e) {
                e.printStackTrace();

            }

        }
        //System.out.println("prints");
        return tomMessages[lastReceived];
    }


    public ArrayList<TOMMessage> getReplicaMessages(){
        return messages;
    }
}
