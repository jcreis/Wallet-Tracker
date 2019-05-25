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
import java.math.BigInteger;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

@Path("/sgx")
public class sconeApi {

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
        }

        byte[] sgxByte = Base64.getDecoder().decode(privateKey);
        PrivateKey sgxPrivate = PrivateKey.createKey(sgxByte);

        byte[] decryptedPrivate = sgxPrivate.decrypt(sgxKey.getBytes());

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

        ReplySGX response = new ReplySGX(type, encryptType, nonce+1, returnList);
        return response;
    }


}
