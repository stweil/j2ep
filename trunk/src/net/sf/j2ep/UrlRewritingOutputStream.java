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
     * Regex matching links in the HTML.
     */
    private static Pattern linkPattern = Pattern.compile("\\b(href=|src=|action=)([\"?\'?])(/[^\"\'\\s>]+)[\"?\'?\\s]", Pattern.CASE_INSENSITIVE | Pattern.CANON_EQ);

    /**
     * Basic constructor.
     * 
     * @param originalStream The stream we are wrapping
     */
    public UrlRewritingOutputStream(ServletOutputStream originalStream) {
        this.originalStream = originalStream;
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
        
        StringBuffer page = new StringBuffer();
        
        Matcher matcher = linkPattern.matcher(stream.toString());
        while (matcher.find()) {
           String link = rule.revert(matcher.group(3));
           System.out.println(link);
           matcher.appendReplacement(page, "$1$2" + link + "$2");
        }
        matcher.appendTail(page);
        originalStream.print(page.toString());
        stream.close();
        originalStream.close();
    }
    
    /**
     * Sends the output to the original response stream without
     * doing any link rewriting.
     * @throws IOException Is thrown when there is a problem with the streams
     */
    public void process() throws IOException {
        originalStream.write(stream.toByteArray());
        stream.close();
        originalStream.close();
    }
    
    

}
