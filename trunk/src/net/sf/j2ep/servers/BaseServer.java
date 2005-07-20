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

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.ResponseHandler;
import net.sf.j2ep.ResponseHandlerFactory;
import net.sf.j2ep.Server;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A basic implementation of the Server interface using a single hostname to map
 * all connections.
 * 
 * @author Anders Nyman
 */
public class BaseServer implements Server {

    /**
     * The host to connect to.
     */
    private String hostName;
    
    private static Log log;
    
    public BaseServer() {
        log = LogFactory.getLog(BaseServer.class);
    }

    /**
     * @see net.sf.j2ep.Server#connect(javax.servlet.http.HttpServletRequest,
     *      java.lang.String, org.apache.commons.httpclient.HttpClient)
     * 
     * Will start a connection using the hostname specifed and the URI beeing
     * sent in.
     */
    public ResponseHandler connect(HttpServletRequest request, String uri,
            HttpClient httpClient) throws IOException{
        
        String url = new StringBuffer(request.getScheme()).append("://")
                .append(getHostName()).append(uri).toString();
        try {
            HttpMethod method = createMethod(request, url);
            try {
                /* 
                 * Why does method.validate() return true when the method has been
                 * aborted? I mean, if validate returns true the API says that means
                 * that the method is ready to be executed. 
                 * TODO I don't like doing typecasting here, see above.
                 */
                if (!((HttpMethodBase) method).isAborted()) {
                    httpClient.executeMethod(method);
                }
                
            } catch (HttpException e) {
                log.error("Problem when executing method", e);
            } catch (IOException e) {
                log.error("Problem when executing method, " + e.getClass() + " " + e.getMessage());
                throw e;
            } 

            return ResponseHandlerFactory.create(method);
            
        } catch (NoSuchMethodException e) {
            log.error("Could not handle this request with method " + request.getMethod(), e);
            return null;
        }

    }

    /**
     * Internal method to choose witch method to use. The method can be any
     * HttpMethod like (GET, POST, etc.). This method is quite important since
     * it will set all the headers for each connection (like Content-type when
     * using POST).
     * 
     * @param request The request to create method for
     * @param url The URL for the method to use
     * @return HttpMethod A method specifed using the input paramaters
     * @throws NoSuchMethodException 
     */
    private HttpMethod createMethod(HttpServletRequest request, String url) throws NoSuchMethodException {
        HttpMethod method = null;

        if (request.getMethod().equals("OPTIONS")) {
            method = new OptionsMethod(url);
            doOptions(request, method);
            
        } else if (request.getMethod().equals("GET")) {
            method = new GetMethod(url);
            doBasic(request, method);
            
        } else if (request.getMethod().equals("HEAD")) {
            method = new HeadMethod(url);
            doBasic(request, method);
            
        } else if (request.getMethod().equals("POST")) {
            method = new PostMethod(url);
            try {
                doEntityEnclosing(request, method);
            } catch (Exception e) {
                log.error("Problem with inputstream from POST defaulting to GET", e);
                method = new GetMethod(url);
                doBasic(request, method);
            }
            
        } else if (request.getMethod().equals("PUT")) {
            method = new PutMethod(url);
            try {
                doEntityEnclosing(request, method);
            } catch (Exception e) {
                log.error("Problem with inputstream from PUT defaulting to GET", e);
                method = new GetMethod(url);
                doBasic(request, method);
            }
            
        } else if (request.getMethod().equals("DELETE")) {
            method = new DeleteMethod(url);
            doBasic(request, method);
        }
        

        if (method == null) {
            throw new NoSuchMethodException();
        }
        return method;
    }

    private void doBasic(HttpServletRequest request, HttpMethod method) {
        setHeaders(method, request);
    }

    private void doEntityEnclosing(HttpServletRequest request, HttpMethod method)
            throws IOException {
        doBasic(request, method);
        InputStreamRequestEntity stream;
        stream = new InputStreamRequestEntity(request.getInputStream());
        ((EntityEnclosingMethod) method).setRequestEntity(stream);
        ((EntityEnclosingMethod) method).setRequestHeader("Content-type",
                request.getContentType());
    }

    private void doOptions(HttpServletRequest request, HttpMethod method) {
        doBasic(request, method);
        try {
            int max = request.getIntHeader("Max-Forwards");
            
            if (max == 0) {
                method.abort();
            } else {
                method.setRequestHeader("Max-Forwards", ((Integer) (max - 1))
                        .toString());
            }
            
        } catch (NumberFormatException e) {
            log.error("Incomming request had a Max-Forwards, but didn't contain integer. Sending along default Max-Forwards to server");
        }
    }
    
    /**
     * Will write almost all request headers stored in the request to the method.
     * Some headers shouldn't be send along to the server though, these are
     * connection along with its connection-tokens. The Accept-Endocing header
     * is also changed to allow compressed content connection to the server even
     * if the endclient doesn't support that. 
     * 
     * @param method The HttpMethod used for this connection
     * @param request The incomming request
     */
    private void setHeaders(HttpMethod method, HttpServletRequest request) {
        Enumeration headers = request.getHeaderNames();
        String connectionToken = request.getHeader("connection");
        
        while (headers.hasMoreElements()) {
            String name = (String) headers.nextElement();
            Enumeration value = request.getHeaders(name);
            while (value.hasMoreElements()) {
                String valueString = (String) value.nextElement();
                boolean equals = (connectionToken != null && name
                        .compareToIgnoreCase(connectionToken) == 0);
                if (!equals) {
                    method.addRequestHeader(name, valueString);
                }
            }
        }
        
        method.removeRequestHeader("connection");
        method.setRequestHeader("accept-encoding", "gzip, deflate");
    }

    /**
     * Returns the hostname.
     * 
     * @return String The hostname
     */
    private String getHostName() {
        return hostName;
    }

    /**
     * Sets the hostname to use for connections.
     * 
     * @param hostName
     *            The hostname to set
     */
    public void setHostName(String hostName) {
        if (hostName == null) {
            throw new IllegalArgumentException(
                    "The hostname string cannot be null.");
        } else {
            this.hostName = hostName;
        }
    }

}
