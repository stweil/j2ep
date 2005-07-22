package net.sf.j2ep.requesthandlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;

import net.sf.j2ep.RequestHandlerBase;

/**
 * Handler for the OPTIONS method.
 *
 * @author Anders Nyman
 */
public class OptionsRequestHandler extends RequestHandlerBase {

    /**
     * @see net.sf.j2ep.RequestHandler#process(javax.servlet.http.HttpServletRequest, java.lang.String)
     * 
     * Sets the headers and does some checking for if this request
     * is meant for the server or for the proxy. This check is done
     * by looking at the Max-Forwards header. 
     */
    public HttpMethod process(HttpServletRequest request, String url) throws IOException {
        OptionsMethod method = new OptionsMethod(url);
        setHeaders(method, request);
        
        try {
            int max = request.getIntHeader("Max-Forwards");

            if (max == 0) {
                method.abort();
            } else if (max != -1) {
                method.setRequestHeader("Max-Forwards", ((Integer) (max - 1))
                        .toString());
            }

        } catch (NumberFormatException e) {
           
        }
        
        return method;
    }
}
