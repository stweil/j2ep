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

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import net.sf.j2ep.ProxyFilter;
import net.sf.j2ep.RewriteFilter;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

public class DirectoryMappingTest extends FilterTestCase {

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
    
    public void beginBasicMapping(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testDirectoryMapping/main.jsp", null, null);
    }
    
    public void testBasicMapping() throws IOException, ServletException {
        rewriteFilter.doFilter(request, response, mockFilterChain);
    }
    
    public void endBasicMapping(WebResponse theResponse) {
        assertEquals("The response code should be 200", 200, theResponse.getStatusCode());
        assertEquals("Test that we got the right page", "/GET/main.jsp", theResponse.getText());
    }
    
    public void beginRewrite(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testDirectoryMapping/links.jsp", null, null);
    }
    
    public void testRewrite() throws IOException, ServletException {
        rewriteFilter.doFilter(request, response, mockFilterChain);
    }
    
    public void endRewrite(WebResponse theResponse) {
        assertEquals("The response code should be 200", 200, theResponse.getStatusCode());
        assertTrue("Test absolute path", theResponse.getText().contains("<a href=\"http://localhost:8080/test/testDirectoryMapping/test1.jsp\">"));
        assertTrue("Test / path", theResponse.getText().contains("<a href=\"/test/testDirectoryMapping/test2.jsp\">"));
        assertTrue("Test relative path", theResponse.getText().contains("<a href=\"test3.jsp\">"));
        assertTrue("Test absolute path not mapped", theResponse.getText().contains("<a href=\"http://localhost:80/test4.jsp\">"));
    }
}
