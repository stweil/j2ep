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

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.RequestHandler;
import net.sf.j2ep.ResponseHandler;
import net.sf.j2ep.Server;
import net.sf.j2ep.factories.MethodNotAllowedException;
import net.sf.j2ep.factories.RequestHandlerFactory;
import net.sf.j2ep.factories.ResponseHandlerFactory;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;

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
     * The host to connect to.
     */
    private String hostName;

    /**
     * Empty constructor.
     */
    public BaseServer() {
    }

    /**
     * Will start a connection using the host name specified and the URI being
     * sent in.
     * 
     * @see net.sf.j2ep.Server#connect(javax.servlet.http.HttpServletRequest,
     *      java.lang.String, org.apache.commons.httpclient.HttpClient)
     */
    public ResponseHandler connect(HttpServletRequest request, String uri,
            HttpClient httpClient) throws IOException, MethodNotAllowedException {

        String url = new StringBuffer(request.getScheme()).append("://")
                .append(getHostName()).append(uri).toString();

        RequestHandler requestHandler = RequestHandlerFactory
                .createRequestMethod(request.getMethod());

        HttpMethod method = requestHandler.process(request, url);

        /*
         * Why does method.validate() return true when the method has been
         * aborted? I mean, if validate returns true the API says that means
         * that the method is ready to be executed. TODO I don't like doing
         * type casting here, see above.
         */
        if (!((HttpMethodBase) method).isAborted()) {
            httpClient.executeMethod(method);
            
            if (method.getStatusCode() == 405) {
                Header allow = method.getResponseHeader("allow");
                String value = allow.getValue();
                throw new MethodNotAllowedException("405 error", ResponseHandlerFactory.processAllowHeader(value));
            }
        }

        return ResponseHandlerFactory.createResponseHandler(method);
    }

    /**
     * Returns the host name.
     * 
     * @return The host name
     */
    private String getHostName() {
        return hostName;
    }

    /**
     * Sets the host name to use for connections.
     * 
     * @param hostName The host name to set
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
