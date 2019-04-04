package api;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import bftsmart.tom.ServiceProxy;
import rest.server.ReplicaServer;


@Path("/users")
public class WalletResources {

	int replicaNumber;

	ServiceProxy serviceProxy = new ServiceProxy(0);

	ReplicaServer replicaServer;

	public WalletResources(int replicaNumber) {
		this.replicaNumber = replicaNumber;
		System.out.println("replica number " + replicaNumber);
		replicaServer = new ReplicaServer(replicaNumber);
	}

	public enum opType{
		TRANSFER,
		ADD_MONEY,
		GET_MONEY,
		GET_USERS,
		ADD_USER
	}

	private Map<String, User> db = new ConcurrentHashMap<String, User>();

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public User[] getUsers() {

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.GET_USERS);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			if (reply.length == 0) {
				System.out.println("1");
				return null;
			}
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				System.out.println("List of users: "+ db.values().toArray(new User[db.size()]));

				return (User[]) objIn.readObject();
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception getting value from map: " + e.getMessage());
		}

		System.out.println( db.size());
		return db.values().toArray( new User[ db.size() ]);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public User register(User user) {
		System.err.printf("register: %s <%s>\n", user.getId(), user);

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.ADD_USER);
			objOut.writeObject(user);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				return null;
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				return (User)objIn.readObject();
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception putting value into map: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	@PUT
	@Path("/{id}")
	public Double addMoney(@PathParam("id") String id, @QueryParam("value") Double value){
		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.ADD_MONEY);
			objOut.writeObject(id);
			objOut.writeObject(value);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				return null;
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				return (Double)objIn.readObject();
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception putting value into map: " + e.getMessage());
		}
		return null;
	}


	@PUT
	@Path("/transfer/{fid}")
	public Double transferMoney(@PathParam("fid") String fid, @QueryParam("tid") String tid, @QueryParam("value") Double value){

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.TRANSFER);
			objOut.writeObject(fid);
			objOut.writeObject(tid);
			objOut.writeObject(value);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeOrdered(byteOut.toByteArray());
			if (reply.length == 0)
				return null;
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				return (Double)objIn.readObject();
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception putting value into map: " + e.getMessage());
		}
		return null;

	}

	@GET
	@Path("/{id}/money")
	@Produces(MediaType.APPLICATION_JSON)
	public Double getMoney(@PathParam("id") String id){

		try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			 ObjectOutput objOut = new ObjectOutputStream(byteOut);) {

			objOut.writeObject(opType.GET_MONEY);
			objOut.writeObject(id);

			objOut.flush();
			byteOut.flush();

			byte[] reply = serviceProxy.invokeUnordered(byteOut.toByteArray());
			if (reply.length == 0)
				return null;
			try (ByteArrayInputStream byteIn = new ByteArrayInputStream(reply);
				 ObjectInput objIn = new ObjectInputStream(byteIn)) {
				return (Double) objIn.readObject();
			}

		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Exception getting value from map: " + e.getMessage());
		}
		return null;

	}

}
