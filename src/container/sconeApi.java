package container;

import com.google.gson.Gson;
import hj.mlib.HelpSerial;
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
    //@Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized ReplySGX getLowHigh(@QueryParam("higher") Long high, @QueryParam("lower") Long low, @QueryParam("nonce")Long nonce,
                            @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                            @QueryParam("db") String db, @QueryParam("sgxKey") String sgxKey) throws Exception {

        System.out.println("HI IM SGX GET_LOW_HIGH METHOD");

        try {


            Gson gson = new Gson();
            String db_D = URLDecoder.decode(db, "UTF-8");

            HashMap<String, TypeAmount> db_filtered = gson.fromJson(db_D, HashMap.class);
            List<String> returnList = new ArrayList<String>();

            File sgxPrivateKey = new File("./sgxPrivateKey.txt");
            String privateKey = "";
            Scanner scanner = new Scanner(sgxPrivateKey);
            System.out.println("11");
            while (scanner.hasNextLine()) {
                privateKey = scanner.next();
                System.out.println("12");
                //System.out.println("privateKey: " + privateKey);
            }
            System.out.println("13");

            byte[] sgxByte = Base64.getDecoder().decode(privateKey);
            System.out.println("14");
            PrivateKey sgxPrivate = PrivateKey.createKey(sgxByte);
            System.out.println("15");

            System.out.println("16");


            //String sgxKey_D = URLDecoder.decode(sgxKey, "UTF-8");
            byte[] decodedBytes = Base64.getDecoder().decode(sgxKey);

            System.out.println("1");
            byte[] decryptedPrivate = sgxPrivate.decrypt(decodedBytes);
            System.out.println("22");

            String decripted = new String(decryptedPrivate);
            //String utfString = URLDecoder.decode(decripted, "UTF-8");

            PaillierKey sgxFinalKey = (PaillierKey)HelpSerial.fromString(decripted);


            System.out.println("23");

            System.out.println("2");
            db_filtered.forEach((String key, TypeAmount value) -> {

                BigInteger valueToDecrypt = BigInteger.valueOf(Long.parseLong(value.getAmount()));
                Long valueToCheck = null;
                System.out.println("3");

                try {
                    System.out.println("4");
                    BigInteger decriptedBigInt = HomoAdd.decrypt(valueToDecrypt, sgxFinalKey);
                    valueToCheck = decriptedBigInt.longValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                System.out.println("5");
                if (valueToCheck >= low && valueToCheck <= high) {
                    returnList.add(key);
                    System.out.println("6");
                }
                System.out.println("7");
            });

            System.out.println("type; " + type);
            System.out.println("encript type: " + encryptType);
            System.out.println("list: " + returnList);
            ReplySGX response = new ReplySGX(type, encryptType, nonce + 1, returnList);
            return response;
        }catch (Exception e ) {
            e.printStackTrace();

            return null;
        }
    }


}
