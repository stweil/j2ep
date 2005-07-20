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

package net.sf.j2ep.test;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletInputStream;

public class MethodWrappingRequest extends MockHttpServletRequest {

    private String uri;
    private String queryString;
    private String method;
    private HashMap<String, String> headers;
    
    public MethodWrappingRequest(String method, String uri, String queryString) {
        this.uri = uri;
        this.queryString = queryString;
        this.method = method;
        headers = new HashMap<String, String>();
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getRequestURI() {
        return uri;
    }
    
    public StringBuffer getRequestURL() {
        return new StringBuffer("http://mockrequest").append(uri);
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public String getServletPath() {
        return uri;
    }
    
    public String getScheme() {
        return "http";
    }
    
    public ServletInputStream getInputStream() {
        return new ServletInputStream() {
            public int readLine(byte[] b, int off, int len) throws IOException {
                return -1;
            }
            public int read() throws IOException {
                return -1;
            }
        };
    }
    
    public Enumeration getHeaderNames() {
        StringBuffer headerString = new StringBuffer("");
        for (String name : headers.keySet()) {
            headerString.append(name).append(";");
        }
        return new StringTokenizer(headerString.toString(), ";");
    }
    
    public Enumeration getHeaders(String name) {
        return new StringTokenizer(headers.get(name), ";");
    }
    
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
    
    public int getIntHeader(String name) throws NumberFormatException {
        String value = headers.get(name);
        if (value != null) {
            return Integer.parseInt(value);
        } else {
            return -1;
        }
        
    }
}
