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

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.Server;

/**
 * A server implementation that have multiple domains
 * to choose from. When a request is received one domain
 * is chosen to handle the request. If the request is
 * linked to a session this server will make sure that it's
 * the domain that created the session that will process this 
 * request.
 *
 * @author Anders Nyman
 */
public class ClusterServer extends BaseServer {
    
    /** 
     * This threads server.
     */
    protected ThreadLocal currentServer = new ThreadLocal();
    
    /** 
     * The lists of servers in out cluster,
     */
    private HashMap servers;
    
    /** 
     * The current number of servers, only used at
     * when the servers are added to the hash map.
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
     * Will identify a session in the request and if there is
     * a session make sure that the server handling this
     * request is the same as the one that created the session.
     * 
     * @see net.sf.j2ep.Server#wrapRequest(javax.servlet.ServletRequest)
     */
    public HttpServletRequest wrapRequest(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        Cookie[] cookies = httpRequest.getCookies();
        for (int i=0; i < cookies.length; i++) {
            Cookie cookie = cookies[i];
            if (cookie.getName().equals("JSESSIONID")) {
                String value = cookie.getValue();
                String serverId = value.substring(value.indexOf(".")+1);
                currentServer.set(servers.get(serverId));
            }
        }
        
        return httpRequest;
    }

    /**
     * @see net.sf.j2ep.Server#getDomainName()
     */
    public String getDomainName() {
        return ((Server) currentServer.get()).getDomainName();
    }

    /**
     * @see net.sf.j2ep.Server#getDirectory()
     */
    public String getDirectory() {
        return ((Server) currentServer.get()).getDirectory();
    }
    
    /**
     * This will add a server to the hashMap. 
     * For each string we have with a server defined
     * a new BaseServer class will be created and added
     * to the hash map of servers.
     * 
     * @param serverString The string representing domainName and directory
     */
    public void setServer(String serverString) {
        BaseServer server = new BaseServer();
        int firstSlash = serverString.indexOf("/");
        
        String domainName = serverString;
        String directory;
        
        if (firstSlash != -1) {
            domainName = serverString.substring(0, firstSlash);
            if (firstSlash != serverString.length()) {
                directory = serverString.substring(firstSlash+1); 
                server.setDirectory(directory);
            }
        }
        server.setDomainName(domainName);
        servers.put("server"+numberOfServers++, server);
    }

}
