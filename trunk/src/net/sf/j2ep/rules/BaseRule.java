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


package net.sf.j2ep.rules;

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.Rule;
import net.sf.j2ep.Server;

/**
 * The BaseRule is an empty rule
 * implementation which can be
 * subclassed for extension.
 * This class is based on the work by Yoav Shapira
 * for the balancer webapp.
 *
 * @author Anders Nyman
 */
public abstract class BaseRule implements Rule {

    /** 
     * The server this rule is bound to.
     */
    private Server server;
    /** 
     * The servers id, used for mapping the correct server.
     */
    private String serverId;

    /**
     * @see Rule#matches(HttpServletRequest)
     */
    public abstract boolean matches(HttpServletRequest request);
    
    /**
     * @see net.sf.j2ep.Rule#process(java.lang.String)
     */
    public String process(String uri) {
        return uri;
    }
    
    /**
     * @see net.sf.j2ep.Rule#revert(java.lang.String)
     */
    public String revert(String uri) {
        return uri;
    }

    /**
     * Returns a String representation of this object.
     *
     * @return String
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[");
        buffer.append(getClass().getName());
        buffer.append(": ");

        buffer.append("]");

        return buffer.toString();
    }

    /**
     * @see net.sf.j2ep.Rule#getServer()
     */
    public Server getServer() {
        return server;
    }

    /**
     * @see net.sf.j2ep.Rule#setServer(net.sf.j2ep.Server)
     */
    public void setServer(Server server) {
        this.server = server;
    }

    /**
     * @see net.sf.j2ep.Rule#getServerId()
     */
    public String getServerId() {
        return serverId;
    }

    /**
     * @param serverId
     */
    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
