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
     * Regex to find absolute links.
     */
    private static Pattern linkPattern = Pattern.compile("\\b([^/]+://)([^/]+)([\\w/]+)", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    /**
     * Basic constructor.
     * 
     * @param response The response we are wrapping
     * @param rule The rule that was matched
     * @param server String we are rewriting servers to
     * @throws IOException When there is a problem with the streams
     */
    public UrlRewritingResponseWrapper(HttpServletResponse response, Rule rule, String server) throws IOException {
        super(response);
        this.response = response;
        this.rule = rule;
        this.server = server;
        
        outStream = new UrlRewritingOutputStream(response.getOutputStream());
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
            value = rewriteHeader(originalValue);
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
            value = rewriteHeader(originalValue);
        } else {
            value = originalValue;
        }
        super.setHeader(name, value);
    }

    
    /**
     * Rewrites the header. Will first locate any
     * links in the header and then rewrite them.
     * 
     * @param value The header value we are to rewrite
     * @return A rewritten header
     */
    private String rewriteHeader(String value) {
        StringBuffer header = new StringBuffer();

        Matcher matcher = linkPattern.matcher(value);
        while (matcher.find()) {
            String link = rule.revert(matcher.group(3));
            matcher.appendReplacement(header, "$1" + server + link);
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
        if (contentType != null && rule.isRewriting() && (contentType.contains("text") || contentType.contains("script"))) {
            outStream.rewrite(rule);
        } else {
            outStream.process();
        }
    }
}
