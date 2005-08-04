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

package net.sf.j2ep;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * A ServerChain is a list of server
 * considered in order.  The first
 * server with a rule that successfully matches 
 * tops the evaluation of servers
 * 
 * This is only a slightly modified version of the 
 * RuleChain used with the balancer webapp shipped 
 * with tomcat.
 *
 * @author Anders Nyman, Yoav Shapira
 */
public class ServerChain{
    
    /**
     * The list of servers to evaluate.
     */
    private List serverContainers;

    /**
     * Constructor.
     */
    public ServerChain(List serverContainers) {
        this.serverContainers = serverContainers;
    }

    /**
     * Returns the list of servers
     * to evaluate.
     *
     * @return The servers
     */
    protected List getServers() {
        return serverContainers;
    }

    /**
     * Returns an iterator over
     * the list of servers to evaluate.
     *
     * @return The iterator
     */
    public Iterator getServerIterator() {
        return getServers().iterator();
    }

    /**
     * Adds a server to evaluate.
     *
     * @param theServer The server to add
     */
    public void addServer(Server theServer) {
        if (theServer == null) {
            throw new IllegalArgumentException("The rule cannot be null.");
        } else {
            getServers().add(theServer);
        }
    }

    /**
     * Evaluates the given request to see if
     * any of the rules matches.  Returns the
     * the server linked to the first matching rule.
     *
     * @param request The request
     * @return The first matching server, null if no rule matched the request
     * @see Rule#matches(HttpServletRequest)
     */
    public Server evaluate(HttpServletRequest request) {
        Iterator itr = getServerIterator();

        ServerContainer currentContainer = null;
        boolean currentMatches = false;

        while (itr.hasNext() && !currentMatches) {
            currentContainer = (ServerContainer) itr.next();
            currentMatches = currentContainer.getRule().matches(request);
        }
        
        if (currentMatches) {
            return currentContainer.getServer(request);
        } else {
            return null;
        }
        
    }

    /**
     * Returns a String representation of this object.
     *
     * @return A string representation
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[");
        buffer.append(getClass().getName());
        buffer.append(": ");

        Iterator iter = getServerIterator();
        Server currentServer = null;

        while (iter.hasNext()) {
            currentServer = (Server) iter.next();
            buffer.append(currentServer);

            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append("]");

        return buffer.toString();
    }
}
