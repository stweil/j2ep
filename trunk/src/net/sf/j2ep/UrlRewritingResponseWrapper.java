package net.sf.j2ep;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class UrlRewritingResponseWrapper extends HttpServletResponseWrapper{
    
    /** 
     * Stream we are using for the response.
     */
    private UrlRewritingOutputStream outStream;
    
    /** 
     * The response we are wrapping
     */
    private HttpServletResponse response;
    
    /** 
     * Rule used for this page
     */
    private Rule rule;
    
    /** 
     * The location for this server, used when we rewrite absolute URIs
     */
    private String server;
    
    /** 
     * The contextPath, needed when we rewrite links.
     */
    private String contextPath;
    
    /** 
     * Regex to find absolute links.
     */
    private static Pattern linkPattern = Pattern.compile("\\b([^/]+://)([^/]+)([\\w/]+)", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
    
    /** 
     * Regex to find the path in Set-Cookie headers.
     */
    private static Pattern pathAndDomainPattern = Pattern.compile("\\b(path=|domain=)([^;\\s]+)", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    
    /**
     * Basic constructor.
     * 
     * @param response The response we are wrapping
     * @param rule The rule that was matched
     * @param server String we are rewriting servers to
     * @throws IOException When there is a problem with the streams
     */
    public UrlRewritingResponseWrapper(HttpServletResponse response, Rule rule, String server, String contextPath) throws IOException {
        super(response);
        this.response = response;
        this.rule = rule;
        this.server = server;
        this.contextPath = contextPath;
        
        outStream = new UrlRewritingOutputStream(response.getOutputStream(), server, contextPath);
    }
    
    /**
     * Checks if we have to rewrite the header and
     * if so will rewrite it.
     * 
     * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
     */
    public void addHeader(String name, String originalValue) {
        String value;
        if (name.toLowerCase().equals("location")) {
            value = rewriteLocation(originalValue);
        } else if (name.toLowerCase().equals("set-cookie")) {
            value = rewriteSetCookie(originalValue);
        } else {
            value = originalValue;
        }
        super.addHeader(name, value);
    }
    
    /**
     * Checks if we have to rewrite the header and
     * if so will rewrite it.
     * 
     * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
     */
    public void setHeader(String name, String originalValue) {
        String value;
        if (name.toLowerCase().equals("location")) {
            value = rewriteLocation(originalValue);
        } else if (name.toLowerCase().equals("set-cookie")) {
            value = rewriteSetCookie(originalValue);
        }
        else {
            value = originalValue;
        }
        super.setHeader(name, value);
    }

    
    /**
     * Rewrites the location header.
     * Will first locate any links in the header and then rewrite them.
     * 
     * @param value The header value we are to rewrite
     * @return A rewritten header
     */
    private String rewriteLocation(String value) {
        StringBuffer header = new StringBuffer();

        Matcher matcher = linkPattern.matcher(value);
        while (matcher.find()) {
            String link = rule.revert(matcher.group(3));
            matcher.appendReplacement(header, "$1" + server + contextPath + link);
        }
        matcher.appendTail(header);
        return header.toString();
    }
    
    /**
     * Rewrites the header Set-Cookie so that path and domain 
     * is correct.
     * 
     * @param value The original header
     * @return The rewritten header
     */
    private String rewriteSetCookie(String value) {
        StringBuffer header = new StringBuffer();

        Matcher matcher = pathAndDomainPattern.matcher(value);
        while (matcher.find()) {
            if (matcher.group(1).equals("path=")) {
                String path = rule.revert(matcher.group(2));
                matcher.appendReplacement(header, "$1" + path); 
            } else {
                matcher.appendReplacement(header, "");
            }
            
        }
        matcher.appendTail(header);
        return header.toString();
    }
    
    /**
     * Returns out own output stream that can be rewritten.
     * 
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        return outStream;
    }
    
    /**
     * Rewrites the output stream to change any links.
     * 
     * @throws IOException Is thrown when there is a problem with the streams
     */
    public void rewriteStream() throws IOException {
        String contentType = response.getContentType();
        if (rule.isRewriting() && contentType != null && shouldRewrite(contentType)) {
            outStream.rewrite(rule);
        } else {
            outStream.process();
        }
    }
    
    /**
     * Checks the contentType to evaluate if we should do 
     * link rewriting for this content.
     * 
     * @param contentType The Content-Type header
     * @return true if we need to rewrite links, false otherwise
     */
    private boolean shouldRewrite(String contentType) {
        String lowerCased = contentType.toLowerCase();
        return (lowerCased.contains("html") || lowerCased.contains("css") || lowerCased.contains("javascript"));
    }
}
