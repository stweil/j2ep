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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;

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

    public BaseServer() {
    }

    /**
     * @see net.sf.j2ep.Server#connect(javax.servlet.http.HttpServletRequest,
     *      java.lang.String, org.apache.commons.httpclient.HttpClient)
     * 
     * Will start a connection using the hostname specifed and the URI beeing
     * sent in.
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
         * typecasting here, see above.
         */
        if (!((HttpMethodBase) method).isAborted()) {
            httpClient.executeMethod(method);
        }

        return ResponseHandlerFactory.createResponseHandler(method);
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
     * @param hostName The hostname to set
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
