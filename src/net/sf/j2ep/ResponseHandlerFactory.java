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

import net.sf.j2ep.responsehandlers.*;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.*;

public class ResponseHandlerFactory {
    
    public static ResponseHandler create(HttpMethod method) {
        ResponseHandler handler = null;

        if (method.getName().equals("OPTIONS")) {
            handler = new OptionsResponseHandler((OptionsMethod) method);
        } else if (method.getName().equals("GET")) {
            handler = new GetResponseHandler((GetMethod) method);
        } else if (method.getName().equals("HEAD")) {
            handler = new HeadResponseHandler((HeadMethod) method);
        } else if (method.getName().equals("POST")) {
            handler = new PostResponseHandler((PostMethod) method);
        } else if (method.getName().equals("PUT")) {
            handler = new PutResponseHandler((PutMethod) method);
        } else if (method.getName().equals("DELETE")) {
            handler = new DeleteResponseHandler((DeleteMethod) method);
        }

        return handler;
    }

}
