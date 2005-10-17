/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.commons.httpclient.methods.PostMethod;

/**
 * Handler for the POST method.
 *
 * @author Anders Nyman
 */
public class PostResponseHandler extends BasicResponseHandler {
    
    /**
     * Default constructor, will only call the super-constructor
     * for ResponseHandlerBase. 
     * 
     * @param method The method used for this response
     */
    public PostResponseHandler(PostMethod method) {
        super(method);
    }

}
