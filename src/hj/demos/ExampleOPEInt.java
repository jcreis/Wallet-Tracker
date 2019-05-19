package hj.demos;

import hlib.hj.mlib.HomoOpeInt;

public class ExampleOPEInt {

	public static void main(String[] args) {
		int voltas = 100000;

		long[] resultado = new long[voltas];
		int contrario;
		System.out.println("Starting Order Preserving Encryption of numbers from 0 to "+voltas);
		int amostras = 31;
		
		for(int j = 0; j < amostras; j++){
			long key = HomoOpeInt.generateKey();
			HomoOpeInt ope = new HomoOpeInt(key);
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < voltas; i++){
				resultado[i] =ope.encrypt(i);

			}
			long endTime = System.currentTimeMillis();
			System.out.print(key+" ; "+voltas+" ; "+(endTime-startTime)+" ; ");
			//System.out.println(voltas+" encryption operations in "+(endTime-startTime)+" miliseconds");
			//System.out.println("Starting Order Preserving Decryption of numbers from "+resultado[0]+" to "+resultado[voltas-1]);		
			startTime = System.currentTimeMillis();
			for(int i = 0; i < voltas; i++){
				contrario = ope.decrypt(resultado[i]);
			}		
			endTime = System.currentTimeMillis();
			System.out.print((endTime-startTime)+" ; ");
			boolean lixoBool;
			startTime = System.currentTimeMillis();
			for(int i = 0; i < voltas; i++){
				lixoBool = ope.compare(resultado[0], resultado[1]);
			}		
			endTime = System.currentTimeMillis();
			System.out.println(endTime-startTime);			
			
			//System.out.println(voltas+" decryption operations in "+(endTime-startTime)+" miliseconds");
		}
	}
}
