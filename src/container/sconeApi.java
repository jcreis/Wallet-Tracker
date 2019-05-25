package container;

import com.google.gson.Gson;
import hj.mlib.HomoAdd;
import model.Reply;
import model.ReplySGX;
import model.TypeAmount;
import org.json.JSONObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Path("/sgx")
public class sconeApi {

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON)
    public ReplySGX getLowHigh(@QueryParam("higher") Long high, @QueryParam("lower") Long low, @QueryParam("nonce")Long nonce,
                            @QueryParam("type") String type, @QueryParam("encryptType") String encryptType,
                            @QueryParam("db") String db){

        Gson gson = new Gson();

        HashMap<String, TypeAmount> db_filtered = gson.fromJson(db, HashMap.class);
        List<String> returnList = new ArrayList<String>();

        db_filtered.forEach((String key, TypeAmount value) -> {
            BigInteger valueToDecrypt = BigInteger.valueOf(Long.parseLong(value.getAmount()));
            Long valueToCheck = HomoAdd.decrypt(valueToDecrypt, pk);

            if(valueToCheck >= low && valueToCheck <= high){
                returnList.add(key);
            }
        });

        ReplySGX response = new ReplySGX(type, encryptType, nonce+1, returnList);
        return response;
    }


}
