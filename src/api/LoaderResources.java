package api;



import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Path("/wallet")
public class LoaderResources {

    private Map<String, Process> processes = new ConcurrentHashMap<>();

    public LoaderResources(){

    }


    @POST
    @Path("/launch")
    public void launch( @QueryParam("port") String port,
                        @QueryParam("id")String id) throws IOException {


        ProcessBuilder pb = new ProcessBuilder("/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java", "-cp", "target/lib/jersey-media-json-jackson-2.25.1.jar:target/rest-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "rest.server.WalletServer", port, id);

        Process p = pb.start();
        processes.put(id, p);


    }

    @POST
    @Path("/stop")
    public void stop(@QueryParam("id")String id) {

        if(processes.get(id).isAlive()) {
            processes.get(id).destroy();
        }else{
            System.out.println("Process is not alive");
        }


    }


}







