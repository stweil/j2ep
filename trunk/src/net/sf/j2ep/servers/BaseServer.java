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

package net.sf.j2ep.servers;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.Rule;
import net.sf.j2ep.Server;

/**
 * A basic implementation of the Server interface using a single host name to map
 * all connections. Can be easily extended to create a server that gets it host
 * name in some other way. For instance a server fetching the host name from the
 * request could be made to change the proxy into a forwarding proxy.
 * 
 * @author Anders Nyman
 */
public class BaseServer implements Server {
    
    /** 
     * The rule we are mapped to.
     */
    private Rule rule;
    
    /** 
     * Marks if this rule server will do any
     * rewriting of links.
     */
    private boolean isRewriting;
    
    /**
     * The host and port for this server
     */
    private String domainName;
    
    /**
     * The host and port for this server
     */
    private String directory;
    
    /**
     * Basic constructor that will initialize
     * the directory to "".
     */
    public BaseServer() {
        directory = "";
        isRewriting = false;
    }   

    /**
     * Will not need any wrapping to the default request is
     * returned.
     * 
     * @see net.sf.j2ep.Server#wrapRequest(javax.servlet.ServletRequest)
     */
    public HttpServletRequest wrapRequest(ServletRequest request) {
        return (HttpServletRequest) request;
    }
    
    /**
     * @see net.sf.j2ep.Server#getDomainName()
     */
    public String getDomainName() {
        return domainName;
    }
    
    /**
     * @see net.sf.j2ep.Server#getDirectory()
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @see net.sf.j2ep.Server#getRule()
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @see net.sf.j2ep.Server#isRewriting()
     */
    public boolean isRewriting() {
        return isRewriting;
    }
    
    /**
     * Set if this server wants absolute links mapped
     * for this server to be rewritten.
     * 
     * @param rewrite Should be true if we want to do rewriting
     */
    public void setIsRewriting(String rewrite) {
        if (rewrite != null && rewrite.equals("true")) {
            isRewriting = true;
        }
    }
    
    /**
     * @see net.sf.j2ep.Server#setRule(net.sf.j2ep.Rule)
     */
    public void setRule(Rule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("The rule cannot be null.");
        } else {
            this.rule = rule;
        }
    }
    
    /**
     * Sets the host and port we are mapping to.
     * 
     * @param domainName Value to set
     */
    public void setDomainName(String domainName) {
        if (domainName == null) {
            throw new IllegalArgumentException(
                    "The hostAndPort string cannot be null.");
        } else {
            this.domainName = domainName;
        }
    }
    
    /**
     * Sets the directory we are mapping to.
     * @param directory The directory
     */
    public void setDirectory(String directory) {
        if (directory == null) {
            directory = "";
        } else {
            this.directory = directory;
        }
    }

}
