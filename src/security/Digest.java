package security;

import java.security.* ;

/** Classe que permite produzir "digests" de mensagens.
 */
final public class Digest {
    private MessageDigest d ;
    
    /** Constroi um novo "digest" MD5.
     * @throws NoSuchAlgorithmException 
     */    
    public static Digest createDigest() throws NoSuchAlgorithmException {
    	return createDigest( "MD5");
    }
    public static Digest createDigest( String algorithm) throws NoSuchAlgorithmException {
    	return new Digest( algorithm);
    }
    /** Constroi um novo "digest" do tipo indicado.
     * @param type Tipo
     * @throws NoSuchAlgorithmException 
     */    
    protected Digest( String type) throws NoSuchAlgorithmException {
    	d = MessageDigest.getInstance( type) ;
    }
    /** Devolve o digest MD5 do array de bytes indicado
     * @param data os bytes que comp�em a informa��o inicial a digerir
     * @throws NoSuchAlgorithmException 
     */ 
    public static byte[] getDigest( byte[] data ) throws NoSuchAlgorithmException {
    	return getDigest( "MD5", data);
    }
    /** Devolve o digest do tipo indicado do array de bytes indicado
     * @param type Tipo
     * @param data os bytes que comp�em a informa��o inicial a digerir
     * @throws NoSuchAlgorithmException 
     */ 
    public static byte[] getDigest( String type, byte[] data ) throws NoSuchAlgorithmException {
    	MessageDigest d = MessageDigest.getInstance(type);
    	d.update( data ) ;
    	return d.digest();
    }
    /** Actualiza o "digest" com nova informa��o a digerir.
     * @param data os bytes que comp�em a informa��o a digerir
     */    
    public void update( byte[] data ) {
        d.update( data ) ;
    }
    
    /** Actualiza o "digest" com nova informa��o a digerir.
     * @param offset in�cio do texto
     * @param length comprimento do text
     * @param data o texto a digerir
     */    
    public void update( byte[] data, int offset, int length ) {
        d.update( data, offset, length ) ;
    }
    
    /** Finaliza e devolve o "digest".
     * @return os bytes que representam a mensagem digerida
     */    
    public byte[] getDigest() {
        return d.digest() ;
    }
}
