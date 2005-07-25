package net.sf.j2ep;

import java.io.IOException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RewriteFilter implements Filter {
    
    /** 
     * Logging element supplied by commons-logging.
     */
    private static Log log;

    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain filterChain) throws IOException, ServletException {
        
        if (response.isCommitted()) {
            log.info("Not proxying, already committed.");
            return;
        } else if (!(request instanceof HttpServletRequest)) {
            log.info("Request is not HttpRequest, will only handle HttpRequests.");
            return;
        } else if (!(response instanceof HttpServletResponse)) {
            log.info("Request is not HttpResponse, will only handle HttpResponses.");
            return;
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            UrlRewritingResponseWrapper wrappedResponse;
            wrappedResponse = new UrlRewritingResponseWrapper(httpResponse);
            
            filterChain.doFilter(request, wrappedResponse);
            
            wrappedResponse.rewrite();
        }
    }
    
    public void init(FilterConfig arg0) throws ServletException {
        log = LogFactory.getLog("org.apache.webapp.reverseproxy");
    }

    public void destroy() {
        log = null;
    }

}
