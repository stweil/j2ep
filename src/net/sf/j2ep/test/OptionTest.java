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
import org.apache.cactus.WebResponse;

/**
 * Tests the OptionsHandler
 *
 * @author Anders Nyman
 */
public class OptionTest extends FilterTestCase {
    
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

    public void testNoMaxFowards() throws ServletException, IOException {
        MethodWrappingRequest req = new MethodWrappingRequest("OPTIONS", "/",
                null);
        proxy.doFilter(req, response, filterChain);

    }

    public void endNoMaxFowards(WebResponse theResponse) {
        assertEquals("Correct options not returned",
                "GET,HEAD,POST,PUT,DELETE,OPTIONS,", theResponse.getConnection()
                        .getHeaderField("Allow"));
    }
    
    public void testStarUri() throws ServletException, IOException {
        //TODO change this to * if that functionally really is required.
        //Will take some time since httpclient doesn't support it.
        MethodWrappingRequest req = new MethodWrappingRequest("OPTIONS", "/*",
                null);
        proxy.doFilter(req, response, filterChain);

    }

    public void endStarUri(WebResponse theResponse) {
        assertEquals("Correct options not returned",
                "GET,HEAD,POST,PUT,DELETE,OPTIONS,", theResponse.getConnection()
                        .getHeaderField("Allow"));
    }

    public void testMaxForwards() throws ServletException,
            IOException {
        MethodWrappingRequest req = new MethodWrappingRequest("OPTIONS", "/maxForwards",
                null);
        req.addHeader("Max-Forwards", "0");
        proxy.doFilter(req, response, filterChain);
    }
    
    public void endMaxForwards(WebResponse theResponse) {
        assertEquals("Status code check", 200, theResponse.getStatusCode());
        String allow = theResponse.getConnection().getHeaderField("allow");
        /*
         * Reason we have to check all and not just compare to string is
         * that the order of the methods returned cannot is unknown and unimportant.
         */
        assertTrue("Should include OPTIONS", allow.contains("OPTIONS"));
        assertTrue("Should include GET", allow.contains("GET"));
        assertTrue("Should include HEAD", allow.contains("HEAD"));
        assertTrue("Should include POST", allow.contains("POST"));
        assertTrue("Should include PUT", allow.contains("PUT"));
        assertTrue("Should include DELETE", allow.contains("DELETE"));
        assertEquals("Content Length should be 0", "0", theResponse.getConnection().getHeaderField("Content-Length"));
    }
    
    public void testServerWithUnsupportedMethods() throws ServletException, IOException {
        MethodWrappingRequest req = new MethodWrappingRequest("OPTIONS", "/testUnsupportedMethods/",
                null);
        proxy.doFilter(req, response, filterChain);
    }
    
    public void endServerWithUnsupportedMethods(WebResponse theResponse) {
        String allow = theResponse.getConnection().getHeaderField("Allow");

        assertTrue("Should include OPTIONS", allow.contains("OPTIONS"));
        assertTrue("Should include GET", allow.contains("GET"));
        assertTrue("Should include HEAD", allow.contains("HEAD"));
        assertTrue("Should include POST", allow.contains("POST"));
        assertTrue("Should include DELETE", allow.contains("DELETE"));
        assertFalse("Shouldn't include TRACE", allow.contains("TRACE"));
        assertFalse("Shouldn't include PROPPATCH", allow.contains("PROPPATCH"));
        assertFalse("Shouldn't include COPY", allow.contains("COPY"));
        assertFalse("Shouldn't include MOVE", allow.contains("MOVE"));
        assertFalse("Shouldn't include LOCK", allow.contains("LOCK"));
        assertFalse("Shouldn't include UNLOCK", allow.contains("UNLOCK"));
        assertFalse("Shouldn't include PROPFIND", allow.contains("PROPFIND"));
    }
}