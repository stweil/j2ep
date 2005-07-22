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

package net.sf.j2ep;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.factories.MethodNotAllowedException;

import org.apache.commons.httpclient.HttpClient;

/**
 * A representation of the server. Its main use it to be able to open
 * a connection to the server sending back a executed method.
 * The server has to choose which type of method to invoke and should
 * support all method in the HTTP specification.
 *
 * @author Anders Nyman
 */
public interface Server {

    /**
     * Will open a connection to the server using the specified URI.
     * 
     * @param request The request to use, is needed to get the connection type (GET, POST, etc.) and also to get any InputStream
     * @param uri The URI for this connection
     * @param httpClient A connection will be made using this HttpClient
     * @return ResponseHandler A class that can process any response that should be send to the client. 
     * @throws IOException Throws an exception when there is problem with connection to the server or with any input, output, etc.
     * @throws MethodNotAllowedException Thrown when a method being sent to the server isn't able to be handled.
     */
    ResponseHandler connect(HttpServletRequest request, String uri, HttpClient httpClient) throws IOException, MethodNotAllowedException;
    
}
