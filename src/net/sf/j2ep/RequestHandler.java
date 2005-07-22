package net.sf.j2ep;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;

public interface RequestHandler {
    
    public HttpMethod process(HttpServletRequest request, String url) throws IOException;

}
