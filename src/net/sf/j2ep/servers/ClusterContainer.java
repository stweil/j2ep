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
import java.util.Iterator;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.Rule;
import net.sf.j2ep.Server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A ServerContainer implementation that have multiple domains to choose from.
 * When a request is received one server is chosen to handle the request. If the
 * request is linked to a session this server will make sure that it's the
 * domain that created the session that will process this request.
 * 
 * @author Anders Nyman
 */
public class ClusterContainer extends ServerContainerBase {

    /** 
     * Logging element supplied by commons-logging.
     */
    private static Log log;
    
    /** 
     * The lists of servers in our cluster,
     */
    protected HashMap servers;
    
    /**
     * The current number of servers, only used at when the servers are added to
     * the hash map. It is assumed that this variable is only modified in a
     * single threaded environment.
     */
    private int numberOfServers;
    
    /**
     * The currentServer we are using.
     */
    private int currentServerNumber;

    /**
     * Basic constructor
     */
    public ClusterContainer() {
        servers = new HashMap();
        numberOfServers = 0;
        log = LogFactory.getLog(ClusterContainer.class);
    }
    
    /**
     * Checks the request for any session. If there is a session created we
     * make sure that the server returned is the one the issued the sesssion.
     * If no session is included in the request we will choose the next server
     * in a round-robin fashion.
     * 
     * @see net.sf.j2ep.ServerContainer#getServer(javax.servlet.http.HttpServletRequest)
     */
    public Server getServer(HttpServletRequest request) {
        String serverId = getServerIdFromCookie(request.getCookies());
        Server server = (Server) servers.get(serverId);
        if (server == null) {
            currentServerNumber = (currentServerNumber + 1) % numberOfServers;
            log.debug("Server: " + currentServerNumber + " mapped for this thread");
            server = (Server) servers.get("server" + currentServerNumber);
        }
        return server;
    }
    
    /**
     * @see net.sf.j2ep.ServerContainer#getServerMapped(java.lang.String)
     */
    public Server getServerMapped(String location) {
        Iterator itr = servers.values().iterator();
        Server match = null;

        while (itr.hasNext() && match == null) {
            Server server = (Server) itr.next();
            String fullPath = server.getDomainName() + server.getDirectory() + "/";
            if (location.startsWith(fullPath)) {
                match = server;
            }
        }
        
        return match;
    }

    /**
     * Locates any specification of which server that issued a
     * session. If there is no session or the session isn't mapped
     * to a specific server null is returned.
     * 
     * @param cookies The cookies so look for a session in
     * @return the server's ID or null if no server is found
     */
    private String getServerIdFromCookie(Cookie[] cookies) {
        String serverId = null;
        if (cookies != null) {
            for (int i=0; i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                if (cookie.getName().equals("JSESSIONID")) {
                    String value = cookie.getValue();
                    String id = value.substring(value.indexOf(".")+1);
                    if (id.startsWith("server")) {
                        serverId = id;
                    }
                }
            } 
        }
        return serverId;
    }
    
    /**
     * Will create a new ClusteredServer and add it to the hash map.
     * 
     * @param domainName The domain name for the new server
     * @param directory The director for the new server.
     */
    public synchronized void addServer(String domainName, String directory) {
        ClusteredServer server = new ClusteredServer(domainName, directory);
        servers.put("server" + numberOfServers, server);
        numberOfServers++;
    }
    
    /**
     * A server in the cluster. Will have access to the encapsulating Cluster
     * so that we can use its methods to get the rule and such.
     *
     * @author Anders Nyman
     */
    private class ClusteredServer implements Server {
        
        /** 
         * The domain name mapping
         */
        private String domainName;
        
        /** 
         * The directory mapping
         */
        private String directory;
        
        /**
         * Basic constructor that sets the domain name and directory.
         * 
         * @param domainName The domain name
         * @param directory The directory
         */
        public ClusteredServer(String domainName, String directory) {
            this.domainName = domainName;
            this.directory = directory;
        }

        public HttpServletRequest wrapRequest(HttpServletRequest request) {
            //TODO fixa hantering här
            return request;
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
            return ClusterContainer.this.getRule();
        }
    }

}

