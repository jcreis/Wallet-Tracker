package container;

import com.google.gson.Gson;
import hj.mlib.HomoAdd;
import hj.mlib.PaillierKey;
import model.ReplySGX;
import model.TypeAmount;
import security.PrivateKey;
import security.PublicKey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Path("/sgx")
public class sconeApi {

    public sconeApi() throws Exception {
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



    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public ReplySGX getLowHigh(@QueryParam("higher") Long high, @QueryParam("lower") Long low, @QueryParam("nonce")Long nonce,
                            @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                            @QueryParam("db") String db, @QueryParam("sgxKey") String sgxKey) throws Exception {

        System.out.println("HI IM SGX GET_LOW_HIGH METHOD");

        Gson gson = new Gson();
        String db_D = URLDecoder.decode(db, "UTF-8");

        HashMap<String, TypeAmount> db_filtered = gson.fromJson(db_D, HashMap.class);
        List<String> returnList = new ArrayList<String>();

        File sgxPrivateKey = new File("./sgxPrivateKey.txt");
        String privateKey="";
        Scanner scanner = new Scanner(sgxPrivateKey);

        while(scanner.hasNextLine()){
            privateKey=scanner.next();
            System.out.println("privateKey: " + privateKey);
        }

        byte[] sgxByte = Base64.getDecoder().decode(privateKey);
        PrivateKey sgxPrivate = PrivateKey.createKey(sgxByte);

        String sgxKey_D = URLDecoder.decode(sgxKey, "UTF-8");
        byte[] decryptedPrivate = sgxPrivate.decrypt(sgxKey_D.getBytes());

        PaillierKey sgxFinalKey = HomoAdd.keyFromString(decryptedPrivate.toString());




        db_filtered.forEach((String key, TypeAmount value) -> {

            BigInteger valueToDecrypt = BigInteger.valueOf(Long.parseLong(value.getAmount()));
            Long valueToCheck = null;

            try {
                BigInteger decriptedBigInt = HomoAdd.decrypt(valueToDecrypt, sgxFinalKey);
                valueToCheck = decriptedBigInt.longValue();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(valueToCheck >= low && valueToCheck <= high){
                returnList.add(key);
            }
        });

        System.out.println("type; " + type);
        System.out.println("encript type: " + encryptType);
        System.out.println("list: " + returnList);
        ReplySGX response = new ReplySGX(type, encryptType, nonce+1, returnList);
        return response;
    }


}
