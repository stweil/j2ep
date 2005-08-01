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
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Basic implementation of a Response Handler. This class
 * can write the headers and process the output stream.
 *
 * @author Anders Nyman
 */
public abstract class ResponseHandlerBase implements ResponseHandler{
    
    /** 
     * Method we are using for this request.
     */
    protected HttpMethod method;
    
    /**
     * Basic constructor only setting the method.
     * 
     * @param method The method we are using
     */
    public ResponseHandlerBase(HttpMethod method) {
        this.method = method;
    }

    /**
     * @see net.sf.j2ep.ResponseHandler#process(javax.servlet.http.HttpServletResponse)
     */
    public abstract void process(HttpServletResponse response) throws IOException;
    
    /**
     * Will release the connection for the method.
     * 
     * @see net.sf.j2ep.ResponseHandler#close()
     */
    public  void close() {
        method.releaseConnection();
    }

    /**
     * @see net.sf.j2ep.ResponseHandler#getStatusCode()
     */
    public int getStatusCode() {
        return method.getStatusCode();
    }
    
    /**
     * Writes the entire stream from the method to the response
     * stream.
     * 
     * @param response Response to send data to
     * @throws IOException An IOException is thrown when we are having problems with reading the streams
     */
    protected void sendStreamToClient(ServletResponse response) throws IOException {
        InputStream streamFromServer = method.getResponseBodyAsStream();
        OutputStream responseStream = response.getOutputStream();
        
        if (streamFromServer != null) {
            byte[] buffer = new byte[1024];
            int read = streamFromServer.read(buffer);
            while (read > 0) {
                responseStream.write(buffer, 0, read);
                read = streamFromServer.read(buffer);
            } 
            streamFromServer.close(); 
        }
        responseStream.flush();
        responseStream.close();
    }
    
    /**
     * Will write all response headers received in the method to the response.
     * One header, connection, is however omitted since we will only want the 
     * client to keep his connection to the proxy not to the backing server.
     * 
     * @param response The response that will have headers written to it
     */
    protected void setHeaders(HttpServletResponse response) {
        Header[] headers = method.getResponseHeaders();
        
        for (int i=0; i < headers.length; i++) {
            Header header = headers[i];
            String name = header.getName();
            boolean contentLength = name.compareToIgnoreCase("content-length") == 0;
            boolean connection = name.compareToIgnoreCase("connection") == 0;
            
            if (!contentLength && !connection) {
                response.addHeader(name, header.getValue());
            } 
        }
    }
}
