package net.sf.j2ep.requesthandlers;

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.RequestHandlerBase;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;

public class BasicRequestHandler extends RequestHandlerBase {

    public HttpMethod process(HttpServletRequest request, String url) {
        
        HttpMethodBase method = null;
      
        if (request.getMethod().equals("GET")) {
            method = new GetMethod(url);
        } else if (request.getMethod().equals("HEAD")) {
            method = new HeadMethod(url);
        } else if (request.getMethod().equals("DELETE")) {
            method = new DeleteMethod(url);
        } else {
            return null;
        }
        
        setHeaders(method, request);
        return method;
    }
      

}
