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

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.logging.LogFactory;

/**
 * A basic implementation of the RequestHandler.
 * Includes a method to set the headers with.
 *
 * @author Anders Nyman
 */
public abstract class RequestHandlerBase implements RequestHandler {
    
    /** 
     * A set of headers that are not to be set in the request,
     * these headers are for example Connection.
     */
    private static Set bannedHeaders = new HashSet();

    /**
     * @see net.sf.j2ep.RequestHandler#process(javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public abstract HttpMethod process(HttpServletRequest request, String url) throws IOException;
    
    /**
     * Will write all request headers stored in the request to the method that
     * are not in the set of banned headers.
     * The Accept-Endocing header is also changed to allow compressed content
     * connection to the server even if the end client doesn't support that. 
     * A Via headers is created as well in compliance with the RFC.
     * 
     * @param method The HttpMethod used for this connection
     * @param request The incoming request
     */
    protected void setHeaders(HttpMethod method, HttpServletRequest request) {
        Enumeration headers = request.getHeaderNames();
        String connectionToken = request.getHeader("connection");
        
        while (headers.hasMoreElements()) {
            String name = (String) headers.nextElement();
            boolean isToken = (connectionToken != null && name.equalsIgnoreCase(connectionToken));
            
            if (!isToken && !bannedHeaders.contains(name.toLowerCase())) {
                Enumeration value = request.getHeaders(name);
                while (value.hasMoreElements()) {
                    method.addRequestHeader(name, (String) value.nextElement());
                } 
            } 
        } 
        
        String originalVia = request.getHeader("via");
        StringBuffer via = new StringBuffer("");
        if (originalVia != null) {
            via.append(originalVia).append(", ");
        }
        via.append(request.getProtocol()).append(" ").append(request.getServerName());
         
        method.setRequestHeader("via", via.toString());
        method.setRequestHeader("x-forwarded-for", request.getRemoteAddr());     
        method.setRequestHeader("x-forwarded-host", request.getServerName());
        try {
            method.setRequestHeader("x-forwarded-server", InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException e) {
            LogFactory.getLog(RequestHandlerBase.class).error("Couldn't write the x-forwarded-server header", e);
        }
        //TODO change this to gzip and make content decoding if the user cant handle it
        method.setRequestHeader("accept-encoding", "");

    }
    
    /**
     * Adds a headers to the list of banned headers.
     * 
     * @param header The header to add
     */
    public static void addBannedHeader(String header) {
        bannedHeaders.add(header);
    }

    /**
     * Adds all the headers in the input to the list 
     * of banned headers. The input string should be
     * comma separated e.g. "Server,Connection,Via"
     * 
     * This method is normally called by the factory that
     * is using this request handler.
     * 
     * @param headers The headers that are banned
     */
    public static void addBannedHeaders(String headers) {
        StringTokenizer tokenizer = new StringTokenizer(headers, ",");
        while (tokenizer.hasMoreTokens()) {
            bannedHeaders.add(tokenizer.nextToken().trim().toLowerCase());
        }
    }

}
