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

public abstract class ResponseHandlerBase implements ResponseHandler{
    
    protected HttpMethod method;
    
    public ResponseHandlerBase(HttpMethod method) {
        this.method = method;
    }

    public abstract void process(HttpServletResponse response) throws IOException;
    
    public  void close() {
        method.releaseConnection();
    }

    public int getStatusCode() {
        return method.getStatusCode();
    }
    
    /**
     * Writes the entire stream from the method to the stream given from the
     * response.
     * 
     * @param response Response to send data to
     * @throws IOException
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
     * Will write all response headers stored in the method to the response.
     * One header connection is however omitted since we will only want the 
     * client to keep his connection to this proxy not to the server.
     * 
     * @param response The response that will have headers written to it
     */
    protected void setHeaders(HttpServletResponse response) {
        for (Header header : method.getResponseHeaders()) {
            String name = header.getName();
            boolean contentLength = name.compareToIgnoreCase("content-length") == 0;
            boolean connection = name.compareToIgnoreCase("connection") == 0;
            
            if (!contentLength && !connection) {
                response.addHeader(name, header.getValue());
            } 
        }
    }
}
