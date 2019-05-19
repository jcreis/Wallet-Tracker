
package hj.demos;

import java.io.UnsupportedEncodingException;
import javax.crypto.SecretKey;

import hlib.hj.mlib.HomoDet;
import hlib.hj.mlib.HomoRand;

public class ExampleRAND {

	public static void main(String[] args) {
		//String key = "Bar12345Bar12345Bar12345"; // 128 bit key
		SecretKey key = HomoRand.generateKey();
		byte[] iV = HomoRand.generateIV();
		String auxMessage = "0123456789012345678901234567890123456789012345678901234567890123";
		byte[] message = null;
		try {
			message = auxMessage.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println("Starting Random Encryption with message: \n"+auxMessage);
		int voltas = 100000;
		int amostras = 31;
		byte[] lixo = null;
		
		for(int j = 0; j < amostras; j++){
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < voltas; i++){
				lixo = HomoRand.encrypt(key,  iV, message);
			}
			long endTime = System.currentTimeMillis();
			System.out.print(voltas+" ; "+(endTime-startTime)+" ; ");
			startTime = System.currentTimeMillis();
			for (int i = 0; i < voltas; i++){
				message = HomoRand.decrypt(key,  iV, lixo);
			}
			endTime = System.currentTimeMillis();
			System.out.println(endTime-startTime);		
		}


	}

}
