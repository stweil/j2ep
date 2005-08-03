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

import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.Server;

/**
 * A server implementation that have multiple domains to choose from. When a
 * request is received one domain is chosen to handle the request. If the
 * request is linked to a session this server will make sure that it's the
 * domain that created the session that will process this request.
 * 
 * @author Anders Nyman
 */
public class ClusterServer extends BaseServer {

    /**
     * This threads server.
     */
    private ThreadLocal currentServer = new ThreadLocal() {
        /**
         * The currentServer we are using. Since many threads might access this
         * field at the same time it has to be volatile.
         */
        private volatile int currentServerNumber;
        
        protected synchronized Object initialValue() {
            currentServerNumber = (currentServerNumber + 1) % numberOfServers;
            return servers.get("server" + currentServerNumber);
        }
    };

    /** 
     * The lists of servers in out cluster,
     */
    protected HashMap servers;
    
    /**
     * The current number of servers, only used at when the servers are added to
     * the hash map. It is assumed that this variable is only modified in a
     * single threaded environment.
     */
    private int numberOfServers;


    /**
     * Basic constructor
     */
    public ClusterServer() {
        servers = new HashMap();
        numberOfServers = 0;
    }
    
    /**
     * Will identify a session in the request and if there is a session make
     * sure that the server handling this request is the same as the one that
     * created the session.
     * 
     * @see net.sf.j2ep.Server#wrapRequest(javax.servlet.http.HttpServletRequest)
     */
    public HttpServletRequest wrapRequest(HttpServletRequest request) {
        boolean needsWrappedRequest = false;
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (int i=0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals("JSESSIONID")) {
                    String value = cookie.getValue();
                    String serverId = value.substring(value.indexOf(".")+1);
                    if (serverId.startsWith("server")) {
                        Server server = (Server) servers.get(serverId);
                        if (server != null) {
                            needsWrappedRequest = true;
                            currentServer.set(server);
                        }
                    }
                }
            } 
        }
        
        if (needsWrappedRequest) {
            return new SessionRewritingRequestWrapper(request);
        } else {
            return request;
        }
    }

    /**
     * @see net.sf.j2ep.Server#getDomainName()
     */
    public String getDomainName() {
        System.out.println(((Server) currentServer.get()).getDomainName());
        return ((Server) currentServer.get()).getDomainName();
    }

    /**
     * @see net.sf.j2ep.Server#getDirectory()
     */
    public String getDirectory() {
        System.out.println(((Server) currentServer.get()).getDirectory());
        return ((Server) currentServer.get()).getDirectory();
    }
    
    /**
     * This will add a server to the hashMap. 
     * 
     * @param server The server to add
     */
    public synchronized void addServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("Server to add cannot be null.");
        } else {
            servers.put("server" + numberOfServers, server);
            System.out.println("server" + numberOfServers);
            numberOfServers++;
        }
    }

}
