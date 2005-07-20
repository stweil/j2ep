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

import javax.servlet.http.HttpServletResponse;

import net.sf.j2ep.ResponseHandlerBase;

import org.apache.commons.httpclient.methods.HeadMethod;

public class HeadHandler extends ResponseHandlerBase {
    
    public HeadHandler(HeadMethod method) {
        super(method);
    }

    @Override
    public void process(HttpServletResponse response) {
        setHeaders(response);
        response.setStatus(getStatusCode());
    }

}
