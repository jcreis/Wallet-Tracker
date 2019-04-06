package api;

import bftsmart.reconfiguration.util.RSAKeyLoader;
import bftsmart.tom.ServiceProxy;
import rest.server.CaptureMessages;
import rest.server.ReplicaServer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Path("/users")
public class WalletResources {

	int replicaNumber;

	Long nonce;

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
		serviceProxy  = new ServiceProxy(replicaNumber, "config",null, captureMessages, keyLoader);

		//TODO: to test nonces comment the next 2 lines and add @Query nonce from methods bellow
		nonce = random.nextLong();
		System.out.println("nonce = " + nonce);
	}

	public enum opType{
		TRANSFER,
		ADD_MONEY,
		GET_MONEY,
		GET_USERS,
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

	@PUT
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Reply addMoney(@QueryParam("publicKey") String publicKey, @QueryParam("value") Double value,
						  @QueryParam("nonce") Long nonce){

		Long replyNonce;

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.ADD_MONEY);
			objOut.writeObject(publicKey);
			objOut.writeObject(value);
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
				return new Reply(captureMessages.sendMessages(), publicKey, money, replyNonce+1) ;
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception putting value into map: " + e.getMessage());
		}
		return null;
	}


	@PUT
	@Path("/transfer/{fpublicKey}")
	@Produces(MediaType.APPLICATION_JSON)
	public Reply transferMoney(@PathParam("fpublicKey") String fpublicKey, @QueryParam("tpublicKey") String tpublicKey, @QueryParam("value")
			Double value, @QueryParam("nonce") Long nonce){

		Long replyNonce;

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
				double money = (Double)objIn.readObject();
				replyNonce = (Long)objIn.readObject();
				return new Reply(captureMessages.sendMessages(), fpublicKey, money, replyNonce+1) ;
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception putting value into map: " + e.getMessage());
		}
		return null;

	}

	@GET
	@Path("/{publicKey}/money")
	@Produces(MediaType.APPLICATION_JSON)
	public Reply getMoney(@PathParam("publicKey") String publicKey){

		Long replyNonce;

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
				double money = (Double)objIn.readObject();
				replyNonce = (Long)objIn.readObject();
				return new Reply(captureMessages.sendMessages(), publicKey,  money, replyNonce+1) ;
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception getting value from map: " + e.getMessage());
		}
		return null;

	}

	public static byte[] getDigest( byte[] data ) throws NoSuchAlgorithmException {
		MessageDigest d = MessageDigest.getInstance("MD5");
		d.update( data ) ;
		return d.digest();
	}

}
