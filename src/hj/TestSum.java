package hj;

import java.math.BigInteger;

import hj.mlib.HelpSerial;
import hj.mlib.HomoAdd;
import hj.mlib.PaillierKey;

/**
 * Testa utilizacao de cifra que permite adicionar valores com dados cifrados.
 */
public class TestSum
{

	public static void main(String[] args) {

		try {
			PaillierKey pk = HomoAdd.generateKey();
			pk.printValues();
			BigInteger big1 = new BigInteger("33");
			BigInteger big2 = new BigInteger("22");
			BigInteger big1Code = HomoAdd.encrypt(big1, pk);				
			BigInteger big2Code = HomoAdd.encrypt(big2, pk);
			System.out.println("big1:     " + big1);
			System.out.println("big2:     " + big2);
			System.out.println("big1Code: " + big1Code);
			System.out.println("big2Code: " + big2Code);
			BigInteger big1plus2Code = HomoAdd.sum(big1Code, big2Code, pk.getNsquare());
			System.out.println("big1+big2 Code: " + big1plus2Code);
			BigInteger big1plus2 = HomoAdd.decrypt(big1plus2Code, pk);
			System.out.println("Resultado = " + big1plus2.intValue());

			System.out.println("Teste de subtracao");
			BigInteger big1minus2Code = HomoAdd.dif(big1Code, big2Code, pk.getNsquare());
			System.out.println("big1-big2 Code: " + big1minus2Code);
			BigInteger big1minus2 = HomoAdd.decrypt(big1minus2Code, pk);
			System.out.println("Resultado = " + big1minus2.intValue());

			// Test key serialization
			String chaveGuardada = "";

			chaveGuardada = HelpSerial.toString(pk);

			System.out.println("Chave guardada: " + chaveGuardada);
			// Test with saved key
			PaillierKey pk2 = null;
			BigInteger op3 = null;
			pk2 = (PaillierKey) HelpSerial.fromString(chaveGuardada);
			op3 = HomoAdd.decrypt(big1minus2, pk2);
			System.out.println("Subtracao: " + op3);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
