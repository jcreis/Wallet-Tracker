package security;

/** Esta representa uma chave criptogr�fica gen�rica.
 */
public abstract class Key {
    private String algorithm ;
    private java.security.Key key;
    
    protected Key( String algorithm, java.security.Key key ) {
        this.algorithm = algorithm ;
        this.key = key ;
    }

    /** Exporta a chave.
     * @return os bytes que comp�em a representa��o externa da chave
     */
    public byte[] exportKey() {
        try {
        	return key.getEncoded();
        } catch( Exception x ) {
            x.printStackTrace() ;
        }
        return null ;
    }
    
    /** Decifra uma mensagem.
     * @param src os bytes que comp�em a mensagem
     * @return a mensagem depois de decifrada
     */
    public byte[] decrypt(byte[] src) {
        return decrypt( src, 0, src.length );
    }
    
    /** Decifra uma mensagem
     * @param src os bytes que comp�es mensagem
     * @param offset in�cio da mensagem
     * @param length comprimento da mensagem
     * @return mensagem depois de decifrada
     */
    public byte[] decrypt(byte[] src, int offset, int length) {
        try {
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance(algorithm) ;
            c.init( javax.crypto.Cipher.DECRYPT_MODE, key ) ;
            return c.doFinal( src, offset, length ) ;
        }
        catch( Exception x ) {
            x.printStackTrace() ;
        }
        return null ;}
    
    /** Cifra uma mensagem.
     * @param src os bytes que comp�em a mensagem
     * @return os bytes que comp�em a mensagem depois de cifrada
     */
    public byte[] encrypt(byte[] src) {
        return encrypt( src, 0, src.length ) ;
    }
    
    /** Cifra uma mensagem.
     * @param src os bytes que comp�em a mensagem
     * @param offset in�cio da mensagem
     * @param length comprimento da mensagem
     * @return os bytes da mensagem depois de cifrada
     */
    public byte[] encrypt(byte[] src, int offset, int length) {
        try {
            javax.crypto.Cipher c = javax.crypto.Cipher.getInstance(algorithm) ;
            c.init( javax.crypto.Cipher.ENCRYPT_MODE, key ) ;
            return c.doFinal( src, offset, length ) ;
        }
        catch( Exception x ) {
            x.printStackTrace() ;
        }
        return null ;
    }
}
