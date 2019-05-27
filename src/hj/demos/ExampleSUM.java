package hj.demos;
import java.math.BigInteger;
import java.util.Random;

import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import hj.mlib.PaillierKey;

public class ExampleSUM {

	public static void main(String[] args) {
		int voltas = 100;
		try {
			PaillierKey pk = HomoAdd.generateKey();
			//pk.printValues();
			//Generate two big integer probably primes with 128 bits
			System.out.println("Generate two big integer probably primes with 128 bits");
			BigInteger big1 = new BigInteger(128, 0, new Random());
			BigInteger big2 = new BigInteger(128, 0, new Random());	
			System.out.println("big1:     "+big1);
			System.out.println("big2:     "+big2);
			BigInteger big1Code = HomoAdd.encrypt(big1, pk);	
			
			//System.out.println("Starting "+voltas+" Paillier Encryptions of the Big Integer: "+big2);		int amostras = 31;
			int amostras = 31;
			System.out.println("Iterations ; Encryption ; Decryption ; Sum ; Subtraction ; Mult. by parameter");
			for(int j = 0; j < amostras; j++){

				long startTime = System.currentTimeMillis();
				BigInteger big2Code = new BigInteger("1");
				for(int i = 0; i < voltas; i++){
					big2Code = HomoAdd.encrypt(big2, pk);
				}
				long endTime = System.currentTimeMillis();
				System.out.print(voltas+" ; "+(endTime-startTime)+" ; ");
				//System.out.println("Starting "+voltas+" Paillier Decryptions of one Big Integer");			
				startTime = System.currentTimeMillis();
				for(int i = 0; i < voltas; i++){
					big2 = HomoAdd.decrypt(big2Code, pk);
				}
				endTime = System.currentTimeMillis();
				System.out.print((endTime-startTime)+" ; ");
				//System.out.println("Resulting: in "+big2);
				BigInteger big3Code = new BigInteger("1");;

				startTime = System.currentTimeMillis();
				for(int i = 0; i < voltas; i++){
					big3Code = HomoAdd.sum(big1Code, big2Code, pk.getNsquare());
				}
				endTime = System.currentTimeMillis();	
				System.out.print((endTime-startTime)+" ; ");
				//System.out.println("Resulting in: "+HomoAdd.decrypt(big3Code, pk));

				startTime = System.currentTimeMillis();
				for(int i = 0; i < voltas; i++){
					big3Code = HomoAdd.dif(big1Code, big2Code, pk.getNsquare());
				}
				endTime = System.currentTimeMillis();	
				System.out.print((endTime-startTime)+" ; ");
				//System.out.println("Resulting in: "+HomoAdd.decrypt(big3Code, pk));			
				int parameter = 10;
				startTime = System.currentTimeMillis();
				for(int i = 0; i < voltas; i++){
					big3Code = HomoAdd.mult(big1Code, parameter, pk.getNsquare());
				}
				endTime = System.currentTimeMillis();	
				System.out.println((endTime-startTime));
								
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}

}
