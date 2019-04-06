package security;

import java.security.*;
import java.security.spec.*;

/** Esta class representa a chave p�blica de um par de chaves assim�tricas.
 */
public class PublicKey extends Key {
    
    public PublicKey( String algorithm, java.security.PublicKey key ) throws Exception {
        super( algorithm, key ) ;
    }
    
    /** Constr�i uma chave p�blica a partir da sua representa��o externa.
     * @param data a representa��o externa em bytes da chave
     * @throws Exception erro interno
     */
    public static PublicKey createKey( byte[] data ) throws Exception {
        return createKey( "RSA", data ) ;
    }
    public static PublicKey createKey( String algorithm, byte[] data ) throws Exception {
    	java.security.PublicKey key = KeyFactory.getInstance(algorithm).generatePublic( new X509EncodedKeySpec( data) ) ;
    	return new PublicKey( algorithm, key);
    }
}
