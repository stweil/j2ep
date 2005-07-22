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

package net.sf.j2ep.factories;

import java.util.HashMap;
import java.util.HashSet;

import net.sf.j2ep.RequestHandler;
import net.sf.j2ep.RequestHandlerBase;
import net.sf.j2ep.requesthandlers.BasicRequestHandler;
import net.sf.j2ep.requesthandlers.EntityEnclosingRequestHandler;
import net.sf.j2ep.requesthandlers.OptionsRequestHandler;

public class RequestHandlerFactory {
    
    private static HashMap<String, RequestHandler> requestHandlers;
    private static String allowedMethods = "OPTIONS,GET,HEAD,POST,PUT,DELETE";
    
    static {
        RequestHandlerBase.bannedHeaders = new HashSet<String>();
        RequestHandlerBase.bannedHeaders.add("connection");
        RequestHandlerBase.bannedHeaders.add("accept-encoding");
        
        requestHandlers = new HashMap<String, RequestHandler>();
        OptionsRequestHandler options = new OptionsRequestHandler();
        BasicRequestHandler basic = new BasicRequestHandler();
        EntityEnclosingRequestHandler entityEnclosing = new EntityEnclosingRequestHandler();
        
        requestHandlers.put("OPTIONS", options);
        requestHandlers.put("GET", basic);
        requestHandlers.put("HEAD", basic);
        requestHandlers.put("POST", entityEnclosing);
        requestHandlers.put("PUT", entityEnclosing);
        requestHandlers.put("DELETE", basic);
    }
    
    public static RequestHandler createRequestMethod(String method) throws MethodNotAllowedException{
        RequestHandler handler = requestHandlers.get(method.toUpperCase());
        if (handler == null) {
            throw new MethodNotAllowedException("The method " + method + " is not handled by this Factory.", allowedMethods);
        } else {
            return handler;
        }
    }
    
    public static String getAllowedMethods() {
        return allowedMethods;
    }
}