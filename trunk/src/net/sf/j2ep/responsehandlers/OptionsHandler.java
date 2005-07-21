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

package net.sf.j2ep.responsehandlers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import net.sf.j2ep.ResponseHandlerBase;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.methods.OptionsMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Handler for the OPTIONS method.
 *
 * @author Anders Nyman
 */
public class OptionsHandler extends ResponseHandlerBase {

    private static Set<String> allowedMethods = new HashSet<String>();
    private static Log log = LogFactory.getLog(OptionsHandler.class);
    
    private boolean useOwnAllow;
    private OptionsMethod method;

    public OptionsHandler(OptionsMethod method) {
        super(method);
        this.method = method;
        if (!method.hasBeenUsed()) {
            useOwnAllow = true;
        }
    }

    /**
     * Will check if we are to handle this request if so 
     * the http methods allowed by this proxy is returned.
     * If it is a request meant for the backing server its
     * allowed method will be returned.
     * 
     * @see net.sf.j2ep.ResponseHandler#process(javax.servlet.http.HttpServletResponse)
     */
    public void process(HttpServletResponse response) {
        setHeaders(response);
        response.setHeader("allow", processAllowHeader());
        response.setStatus(getStatusCode());

        if (useOwnAllow) {
            response.setHeader("content-length", "0");
        } else {
            Header contentLength = method.getResponseHeader("Content-Length");
            if (contentLength == null || contentLength.getValue().equals("0")) {
                response.setHeader("Content-Length", "0");
            } else {
                try {
                    sendStreamToClient(response);
                } catch (IOException e) {
                    log.error("Problem with writing response stream, solving by setting Content-Length=0", e);
                    response.setHeader("Content-Length", "0");
                }
            }
        }
    }

    /**
     * Will go through all the methods returned in the 
     * Allow header. Each method will be checked to see
     * that the method is allowed, if to it will be included
     * in the returned value.
     * 
     * @return String the allowed headers for this request
     */
    private String processAllowHeader() {
        StringBuffer allowToSend = new StringBuffer("");
        if (useOwnAllow) {
            for (String value : allowedMethods) {
                allowToSend.append(value).append(",");
            }
        } else {
            Enumeration<String> tokenizer = method.getAllowedMethods();
                while (tokenizer.hasMoreElements()) {
                String token = tokenizer.nextElement();
                if (allowedMethods.contains(token)) {
                    allowToSend.append(token).append(",");
                }
            }
        }
        return allowToSend.toString();
    }

    /**
     * @see net.sf.j2ep.ResponseHandler#getStatusCode()
     * 
     * Returned 200 if the request is targeted to the proxy
     * otherwise the normal status code is returned.
     */
    public int getStatusCode() {
        if (useOwnAllow) {
            return 200;
        } else {
            return super.getStatusCode();
        }
    }


    /**
     * Adds a method to the list of allowed methods.
     * 
     * @param methodName The method to add
     */
    public static void addAllowedMethod(String methodName) {
        allowedMethods.add(methodName);
    }

    /**
     * Adds all the methods in the input to the list 
     * of allowed methods. The input string should be
     * comma seperated e.g. "OPTIONS,GET,POST"
     * 
     * @param methodName
     */
    public static void addAllowedMethods(String methodName) {
        StringTokenizer tokenizer = new StringTokenizer(methodName, ",");
        while (tokenizer.hasMoreTokens()) {
            allowedMethods.add(tokenizer.nextToken().trim());
        }
    }
}
