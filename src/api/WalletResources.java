package api;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import rest.server.CaptureMessages;
import rest.server.ReplicaServer;
import security.Digest;
import security.PublicKey;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static security.Digest.getDigest;


@Path("/users")
public class WalletResources {

    int replicaNumber;

    ServiceProxy serviceProxy;

    ReplicaServer replicaServer;

    RSAKeyLoader keyLoader;

    CaptureMessages captureMessages = new CaptureMessages();

    SecureRandom random = new SecureRandom();

    public WalletResources(int replicaNumber) {
        this.replicaNumber = replicaNumber;
        System.out.println("replica number " + replicaNumber);
        replicaServer = new ReplicaServer(replicaNumber);
        keyLoader = new RSAKeyLoader(replicaNumber, "config", false, "sha512WithRSAEncryption");
        serviceProxy = new ServiceProxy(replicaNumber, "config", null, captureMessages, keyLoader);

        //TODO: to test nonces comment the next 2 lines and add @Query nonce from methods bellow

    }

    public enum opType {
        TRANSFER,
        ADD_MONEY,
        GET_MONEY,
        ADD_USER
    }

    private Map<String, Double> db = new ConcurrentHashMap<String, Double>();
	/*@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Reply getUsers(@QueryParam("nonce") Long nonce, @QueryParam("publicKey") String publicKey) { //
		User[] userReply ;
		Long replyNonce = 0L;

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.GET_USERS);
			objOut.writeObject(nonce);
			objOut.writeObject(publicKey);


			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			if (reply.length == 0) {
				//System.out.println("1");
				return null;
			}
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				System.out.println("List of users: "+ db.values().toArray(new User[db.size()]));
				userReply = (User[])objIn.readObject();
				replyNonce = (Long)objIn.readObject();
				System.out.println(captureMessages.sendMessages());
				System.out.println("nonce :" + replyNonce);
				return new Reply(captureMessages.sendMessages(), userReply, replyNonce+1);
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception getting value from map: " + e.getMessage());
		}

		System.out.println( db.size());
		return new Reply(captureMessages.sendMessages(), db.values().toArray( new User[ db.size() ]), replyNonce+1);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Reply register(@QueryParam("publicKey") String publicKey,  @QueryParam("amount") Double amount, @QueryParam("nonce") Long nonce) {

		Long replyNonce;

		//System.err.printf("register: %s <%s>\n", user.getId(), user);

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.ADD_USER);
			objOut.writeObject(publicKey);
			objOut.writeObject(amount);
			objOut.writeObject(nonce);



			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				return null;
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				double money = (Double)objIn.readObject();
				replyNonce = (Long)objIn.readObject();
				return new Reply(captureMessages.sendMessages(), publicKey, money,replyNonce+1);

			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception putting value into map: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}*/

    @SuppressWarnings("Duplicates")
    @POST
    @Path("/{publicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Reply addMoney(@PathParam("publicKey") String publicKey,
                          @QueryParam("value") Double value,
                          @QueryParam("nonce") Long nonce,
                          @QueryParam("msg") String msg) throws Exception {

        Long replyNonce;

            URLDecoder.decode(publicKey, "UTF-8");
            String verify = publicKey + value + nonce;
            byte[] hash = Digest.getDigest(verify.getBytes());

            byte[] pubKeyArr = Base64.getDecoder().decode(publicKey);
            PublicKey pub2 = PublicKey.createKey(pubKeyArr);

            byte[] decodedBytes = Base64.getDecoder().decode(msg);
            byte[] hashDecriptPriv = pub2.decrypt(decodedBytes);

            System.out.println("1");
            if (Arrays.equals(hashDecriptPriv,hash)) {

                System.out.println("12");
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                    objOut.writeObject(opType.ADD_MONEY);
                    objOut.writeObject(publicKey);
                    objOut.writeObject(value);
                    objOut.writeObject(nonce);
                    System.out.println("2");

                    objOut.flush();
                    byteOut.flush();
                    byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
                    if (reply.length == 0)
                        return null;
                    try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                         ObjectInput objIn = new ObjectInputStream(byteIn)) {
                        double money = (Double) objIn.readObject();
                        replyNonce = (Long) objIn.readObject();
                        System.out.println("RESPONSE FROM ADD MONEY IS:");
                        Reply r = new Reply(captureMessages.sendMessages(), publicKey, money, replyNonce + 1);
                        System.out.println(r);
                        return r;
                    }

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("Exception putting value into map: " + e.getMessage());
                }
            }
            return null;

    }


    @SuppressWarnings("Duplicates")
    @PUT
    @Path("/transfer/{fpublicKey}")
    @Produces(MediaType.APPLICATION_JSON)
    public Reply transferMoney(@PathParam("fpublicKey") String fpublicKey, @QueryParam("tpublicKey") String tpublicKey, @QueryParam("value")
            Double value, @QueryParam("nonce") Long nonce, @QueryParam("msg") String msg) throws NoSuchAlgorithmException {

        Long replyNonce;

        String verify = fpublicKey + tpublicKey + value + nonce;

        if (getDigest(verify.getBytes()) == getDigest(msg.getBytes())) {


            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                objOut.writeObject(opType.TRANSFER);
                objOut.writeObject(fpublicKey);
                objOut.writeObject(tpublicKey);
                objOut.writeObject(value);
                objOut.writeObject(nonce);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    double money = (Double) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    return new Reply(captureMessages.sendMessages(), fpublicKey, money, replyNonce + 1);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception putting value into map: " + e.getMessage());
            }
        }
        throw new ForbiddenException();

    }

    @SuppressWarnings("Duplicates")
    @GET
    @Path("/{publicKey}/money")
    @Produces(MediaType.APPLICATION_JSON)
    public Reply getMoney(@PathParam("publicKey") String publicKey, @QueryParam("nonce") Long nonce,
                          @QueryParam("msg") String msg) throws NoSuchAlgorithmException {

        Long replyNonce;

        String verify = publicKey + nonce;

        if (getDigest(verify.getBytes()) == getDigest(msg.getBytes())) {

            try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

                objOut.writeObject(opType.GET_MONEY);
                objOut.writeObject(publicKey);
                objOut.writeObject(nonce);


                objOut.flush();
                byteOut.flush();

                byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
                if (reply.length == 0)
                    return null;
                try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
                     ObjectInput objIn = new ObjectInputStream(byteIn)) {
                    double money = (Double) objIn.readObject();
                    replyNonce = (Long) objIn.readObject();
                    return new Reply(captureMessages.sendMessages(), publicKey, money, replyNonce + 1);
                }

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception getting value from map: " + e.getMessage());
            }
        }
        throw new ForbiddenException();

    }



}
