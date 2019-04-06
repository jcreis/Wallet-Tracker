package security;

import java.security.*;
import java.security.spec.*;

/** Esta class representa a chave privada de um par de chaves assimetricas.
 */
public class PrivateKey extends Key {
    
    public PrivateKey( String algorithm, java.security.PrivateKey key ) throws Exception {
        super( algorithm, key ) ;
    }
    
    /** Constroi uma chave privada a partir da sua representacao externa.
     * @param data a representacao externa em bytes da chave
     * @throws Exception erro interno
     */
    public static PrivateKey createKey( byte[] data ) throws Exception {
        return createKey( "RSA", data ) ;
    }
    public static PrivateKey createKey( String algorithm, byte[] data ) throws Exception {
    	java.security.PrivateKey key = KeyFactory.getInstance(algorithm).generatePrivate( new PKCS8EncodedKeySpec( data) ) ;
    	return new PrivateKey( algorithm, key);
    }
}
