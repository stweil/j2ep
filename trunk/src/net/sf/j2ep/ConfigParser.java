/*
 * Copyright 2000,2004 The Apache Software Foundation.
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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The config parser uses Digester
 * to parse the config file. A rule chain
 * with links to the servers will be constructed.
 * 
 * Based on the work by Yoav Shapira for the
 * balancer webapp distributed with Tomcat.
 *
 * @author Anders Nyman, Yoav Shapira
 */
public class ConfigParser {
    
    /**
     * The resulting server chain.
     */
    private ServerChain serverChain;
    
    /** 
     * A logging instance supplied by commons-logging.
     */
    private static Log log;

    /**
     * Standard constructor only specifying the input file.
     * The constructor will parse the config and build a 
     * corresponding rule chain with the server mappings included.
     * 
     * @param data The config file containing the XML data structure.
     */
    public ConfigParser(File data) {
        log = LogFactory.getLog("org.apache.webapp.reverseproxy");
        try {
            LinkedList servers = createServerList(data);
            HashMap ruleIdMap = createRuleIdMap(data);
            mapServersToRules(servers, ruleIdMap);
            serverChain = new ServerChain(servers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the parsed server chain.
     *
     * @return The resulting ServerChain
     */
    public ServerChain getServerChain() {
        return serverChain;
    }

    /**
     * Creates the rules.
     *
     * @return The rules all put into a rule chain
     */
    private LinkedList createServerList(File data) throws Exception{
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        
        // Construct server list
        digester.addObjectCreate("config/servers", LinkedList.class);
        
        // Create servers
        digester.addObjectCreate("config/servers/server", null, "className");
        digester.addSetProperties("config/servers/server"); 
        // Add server to list
        digester.addSetNext("config/servers/server", "add");
        
        // Create cluster server
        digester.addObjectCreate("config/servers/cluster-server", null, "className");
        digester.addSetProperties("config/servers/cluster-server"); 
        // Create the servers in this cluster
        digester.addObjectCreate("config/servers/cluster-server/server", null, "className");
        digester.addSetProperties("config/servers/cluster-server/server"); 
        digester.addSetNext("config/servers/cluster-server/server", "addServer", "net.sf.j2ep.Server");
        // Add cluster to list
        digester.addSetNext("config/servers/server", "add");
        
        return (LinkedList) digester.parse(data);
    }
    
    /**
     * Creates the rules mapped by id.
     *
     * @return A hash map containing all the rules
     */
    private HashMap createRuleIdMap(File data) throws Exception{
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        
        // Construct rule map
        digester.addObjectCreate("config/rules", HashMap.class);
        org.apache.commons.digester.Rule addRule = new CallMethodRule(1, "put", 2);
        
        // Create rule
        digester.addObjectCreate("config/rules/rule", null, "className");
        digester.addSetProperties("config/rules/rule"); 
        // Add rule 
        digester.addRule("config/rules/rule", addRule);        
        digester.addCallParam("config/rules/rule", 0, "id");
        digester.addCallParam("config/rules/rule", 1, true);
        
        // Create composite rule
        digester.addObjectCreate("config/rules/composite-rule", null, "className");
        digester.addSetProperties("config/rules/composite-rule"); 
        // Create the rules in this composite rule
        digester.addObjectCreate("config/rules/composite-rule/rule", null, "className");
        digester.addSetProperties("config/rules/composite-rule/rule"); 
        digester.addSetNext("config/rules/composite-rule/rule", "addRule", "net.sf.j2ep.Rule");
        // Add cluster server
        digester.addRule("config/rules/composite-rule", addRule);        
        digester.addCallParam("config/rules/composite-rule", 0, "id");
        digester.addCallParam("config/rules/composite-rule", 1, true);
        
        // Construct server
        return (HashMap) digester.parse(data);
    }
    
    /**
     * Maps the rules to the servers using the servers specified ruleId.
     * The reason that the rules aren't mapped directly on creation
     * of the server is limitation in the Digester.
     * 
     * @param rules The rules
     * @param servers The servers
     */
    private void mapServersToRules(LinkedList servers, HashMap rules) {
        log.debug("These are the server to rule mappings");
        Iterator itr = servers.iterator();
        while(itr.hasNext()) {
            Server server = (Server) itr.next();
            Rule rule = (Rule) rules.get(server.getRuleId());
            if (server != null) {
                server.setRule(rule);
                log.debug("Rule " + rule + " using server " + server);
            }
        }
    }
}
