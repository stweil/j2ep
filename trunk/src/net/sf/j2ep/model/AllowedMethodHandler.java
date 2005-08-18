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

package net.sf.j2ep.model;

import java.util.HashSet;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpMethod;

public class AllowedMethodHandler {
    
    /** 
     * The methods handled by this factory.
     */
    private static String allowString = "OPTIONS,GET,HEAD,POST,PUT,DELETE,TRACE";
    
    private static HashSet allowedMethods;
    
    /**
     * Will go through all the methods sent in
     * checking to see that the method is allowed.
     * If it's allowed it will be included
     * in the returned value.
     * 
     * @param allowSent The header returned by the server
     * @return The allowed headers for this request
     */
    public static String processAllowHeader(String allowSent) {
        StringBuffer allowToSend = new StringBuffer("");
        StringTokenizer tokenizer = new StringTokenizer(allowSent, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim().toUpperCase();
            if (allowedMethods.contains(token)) {
                allowToSend.append(token).append(",");
            }
        }

        return allowToSend.toString();
    }
    
    /**
     * Returns the allow methods for this factory.
     * 
     * @return Allowed methods
     */ 
    public static String getAllowHeader() {
        return allowString;
    }
    
    public static boolean methodAllowed(String method) {
        return allowedMethods.contains(method.toUpperCase());
    }
    
    public static boolean methodAllowed(HttpMethod method) {
        return methodAllowed(method.getName());
    }
    
    public synchronized static void setAllowedMethods(String allowed) {
        allowedMethods = new HashSet();
        allowString = allowed;
        StringTokenizer tokenizer = new StringTokenizer(allowed, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim().toUpperCase();
            allowedMethods.add(token);
        }
    }

}
