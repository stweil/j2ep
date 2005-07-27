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


/**
 * A representation of the server. Its main use it to be able to open
 * a connection to the server sending back a executed method.
 * The server has to choose which type of method to invoke and should
 * support all method in the HTTP specification.
 *
 * @author Anders Nyman
 */
public interface Server {

    /**
     * Returns the host name and port for this server.
     * @return Host name and port
     */
    String getHostAndPort();
    
    /**
     * Returns the full URL to this server including
     * directories on the server
     * @return The full server path
     */
    String getName();
}
