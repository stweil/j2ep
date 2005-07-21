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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.j2ep.ProxyFilter;

import org.apache.cactus.FilterTestCase;
import org.apache.cactus.WebRequest;
import org.apache.cactus.WebResponse;

public class PostTest extends FilterTestCase {
    
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

    
    public void beginSendParam(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/POST/param.jsp", null, null);
        theRequest.addParameter("testParam", "testValue", WebRequest.POST_METHOD);
    }
    
    public void testSendParam() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
    }
    
    public void endSendParam(WebResponse theResponse) {
        assertEquals("Checking output", "testValue", theResponse.getText());
    }
    
    public void beginExpectContinue(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/POST/param.jsp", null, null);
        theRequest.addHeader("Expect", "100-continue");
        theRequest.addHeader("TestHeader", "tst");
        theRequest.addParameter("testParam", "testValue", WebRequest.POST_METHOD);
    }
    
    public void testExpectContinue() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
        /*
         * Will not send any body, but that shouldn't be a problem since the server
         * will send back a 100 - continue first 
         */
        /*
        MethodWrappingRequest req = new MethodWrappingRequest("POST", "/POST/param.jsp", null);
        req.addHeader("Expect", "100-continue");
        req.addHeader("TestHeader", "tst");
        proxy.doFilter(req, response, filterChain);
        */
    }
    
    public void endExpectContinue(WebResponse theResponse) {
        assertEquals("We should get status code 100", 100, theResponse.getStatusCode());
    }
}
