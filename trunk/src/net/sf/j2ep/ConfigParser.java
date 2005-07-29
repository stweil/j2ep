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
     * The resulting rule chain.
     */
    private RuleChain ruleChain;
    
    /** 
     * The servers mapped by id.
     */
    private HashMap<String, Server> serverIdMap;
    
    /** 
     * The servers mapped by their host name and directory.
     */
    private HashMap<String, Server> serverLocationMap;
    
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
            ruleChain = createRuleChain(data);
            serverIdMap = createServerIdMap(data);
            serverLocationMap = createServerLocationMap(serverIdMap);
            mapServersToRules(ruleChain, serverIdMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the parsed rule chain.
     *
     * @return The resulting RuleChain
     */
    public RuleChain getRuleChain() {
        return ruleChain;
    }
    
    /**
     * Returns the map of servers based on location.
     * 
     * @return The servers
     */
    public HashMap<String, Server> getServerLocationMap() {
        return serverLocationMap;
    }

    /**
     * Creates the rules.
     *
     * @return The rules all put into a rule chain
     */
    private RuleChain createRuleChain(File data) throws Exception{
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        
        // Construct rule chain
        digester.addObjectCreate("config/rules", RuleChain.class);

        // Construct rule
        digester.addObjectCreate("config/rules/rule", null, "className");
        digester.addSetProperties("config/rules/rule");   
        digester.addSetNext("config/rules/rule", "addRule", "net.sf.j2ep.Rule");

        // Construct composite rule
        digester.addObjectCreate("config/rules/composite-rule", "net.sf.j2ep.rules.CompositeRule");
        digester.addSetProperties("config/rules/composite-rule"); 
        // Construct rule for the composite rule
        digester.addObjectCreate("config/rules/composite-rule/rule", null, "className");
        digester.addSetProperties("config/rules/composite-rule/rule"); 
        digester.addSetNext("config/rules/composite-rule/rule", "addRule", "net.sf.j2ep.Rule");
        // Add rule to chain
        digester.addSetNext("config/rules/composite-rule", "addRule", "net.sf.j2ep.Rule");
        
        return (RuleChain) digester.parse(data);
    }
    
    /**
     * Creates the servers mapped by id.
     *
     * @return A hash map containing all the servers
     */
    @SuppressWarnings("unchecked")
    private HashMap<String, Server> createServerIdMap(File data) throws Exception{
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        
        // Construct server map
        digester.addObjectCreate("config/servers", HashMap.class);
        digester.addObjectCreate("config/servers/server", null, "className");
        digester.addSetProperties("config/servers/server"); 
        
        org.apache.commons.digester.Rule r = new CallMethodRule(1, "put", 2);
        digester.addRule("config/servers/server", r);        
        digester.addCallParam("config/servers/server", 0, "id");
        digester.addCallParam("config/servers/server", 1, true);
        
        // Construct server
        return (HashMap<String, Server>) digester.parse(data);
    }
    
    /**
     * Creates a map of the server based on their host name and directory.
     * The creation is done by traversing a already created map 
     * containing the servers mapped on their id.
     *
     * @param idMap The map we are creating this map from
     * @return A hash map containing all the servers
     */
    private HashMap<String, Server> createServerLocationMap(HashMap<String, Server> idMap) {
        HashMap<String, Server> map = new HashMap<String, Server>();
        
        for (Server server : idMap.values()) {
            String location = server.getHostAndPort() + server.getDirectory();
            map.put(location, server);
        }
        
        return map;
    }
    
    /**
     * Maps the servers to the rules using the rules specified serverId.
     * The reason that the servers isn't mapped directly on creation
     * of the rules is limitation in the Digester.
     * 
     * @param rules The rules
     * @param servers The servers
     */
    private void mapServersToRules(RuleChain rules, HashMap<String, Server> servers) {
        Iterator<Rule> itr = rules.getRuleIterator();
        while(itr.hasNext()) {
            log.debug("These are the rule to server mappings");
            Rule rule = itr.next();
            Server server = servers.get(rule.getServerId());
            if (server != null) {
                rule.setServer(server);
                log.debug("Rule " + rule + " using server " + server);
            }
        }
    }
}
