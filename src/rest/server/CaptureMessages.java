package rest.server;

import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.util.Extractor;

public class CaptureMessages implements Extractor {

    public TOMMessage messages;

    public CaptureMessages(){

    }


    @Override
    public TOMMessage extractResponse(TOMMessage[] tomMessages, int sameContent, int lastReceived) {


        System.out.println("prints");
        return null;
    }
}
