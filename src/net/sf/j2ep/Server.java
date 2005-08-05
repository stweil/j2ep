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

package net.sf.j2ep;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A representation of the server. Its main use it to be able to open
 * a connection to the server sending back a executed method.
 * The server has to choose which type of method to invoke and should
 * support all method in the HTTP specification.
 *
 * @author Anders Nyman
 */
public interface Server {

    /**
     * Can do any handling needed of a request before
     * the HttpMethod is executed. Example of handling 
     * is to wrap the request. 
     * 
     * @param request The request we are receiving
     * @param response The response we are receiving
     */
    void prepareForExecution(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Returns the host name and port for this server.
     * @return Host name and port
     */
    String getDomainName();
    
    /**
     * Returns the directory being mapped for this server.
     * The directory starts with a / but doesn't end with 
     * a /. A mapping to the root results in
     * directory being an empty string "".
     * 
     * @return Directory The directory
     */
    String getDirectory();

    
    /**
     * Returns the mapped rule so we can rewrite links.
     * 
     * @return The rule we are mapped to
     */
    Rule getRule();
}
