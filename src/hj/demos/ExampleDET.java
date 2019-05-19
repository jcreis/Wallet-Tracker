package hj.demos;
import hlib.hj.mlib.HomoDet;

import java.io.UnsupportedEncodingException;
import javax.crypto.SecretKey;


public class ExampleDET {

	public static void main(String[] args) {
		//String key = "Bar12345Bar12345Bar12345"; // 128 bit key
                //Anthing if you want to have this static initialization
		SecretKey key = HomoDet.generateKey(); 
                // In this case for dynamic generation
		String auxMessage = "0123456789012345678901234567890123456789012345678901234567890123";

		byte[] message = null;

		try {
			message = auxMessage.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println("Starting Deterministic Encryption with message: \n"+auxMessage);
		int rondas = 100000;
		byte[] lixo = null;
		int amostras = 31;
		
		for(int j = 0; j < amostras; j++){

			long startTime = System.currentTimeMillis();
			for (int i = 0; i < rondas; i++){
				lixo = HomoDet.encrypt(key,  message);
			}
			long endTime = System.currentTimeMillis(); 
			System.out.print(rondas+" ; "+(endTime-startTime)+" ; ");
			startTime = System.currentTimeMillis();
			for (int i = 0; i < rondas; i++){
				message = HomoDet.decrypt(key,  lixo);
			}
			endTime = System.currentTimeMillis();
			System.out.print((endTime-startTime)+" ; ");
			byte[] lixo2 = HomoDet.encrypt(key,  message);
			startTime = System.currentTimeMillis();
			boolean lixoBool;
			for (int i = 0; i < rondas; i++){
				try {
					lixoBool = HomoDet.compare(lixo, lixo2);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			endTime = System.currentTimeMillis();
			System.out.println(endTime-startTime);			
			
			
		}

	}

}
