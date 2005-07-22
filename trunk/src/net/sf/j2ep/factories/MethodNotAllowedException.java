package net.sf.j2ep.factories;

/**
 * An exception thrown when a factory can't handle the incoming method.
 *
 * @author Anders Nyman
 */
public class MethodNotAllowedException extends Exception {

    /** 
     * Our id
     */
    private static final long serialVersionUID = 4149736397823198286L;
   
    /** 
     * List of methods that are being allowed by the factory.
     */
    private String allowedMethods;
    
    public MethodNotAllowedException(String message, String allowedMethods) {
        super(message);
        this.allowedMethods = allowedMethods;
    }
    
    /**
     * Returns which methods that are allowed by the instance throwing
     * this exception.
     * 
     * @return The allowed methods
     */
    public String getAllowedMethods() {
        return allowedMethods;
    }

}
