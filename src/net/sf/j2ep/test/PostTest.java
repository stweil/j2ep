/*
 * Copyright 2005 Anders Nyman.
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

package net.sf.j2ep.test;

import java.io.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.sf.j2ep.ProxyFilter;
import net.sf.j2ep.RewriteFilter;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

public class PostTest extends FilterTestCase {
    
    private RewriteFilter rewriteFilter;
    private FilterChain mockFilterChain;

    public void setUp() {        
        rewriteFilter = new RewriteFilter();

        mockFilterChain = new FilterChain() {
            ProxyFilter proxyFilter = new ProxyFilter();

            public void doFilter(ServletRequest theRequest, ServletResponse theResponse) throws IOException, ServletException {
                proxyFilter.init(config);
                proxyFilter.doFilter(theRequest, theResponse, this);
            }
        };

        config.setInitParameter("dataUrl", "/WEB-INF/classes/net/sf/j2ep/test/testData.xml");
        try {
            rewriteFilter.init(config);
        } catch (ServletException e) {
            fail("Problem with init, error given was " + e.getMessage());
        }
    }

    
    public void beginSendParam(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/POST/param.jsp", null, null);
        theRequest.addParameter("testParam", "myValue", WebRequest.POST_METHOD);
    }
    
    public void testSendParam() throws IOException, ServletException {
        rewriteFilter.doFilter(request, response, mockFilterChain);
    }
    
    public void endSendParam(WebResponse theResponse) {
        assertEquals("Checking output", "myValue", theResponse.getText());
    }

    public void beginSendMultipart(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/POST/multipart.jsp", null, null);
        theRequest.addParameter("tmp", "", WebRequest.POST_METHOD);
        
        try {
            PostMethod post = new PostMethod();
            FilePart filePart = new FilePart("theFile", new File("WEB-INF/classes/net/sf/j2ep/test/POSTdata"));
            StringPart stringPart = new StringPart("testParam", "123456");
            Part[] parts = new Part[2];
            parts[0] = stringPart;
            parts[1] = filePart;
            MultipartRequestEntity reqEntitiy = new MultipartRequestEntity(parts, post.getParams());
            
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            reqEntitiy.writeRequest(outStream);

            theRequest.setUserData(new ByteArrayInputStream(outStream.toByteArray()));
            theRequest.addHeader("content-type", reqEntitiy.getContentType());
        } catch (FileNotFoundException e) {
            fail("File was not found " + e.getMessage());
        } catch (IOException e) {
            fail("IOException");
            e.printStackTrace();
        }
    }
    
    public void testSendMultipart() throws IOException, ServletException {
        rewriteFilter.doFilter(request, response, mockFilterChain);
    }
    
    public void endSendMultipart(WebResponse theResponse) {
        assertTrue("Checking for the param", theResponse.getText().contains("123456"));
        assertTrue("Checking for the file data", theResponse.getText().contains("here is some data that will be sent using multipart POST"));
    }
    
}
