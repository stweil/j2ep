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

public class DirectoryMappingTest extends FilterTestCase {

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
    
    public void beginBasicMapping(WebRequest theRequest) {
        theRequest.setURL("localhost:8080", "/test", "/testDirectoryMapping/main.jsp", null, null);
    }
    
    public void testBasicMapping() throws IOException, ServletException {
        proxy.doFilter(request, response, filterChain);
    }
    
    public void endBasicMapping(WebResponse theResponse) {
        assertEquals("The response code should be 200", 200, theResponse.getStatusCode());
        assertEquals("The that we got the right page", "/GET/main.jsp", theResponse.getText());
    }
}