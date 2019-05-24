package rest.server;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class LoaderServer {


    public static void main(String[] args) throws Exception {

        System.setProperty("javax.net.ssl.keyStore","server.jks");
        System.setProperty("javax.net.ssl.keyStorePassword","qwerty");

        ProcessBuilder pb = new ProcessBuilder("/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java", "-cp", "target/rest-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "rest.server.WalletServer");
        ProcessBuilder pb2 = new ProcessBuilder("/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java", "-cp", "target/rest-0.0.1-SNAPSHOT-jar-with-dependencies.jar", "rest.server.WalletServer", "8090", "1");
        Process p = pb.start();
        Process p2 = pb2.start();
        InputStream is = p.getErrorStream();
        for (; ; ) {
            int ch = is.read();
            if( ch == -1)
                return;
            System.out.write(ch);
            System.out.flush();
        }
        //launchSeparateProcess();
        //launchMain("10000", "0");


    }


    /*private static void launchSeparateProcess() throws IOException, InterruptedException {


        ProcessBuilder pb = new ProcessBuilder("/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java", "-cp", "target/CSD_Proj1.jar", "rest.server.WalletServer");
        ProcessBuilder pb2 = new ProcessBuilder("/Library/Java/JavaVirtualMachines/jdk1.8.0_181.jdk/Contents/Home/bin/java", "-cp", "target/CSD_Proj1.jar", "rest.server.WalletServer", "8090", "1");
        //Process p = pb.start();
        //ProcessBuilder pb = new ProcessBuilder("java", "-jar", f.getAbsolutePath());
        Process p = pb.start();
        Process p2 = pb2.start();
        InputStream is = p.getErrorStream();
        for (; ; ) {
            int ch = is.read();
            if( ch == -1)
                return;
            System.out.write(ch);
            System.out.flush();
        }

       *//* System.out.println(p.isAlive());
        System.out.println(p2.isAlive());*//*


    }*/

    private static void launchMain(String port, String n) throws IOException {
        URL[] classLoaderUrls;
        try {
            classLoaderUrls = new URL[]{new URL("file:CSD_Proj1.jar")};

            // Create a new URLClassLoader
            URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

            // Load the target class
            Class<?> beanClass = urlClassLoader.loadClass("rest.server.WalletServer");

            // Create a new instance from the loaded class
            Constructor<?> constructor = beanClass.getConstructor();
            Object beanObj = constructor.newInstance();

            final Method method = beanClass.getMethod("main", String[].class);
            final Object[] arg = new Object[1];
            arg[0] = new String[] { port, n};

            method.invoke(beanObj, arg);



        } catch (MalformedURLException | InstantiationException | InvocationTargetException | NoSuchMethodException |
                IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }








}
