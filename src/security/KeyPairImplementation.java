package security;

import java.security.*;

/** Esta classe permite gerar pares de chaves destinadas a opera��es de criptografia assim�trica.
 * Dimens�o m�xima das mensagens a cficrar: 117 bytes.
 */
final public class KeyPairImplementation {
    private PublicKey pubKey ;
    private PrivateKey prvKey ;
    
    /** Cria um par de chaves assim�tricas RSA.
     */
    public static KeyPairImplementation createKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA") ;
            kpg.initialize(1024);
            java.security.KeyPair kp = kpg.generateKeyPair() ;
            
            PublicKey pub = new PublicKey( "RSA", kp.getPublic() ) ;
            PrivateKey priv = new PrivateKey( "RSA", kp.getPrivate() ) ;
            
            return new KeyPairImplementation( pub, priv);
        }
        catch( Exception x ) {
            x.printStackTrace() ;
        }
        return null;
    }
    
    protected KeyPairImplementation(PublicKey pub, PrivateKey priv) {
    	this.pubKey = pub;
    	this.prvKey = priv;
    	
    }

    /** Devolve a chave p�blica do par.
     * @return a chave p�blica que comp�e o par
     */
    public PublicKey getPublic() {
        return pubKey ;
    }
    
    /** Devolve a chave privada do par.
     * @return a chave privada que comp�e o par
     */
    public PrivateKey getPrivate() {
        return prvKey ;
    }
    
}
