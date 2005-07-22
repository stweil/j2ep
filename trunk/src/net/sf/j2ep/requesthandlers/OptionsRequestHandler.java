package net.sf.j2ep.requesthandlers;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.OptionsMethod;

import net.sf.j2ep.RequestHandlerBase;

public class OptionsRequestHandler extends RequestHandlerBase {

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
            throw new IOException(
                    "Incomming request had a Max-Forwards, but didn't contain integer.");
        }
        
        return method;
    }
}
