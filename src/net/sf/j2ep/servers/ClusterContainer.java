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
import javax.servlet.http.HttpServletResponse;

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
public class ClusterContainer extends ServerContainerBase implements ServerStatusListener {

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
     * Class that will check if our servers are online or offline.
     */
    private ServerStatusChecker statusChecker;
    
    /**
     * Basic constructor
     */
    public ClusterContainer() {
        servers = new HashMap();
        numberOfServers = 0;
        statusChecker = new ServerStatusChecker(this, 10*1000);
        log = LogFactory.getLog(ClusterContainer.class);
        statusChecker.start();
    }
    
    /**
     * Checks the request for any session. If there is a session created we
     * make sure that the server returned is the one the issued the session.
     * If no session is included in the request we will choose the next server
     * in a round-robin fashion.
     * 
     * @see net.sf.j2ep.ServerContainer#getServer(javax.servlet.http.HttpServletRequest)
     */
    public Server getServer(HttpServletRequest request) {
        String serverId = getServerIdFromCookie(request.getCookies());
        ClusteredServer server = (ClusteredServer) servers.get(serverId);
        if (server == null || !server.online()) {
            int start = currentServerNumber;
            int current = -1; 
            while (!server.online() && start != current) {
                current = (start + 1) % numberOfServers;
                server = (ClusteredServer) servers.get("server" + current); 
            }
            currentServerNumber = (currentServerNumber + 1) % numberOfServers;
        }
        
        if (server.online()) {
            log.debug("Using server" + server.serverId + " for this request"); 
        } else {
            log.error("All the servers in this cluster are offline. Using server \"server + server.serverId + \", will probably not work");
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
                if ( isSessionCookie(cookie.getName()) ) {
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
     * Checks if the supplied name of a cookie is known to be a 
     * session.
     * 
     * @param name The cookies name
     * @return true if this cookie is specifying a session
     */
    private boolean isSessionCookie(String name) {
        return name.equalsIgnoreCase("JSESSIONID")
                || name.equalsIgnoreCase("PHPSESSID")
                || name.equalsIgnoreCase("ASPSESSIONID")
                || name.equalsIgnoreCase("ASP.NET_SessionId");
    }
    
    /**
     * Sets the server to offline status.
     * Will only handle servers that are ClusteredServers
     * @see net.sf.j2ep.servers.ServerStatusListener#serverOffline(net.sf.j2ep.Server)
     */
    public void serverOffline(Server server) {
        if (server instanceof ClusteredServer) {
            ((ClusteredServer) server).setOnline(false);
        }
    }
    
    /**
     * Sets the server to online status.
     * Will only handle servers that are ClusteredServers
     * @see net.sf.j2ep.servers.ServerStatusListener#serverOnline(net.sf.j2ep.Server)
     */
    public void serverOnline(Server server) {
        if (server instanceof ClusteredServer) {
            ((ClusteredServer) server).setOnline(false);
        }
    }
    
    /**
     * Will create a new ClusteredServer and add it to the hash map.
     * 
     * @param domainName The domain name for the new server
     * @param directory The director for the new server.
     */
    public synchronized void addServer(String domainName, String directory) {
        if (domainName == null) {
            throw new IllegalArgumentException("The domainName cannot be null");
        }
        if (directory == null) {
            directory = "";
        }
        String id = "server" + numberOfServers;
        ClusteredServer server = new ClusteredServer(domainName, directory, id);
        servers.put(id, server);
        statusChecker.addServer(server);
        log.debug("Added server " + domainName + directory + " to the cluster on id server" + numberOfServers);
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
         * This servers id
         */
        private String serverId;
        
        /** 
         * The status of this server
         */
        private boolean online;
        
        /**
         * Basic constructor that sets the domain name and directory.
         * 
         * @param domainName The domain name
         * @param directory The directory
         */
        public ClusteredServer(String domainName, String directory, String serverId) {
            this.domainName = domainName;
            this.directory = directory;
            this.serverId = serverId;
            this.online = true;
        }

        /**
         * Will wrap the request so the tailing .something,
         * identifying the server, is removed from the request.
         * 
         * @see net.sf.j2ep.Server#preExecution(javax.servlet.http.HttpServletRequest)
         */
        public HttpServletRequest preExecution(HttpServletRequest request) {
            return new ClusterRequestWrapper(request);
        }
        
        /**
         * Will wrap the response so that sessions are rewritten to
         * remove the tailing .something that indicated which server
         * the session is linked to.
         * @see net.sf.j2ep.Server#postExecution(javax.servlet.http.HttpServletResponse)
         */
        public HttpServletResponse postExecution(HttpServletResponse response) {
            return new ClusterResponseWrapper(response, serverId);
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
         * Returns the online status of this server
         * @return true if the server is online, otherwise false
         */
        public boolean online() {
            return online;
        }
        
        /**
         * Marks if this server should be considered online or
         * offline.
         * @param online The status of the server
         */
        public void setOnline(boolean online) {
            this.online = online;
        }

        /**
         * @see net.sf.j2ep.Server#getRule()
         */
        public Rule getRule() {
            return ClusterContainer.this.getRule();
        }
    }
}
