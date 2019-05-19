package hj;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import hj.mlib.HomoMult;

/**
 * Testa utilizacao da cifra que permite multiplicacoes
 *
 */
public class TestMult {

	public static void main(String[] args) {
		BigInteger original = new BigInteger("10");
		System.out.println("Inteiro inicial plaintext: "+original);
		BigInteger original2 = new BigInteger("15");
		System.out.println("Inteiro 2 inicial plaintext: "+original2);

		KeyPair keyPair = HomoMult.generateKey();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		BigInteger cifrado = HomoMult.encrypt(publicKey, original);
		System.out.println("Inteiro ciphertext: "+cifrado);	
		BigInteger decifrado = HomoMult.decrypt(privateKey, cifrado);
		System.out.println("Inteiro decifrado: "+decifrado);
		BigInteger cifrado2 = HomoMult.encrypt(publicKey, original2);
		System.out.println("Inteiro 2 ciphertext: "+cifrado2);	
		BigInteger multCifrado = HomoMult.multiply(cifrado, cifrado2, publicKey); // multiplica os dois valores cifrados
		System.out.println("Valor da multiplicacao cifrado: "+multCifrado);	
		BigInteger mult = HomoMult.decrypt(privateKey, multCifrado);
		System.out.println("Valor da multiplicacao decifrado: "+mult);	
		// Test serializable
		String chaveGuardada = HomoMult.stringFromKey(keyPair);
		KeyPair keyPair2 = HomoMult.keyFromString(chaveGuardada);
		RSAPublicKey publicKey2 = (RSAPublicKey) keyPair2.getPublic();
		RSAPrivateKey privateKey2 = (RSAPrivateKey) keyPair2.getPrivate();
		multCifrado = HomoMult.encrypt(publicKey2, mult);;// acha o quadrado
		mult = HomoMult.decrypt(privateKey2, multCifrado);
		System.out.println("Valor da multiplicacao: "+mult+" (depois de guardar chave)");	
	}

}
