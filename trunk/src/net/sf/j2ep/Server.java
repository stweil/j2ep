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
     * Returns the host name and port for this server.
     * @return Host name and port
     */
    String getHostAndPort();
    
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
     * Returns the full URL to this server including
     * directories on the server
     * @return The full server path
     */
    String getFullPath();
    
    /**
     * Sets the rule that is mapped for this server. Will
     * be used when we rewrite links to know how a absolute 
     * path should be rewritten.
     * 
     * @param rule The rule
     */
    void setRule(Rule rule);
    
    /**
     * Returns the mapped rule so we can rewrite links.
     * 
     * @return The rule we are mapped to
     */
    Rule getRule();
    
    /**
     * Marks if this server should rewrite absolute 
     * links found in html pages.
     * 
     * @return true if the rule should be rewritten, false otherwise
     */
    boolean isRewriting();
}
