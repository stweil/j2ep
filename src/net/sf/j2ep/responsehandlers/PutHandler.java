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

import org.apache.commons.httpclient.methods.PutMethod;

public class PutHandler extends ResponseHandlerBase {
    
    public PutHandler(PutMethod method) {
        super(method);
    }

    @Override
    public void process(HttpServletResponse response) {
        // TODO Auto-generated method stub
        
    }

}
