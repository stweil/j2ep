/*
 * Copyright 2000,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.j2ep;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UrlRewritingResponseWrapper extends HttpServletResponseWrapper{
    
    /** 
     * Stream we are using for the response.
     */
    private UrlRewritingOutputStream outStream;
    
    /** 
     * Rule used for this page
     */
    private Rule rule;
    
    /** 
     * The location for this server, used when we rewrite absolute URIs
     */
    private String ownHostName;
    
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
     * Logging element supplied by commons-logging.
     */
    private static Log log;
    
    /** 
     * Marks if we are rewriting the stream or not.
     */
    private boolean isRewriting;
    
    /**
     * Basic constructor.
     * 
     * @param response The response we are wrapping
     * @param rule The rule that was matched
     * @param ownHostName String we are rewriting servers to
     * @throws IOException When there is a problem with the streams
     */
    public UrlRewritingResponseWrapper(HttpServletResponse response, Rule rule, String ownHostName, String contextPath, Collection serverCollection) throws IOException {
        super(response);
        this.rule = rule;
        this.ownHostName = ownHostName;
        this.contextPath = contextPath;
        isRewriting = false;
        
        log = LogFactory.getLog("net.sf.j2ep.rewriter");        
        outStream = new UrlRewritingOutputStream(response.getOutputStream(), ownHostName, contextPath, serverCollection);
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
            matcher.appendReplacement(header, "$1" + ownHostName + contextPath + link);
        }
        matcher.appendTail(header);
        log.debug("Location header rewritten "+ value + " >> " + header.toString());
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
        log.debug("Set-Cookie header rewritten "+ value + " >> " + header.toString());
        matcher.appendTail(header);
        return header.toString();
    }
    
    /**
     * Returns out own output stream that can be rewritten.
     * 
     * @see javax.servlet.ServletResponse#getOutputStream()
     */
    public ServletOutputStream getOutputStream() throws IOException {
        String contentType = getContentType();
        isRewriting = contentType != null && shouldRewrite(contentType);
        if (isRewriting) {
            return outStream;
        } else {
            return super.getOutputStream();
        }
    }
    
    /**
     * Rewrites the output stream to change any links.
     * 
     * @throws IOException Is thrown when there is a problem with the streams
     */
    public void processStream() throws IOException {
        if (isRewriting) {
            outStream.rewrite(rule);
        }
        super.getOutputStream().flush();
        super.getOutputStream().close();
        outStream.close();
        
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
