package hj.demos;

import hj.mlib.HelpSerial;
import hj.mlib.HomoSearch;

import javax.crypto.SecretKey;

public class ExampleSEARCH {

	public static void main(String[] args) {
		int voltas = 100000;
		
		String palavra = "ten";
		String encrypted = "";
		SecretKey key = HomoSearch.generateKey();
		System.out.println("Starting "+voltas+" encryptions of the phrase:");
		String texto = "one two three four five six seven eight nine ten eleven twelve thirteen fourteen fifteen sixteen";
		System.out.println(texto);
		int amostras = 31;
		
		for(int j = 0; j < amostras; j++){
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < voltas; i++){
				encrypted = HomoSearch.encrypt(key, texto);
			}
			long endTime = System.currentTimeMillis();
			System.out.print(voltas+" ; "+(endTime-startTime)+" ; ");
			//System.out.println("Starting "+voltas+" decryptions of the cryptogram:");
			//System.out.println(encrypted);			
			startTime = System.currentTimeMillis();
			for (int i = 0; i < voltas; i++){
				texto = HomoSearch.decrypt(key, encrypted);
			}
			endTime = System.currentTimeMillis();
			System.out.print((endTime-startTime)+" ; ");
			//System.out.println("Result of the decryption:");
			//System.out.println(texto);	
			String palavraEnc = HomoSearch.wordDigest64(key, palavra);	
			boolean match = false;
			//System.out.println("Starting "+voltas+" searchs of the word "+palavra);
			startTime = System.currentTimeMillis();
			for (int i = 0; i < voltas; i++){
				match = HomoSearch.pesquisa(palavraEnc, encrypted);
			}
			endTime = System.currentTimeMillis();
			System.out.println((endTime-startTime));
		}
	}

}
