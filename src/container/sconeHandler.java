package container;

import security.PrivateKey;
import security.PublicKey;
import java.io.File;
import java.io.FileWriter;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class sconeHandler {




    public sconeHandler() throws Exception {


        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pub = new PublicKey("RSA", kp.getPublic());
        PrivateKey priv = new PrivateKey("RSA", kp.getPrivate());


        String publicString = Base64.getEncoder().encodeToString(pub.exportKey());
        String privateString = Base64.getEncoder().encodeToString(priv.exportKey());


        File publicKey = new File("./sgxPublicKey.txt");
        File privateKey = new File("./sgxPrivateKey.txt");


        FileWriter pb = new FileWriter(publicKey, false);
        FileWriter pr = new FileWriter(privateKey, false);
        pb.write(publicString);
        pr.write(privateString);
        pb.close();
        pr.close();
    }

}
