package net.sf.j2ep.factories;

public class MethodNotAllowedException extends Exception {

    /** 
     * Our id
     */
    private static final long serialVersionUID = 4149736397823198286L;
   
    private String allowedMethods;
    
    public MethodNotAllowedException(String message, String allowedMethods) {
        super(message);
        this.allowedMethods = allowedMethods;
    }
    
    public String getAllowedMethods() {
        return allowedMethods;
    }

}
