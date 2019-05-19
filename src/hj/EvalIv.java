package hj;

import javax.crypto.SecretKey;
import hj.mlib.HomoRand;

/**
 * Testa tempo de geracao de chaves e vetores de inicializacao
 */
public class EvalIv
{

	public static void main(String[] args) {
		long initTime = System.nanoTime(); // todo o teste
		long startTime = System.currentTimeMillis(); // para cada ronda
		long endTime = 0; // para cada ronda
		long finalTime = 0; // para todo o teste

		for (int i = 0; i < 1000; i++) {
			// System.out.println("Round: "+i);
			SecretKey key = HomoRand.generateKey();
			endTime = System.currentTimeMillis();
			// System.out.println("Chave gerada em: "+(endTime-startTime));
			startTime = endTime;
			byte[] iV = HomoRand.generateIV();
			endTime = System.currentTimeMillis();
			// System.out.println("IV gerado em: "+(endTime-startTime));
			startTime = endTime;
		}

		finalTime = System.nanoTime();
		System.out.println("Tempo do teste: ~" + (finalTime - initTime) / 1000000 + " ms");
		System.out.println("Tempo por ronda: ~" + ((finalTime - initTime)) / 1000000 + " nanoseconds");
	}
}
