package net.sf.j2ep;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class UrlRewritingResponseWrapper extends HttpServletResponseWrapper{
    
    private UrlRewritingOutputStream outStream;
    private HttpServletResponse response;

    public UrlRewritingResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        this.response = response;
        
        outStream = new UrlRewritingOutputStream(response.getOutputStream());
    }
    
    public ServletOutputStream getOutputStream() throws IOException {
        return outStream;
    }
    
    public void rewrite() throws IOException {
        if (response.getContentType().contains("text")) {
            outStream.rewrite();
        } else {
            outStream.process();
        }
    }
}
