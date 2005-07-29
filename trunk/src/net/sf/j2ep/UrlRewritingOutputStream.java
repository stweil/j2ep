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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletOutputStream;

public class UrlRewritingOutputStream extends ServletOutputStream {
    
    /** 
     * The stream we are wrapping, is the original response stream.
     */
    private ServletOutputStream originalStream;
    
    /** 
     * Stream that is written to, works as a buffer for the response stream.
     */
    private ByteArrayOutputStream stream;
    
    /** 
     * The server, needed when we rewrite absolute links.
     */
    private String ownHostName;
    
    /** 
     * The contextPath, needed when we rewrite links.
     */
    private String contextPath;
    
    /** 
     * Regex matching links in the HTML.
     */
    private static Pattern linkPattern = Pattern.compile("\\b(href=|src=|action=|url\\()([\"\'])(([^/]+://)([^/]+))?(/[^\"\\s\'>]+)[\"\'\\s]", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);
    //private static Pattern linkPattern = Pattern.compile("\\b(href=|src=|action=)([\"?\'?])(/[^\"\'\\s>]+)[\"?\'?\\s]", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    /**
     * Basic constructor.
     * 
     * @param originalStream The stream we are wrapping
     */
    public UrlRewritingOutputStream(ServletOutputStream originalStream, String ownHostName, String contextPath) {
        this.originalStream = originalStream;
        this.ownHostName = ownHostName;
        this.contextPath = contextPath;
        
        stream = new ByteArrayOutputStream();
    }

    /**
     * @see java.io.OutputStream#write(int)
     */
    public void write(int b) throws IOException {
        stream.write(b);        
    }
    
    /**
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    public void write(byte[] b, int off, int len) throws IOException {
        stream.write(b, off, len);
    }
    
    /**
     * @see java.io.OutputStream#write(byte[])
     */
    public void write(byte[] b) throws IOException {
        stream.write(b);
    }
    
    /**
     * Processes the stream looking for links, all links
     * found are rewritten. After this the stream is written
     * to the response.
     * 
     * @param rule The rule knowing how to rewrite
     * @throws IOException Is thrown when there is a problem with the streams
     */
    public void rewrite(Rule rule) throws IOException {
        /*
         * Using regex can be quite harsh sometimes so here is how
         * the regex trying to find links works
         * 
         * \\b(href=|src=|action=|url\\()([\"?\'?])
         * This part is the identification of links, matching
         * something like href=", href=' and href=
         * 
         * (([^/]+://)([^/]+))?
         * This is to identify absolute paths. A link doesn't have
         * to be absolute therefor there is a ?.
         * 
         * (/[^\"\\s\'>]+)
         * This is the link, has to start with a / since relative
         * links shouldn't be rewritten
         * 
         * [\"?\'?\\s]
         * Ending ", ' or whitespace
         * 
         * $1 - link type, e.g. href=
         * $2 - ", ' or whitespace
         * $3 - The entire http://www.server.com if present
         * $4 - The protocol, e.g http:// or ftp:// 
         * $5 - The host name, e.g. www.server.com
         * $6 - The link
         */
        StringBuffer page = new StringBuffer();
        
        Matcher matcher = linkPattern.matcher(stream.toString());
        while (matcher.find()) {
            
           String serverDir = rule.getServer().getDirectory();
           String link = matcher.group(6).replace("$", "\\$");

           if ( serverDir.equals("") || link.startsWith(serverDir+"/") ) {
               link = rule.revert( link.substring(serverDir.length()) );
               if (matcher.group(4) != null) {
                   String endServer = rule.getServer().getHostAndPort();
                    if (matcher.group(5).compareToIgnoreCase(endServer) == 0) {
                        matcher.appendReplacement(page, "$1$2$4" + ownHostName
                                + contextPath + link + "$2");
                    }
                } else {
                    matcher.appendReplacement(page, "$1$2" + contextPath + link
                            + "$2");

                }
            }
           
        }
        
        matcher.appendTail(page);
        originalStream.print(page.toString());
        
    }
    
    /**
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
        stream.close();
    }
    
    

}
