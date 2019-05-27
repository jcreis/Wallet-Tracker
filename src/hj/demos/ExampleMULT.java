package hj.demos;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Random;

import hj.mlib.HomoAdd;
import hj.mlib.HomoMult;

public class ExampleMULT {

	public static void main(String[] args) {
		int voltas = 10000;
		//Generate two big integer probably primes with 128 bits

		System.out.println("Generate two big integer probably primes with 128 bits");
		BigInteger big1 = new BigInteger(128, 0, new Random());
		BigInteger big2 = new BigInteger(128, 0, new Random());	
		
		System.out.println("big1:     "+big1);
		System.out.println("big2:     "+big2);
		KeyPair keyPair = HomoMult.generateKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();		
		BigInteger big1Code = HomoMult.encrypt(publicKey, big1);
		
		
		System.out.println("Starting "+voltas+" RSA Encryptions of the Big Integer: "+big2);
		int amostras = 31;
		System.out.println("Iterations ; Encryption ; Decryption ; Multiplication");
		for(int j = 0; j < amostras; j++){		

			long startTime = System.currentTimeMillis();
			BigInteger big2Code = new BigInteger("1");
			for(int i = 0; i < voltas; i++){
				big2Code = HomoMult.encrypt(publicKey, big2);
			}
			long endTime = System.currentTimeMillis();
			System.out.print(voltas+" ; "+(endTime-startTime)+" ; ");
			//System.out.println("Starting "+voltas+" RSA Decryptions of one Big Integer");			
			startTime = System.currentTimeMillis();
			for(int i = 0; i < voltas; i++){
				big2 = HomoMult.decrypt(privateKey, big2Code);
			}
			endTime = System.currentTimeMillis();
			System.out.print((endTime-startTime)+" ; ");
			//System.out.println("Resulting: in "+big2);
			BigInteger produto = new BigInteger("1");
			startTime = System.currentTimeMillis();
			for(int i = 0; i < voltas; i++){
				produto = HomoMult.multiply(big1Code, big2Code, publicKey);
			}
			endTime = System.currentTimeMillis();
			System.out.println(endTime-startTime);
			//System.out.println("Resulting: in "+HomoMult.decrypt(privateKey,produto));
		}
		
	}

}
