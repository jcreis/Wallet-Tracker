package hj;

import java.io.UnsupportedEncodingException;
import javax.crypto.SecretKey;
import hlib.hj.mlib.HomoDet;

/**
 * Testa utilizacao da cifra determinista
 *
 */
public class TestEq
{

	public static void main(String[] args) {
		// String key = "Bar12345Bar12345Bar12345"; // 128 bit key
		SecretKey key = HomoDet.generateKey();
		String input1 = "String a usar como String na API da mlib";
		String input2 = "String a usar como Array-Bytes na API da mlib";

		System.out.println();
		System.out.println("=================================== ");
		System.out.println("Variante String usada como String:");
		System.out.println(HomoDet.decrypt(key, HomoDet.encrypt(key, input1)));
		try {
			System.out.println();
			System.out.println("Variante String usada como Array de Bytes:");
			System.out
					.println(new String(HomoDet.decrypt(key, HomoDet.encrypt(key, input2.getBytes("UTF-8"))), "UTF-8"));

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		System.out.println();
		System.out.println("=================================== ");
		String str1 = new String("String 1");
		String str2 = new String("String 2");
		String str3 = new String("String 1");
		String sentenca;
		System.out.println("Teste de igualdade de Cifras na versao String:");
		if (HomoDet.compare(HomoDet.encrypt(key, str1), HomoDet.encrypt(key, str2)))
			sentenca = new String("OK, Igual");
		else
			sentenca = new String("NOK, Diferente");
		System.out.println(str1 + " - " + str2 + " - " + sentenca);
		if (HomoDet.compare(HomoDet.encrypt(key, str1), HomoDet.encrypt(key, str3)))
			sentenca = new String("OK, Igual");
		else
			sentenca = new String("NOK Diferente");
		System.out.println(str1 + " - " + str3 + " - " + sentenca);

		System.out.println();
		System.out.println("Teste de igualdade de Cifras na versao Array de Bytes:");

		try {
			if (HomoDet.compare(HomoDet.encrypt(key, str1.getBytes("UTF-8")),
					HomoDet.encrypt(key, str2.getBytes("UTF-8"))))
				sentenca = new String("Igual");
			else
				sentenca = new String("Diferente");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(str1 + " - " + str2 + " - " + sentenca);

		try {
			if (HomoDet.compare(HomoDet.encrypt(key, str1.getBytes("UTF-8")),
					HomoDet.encrypt(key, str3.getBytes("UTF-8"))))
				sentenca = new String("OK Igual");
			else
				sentenca = new String("NOK Diferente");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println(str1 + " - " + str3 + " - " + sentenca);

		System.out.println();

	}

}
