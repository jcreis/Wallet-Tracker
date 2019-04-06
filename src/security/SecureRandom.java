package security;

/** Esta classe permite obter geradores seguros de sequ�ncias aleat�rias de bytes.
 */
final public class SecureRandom {
    
    /** Cria um novo gerador com uma semente aleat�ria.
     */
    public SecureRandom() {
        this( null ) ;
    }
    
    /** Cria um novo gerador dada uma semente, de modo a poder reproduzir a sequ�ncia.
     * @param seed uma sequ�ncia de bytes que ser� usada como semente
     */    
    public SecureRandom( byte[] seed ) {
        try {
            sr = java.security.SecureRandom.getInstance( "sha1PRNG") ;
            if( seed != null )
            	sr.setSeed( seed ) ;
        }
        catch( Exception x ) {
            x.printStackTrace() ;
        }    
    }
        
    /** Gera uma sequ�ncia aleat�ria de bytes.
     * @param count a dimens�o em bytes da sequ�ncia gerar
     * @return os bytes da sequencia aleat�ria
     */    
    public byte[] randomBytes( int count ) {
        byte[] tmp = new byte[ count ] ;
        sr.nextBytes( tmp ) ;
        return tmp ;
    }
    
    private java.security.SecureRandom sr ;
}
