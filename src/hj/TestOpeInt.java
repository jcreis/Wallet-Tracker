package hj;

import hlib.hj.mlib.HomoOpeInt;

/**
 * Testa utilizacao da cifra order-preserving
 */
public class TestOpeInt
{

	public static void main(String[] args) {
		HomoOpeInt ope = new HomoOpeInt("Ola Palerma");
		long last = Long.MIN_VALUE;
		long resultado;
		int contrario;
		for (int i = -100; i < 100; i++) {
			resultado = ope.encrypt(i);
			contrario = ope.decrypt(resultado);
			System.out.println("Linha " + i + " - plano " + i + " cifra = " + resultado + " decifra = " + contrario);
			if( last < resultado)
				System.out.println( "OK - preserva ordem");
			else
				System.out.println( "NOK - NAO preserva ordem");
			last = resultado;
		}

	}

}
