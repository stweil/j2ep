package net.sf.j2ep;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;

/**
 * A basic implementation of the RequestHandler.
 * Includes a method to set the headers with.
 *
 * @author Anders Nyman
 */
public abstract class RequestHandlerBase implements RequestHandler {
    
    /** 
     * A set of headers that are not to be set in the request,
     * these headers are for example Connection.
     */
    public static Set<String> bannedHeaders;

    public abstract HttpMethod process(HttpServletRequest request, String url) throws IOException;
    
    /**
     * Will write all request headers stored in the request to the method that
     * are not in the set of banned headers.
     * The Accept-Endocing header is also changed to allow compressed content
     * connection to the server even if the end client doesn't support that. 
     * A Via headers is created as well in compliance with the RFC.
     * 
     * @param method The HttpMethod used for this connection
     * @param request The incoming request
     */
    protected void setHeaders(HttpMethod method, HttpServletRequest request) {
        Enumeration headers = request.getHeaderNames();
        String connectionToken = request.getHeader("connection");
        
        while (headers.hasMoreElements()) {
            String name = (String) headers.nextElement();
            boolean isToken = (connectionToken != null && name.compareToIgnoreCase(connectionToken) == 0);
            
            if (!isToken && !bannedHeaders.contains(name.toLowerCase())) {
                Enumeration value = request.getHeaders(name);
                while (value.hasMoreElements()) {
                    method.addRequestHeader(name, (String) value.nextElement());
                } 
            } 
        } 
        
        String originalVia = request.getHeader("via");
        StringBuffer via = new StringBuffer("");
        if (originalVia != null) {
            via.append(originalVia).append(", ");
        }
        via.append(request.getProtocol()).append(" ").append(request.getServerName());
        
        method.setRequestHeader("via", via.toString());
        method.setRequestHeader("accept-encoding", "gzip, deflate");
    }

}
