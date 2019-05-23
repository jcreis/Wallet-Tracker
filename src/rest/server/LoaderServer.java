package rest.server;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class LoaderServer {


    public static void main(String[] args) throws Exception {
        URL[] classLoaderUrls;

        {
            try {
                classLoaderUrls = new URL[]{new URL("")};

                // Create a new URLClassLoader
                URLClassLoader urlClassLoader = new URLClassLoader(classLoaderUrls);

                // Load the target class
                Class<?> beanClass = urlClassLoader.loadClass("rest.server.WalletServer");

                // Create a new instance from the loaded class
                Constructor<?> constructor = beanClass.getConstructor();
                Object beanObj = constructor.newInstance();

                // Getting a method from the loaded class and invoke it
                Method method = beanClass.getMethod("launch");
                method.invoke(beanObj);


            } catch (MalformedURLException | InstantiationException | InvocationTargetException | NoSuchMethodException |
                    IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }



}
