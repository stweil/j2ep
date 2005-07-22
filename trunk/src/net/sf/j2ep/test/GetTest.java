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

import javax.servlet.ServletException;

import net.sf.j2ep.ProxyFilter;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

/**
 * Tests the GetHandler. Is the main testclass for this program since it will
 * test some simple default behavior.
 * 
 * @author Anders Nyman
 */
public class GetTest extends FilterTestCase {

    private ProxyFilter proxy;

    public void setUp() {
        proxy = new ProxyFilter();

        config.setInitParameter("dataUrl",
                        "/WEB-INF/classes/net/sf/j2ep/test/testData.xml");
        try {
            proxy.init(config);
        } catch (ServletException e) {
            fail("Problem with init, error given was " + e.getMessage());
        }
    }
    
    public void beginNormalRequest(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/GET/main.jsp", null, null);
    }
    
    public void testNormalRequest() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
    }
    
    public void endNormalRequest(WebResponse theResponse) {
        assertEquals("The response code should be 200", 200, theResponse.getStatusCode());
        assertEquals("Checking for correct page", "/GET/main.jsp", theResponse.getText());
    }
    
    public void begin404(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/GET/nonexistant.jsp", null, null);
    }
    
    public void test404() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
    }
    
    public void end404(WebResponse theResponse) {
        assertEquals("The response code should be 404", 404, theResponse.getStatusCode());
    }
    
    public void beginNonExistentServer(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testNonExistentServer/", null, null);
    }
    
    public void testNonExistentServer() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
    }
    
    public void endNonExistentServer(WebResponse theResponse) {
        assertEquals("The response code should be 504", 504, theResponse.getStatusCode());
    }
    
    public void beginConditional(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/GET/image.gif", null, null);
        theRequest.addHeader("If-Unmodified-Since", "Wed, 20 Jul 2000 15:00:00 GMT");
    }
    
    public void testConditional() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
    }
    
    public void endConditional(WebResponse theResponse) {
        assertEquals("The response code should be 412", 412, theResponse.getStatusCode());
    }
    
    public void testUnhandledMethod() throws ServletException, IOException {
        MethodWrappingRequest req = new MethodWrappingRequest("JDFJDSJSN", "/",
                null);
        proxy.doFilter(req, response, filterChain);

    }

    public void endUnhandledMethod(WebResponse theResponse) {
        assertEquals("Checking that we got a 405 response", 405, theResponse.getStatusCode());
        assertEquals("Correct options not returned",
                "OPTIONS,GET,HEAD,POST,PUT,DELETE", theResponse.getConnection()
                        .getHeaderField("Allow"));
    }
    
}
