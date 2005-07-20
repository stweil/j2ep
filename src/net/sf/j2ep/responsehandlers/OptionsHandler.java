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

    public int getStatusCode() {
        if (useOwnAllow) {
            return 200;
        } else {
            return super.getStatusCode();
        }
    }

    public static void addAllowedMethod(String methodName) {
        allowedMethods.add(methodName);
    }

    public static void addAllowedMethods(String methodName) {
        StringTokenizer tokenizer = new StringTokenizer(methodName, ",");
        while (tokenizer.hasMoreTokens()) {
            allowedMethods.add(tokenizer.nextToken().trim());
        }
    }
}
