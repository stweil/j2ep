/*
 * Copyright 2000,2004 Anders Nyman.
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

import net.sf.j2ep.factories.ResponseHandlerFactory;
import junit.framework.TestCase;

public class AllowHeaderTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public void testprocessAllowHeader() {
        String allow = "OPTIONS,PROPFIND,OP,PUT";
        String correct = "OPTIONS,PUT,";
        String returned = ResponseHandlerFactory.processAllowHeader(allow);
        assertEquals("Checking factory implementation for allow header", correct, returned);
    }

}
