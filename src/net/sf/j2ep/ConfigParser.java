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
import java.util.Collection;
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
     * The resulting rule chain.
     */
    private RuleChain ruleChain;
    
    /** 
     * The servers mapped by id.
     */
    private HashMap serverIdMap;
    
    /** 
     * A collection of the servers.
     */
    private Collection serverCollection;
    
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
            serverCollection = createServerCollection(serverIdMap);
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
    public Collection getServerCollection() {
        return serverCollection;
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
        digester.addObjectCreate("config/rules/composite-rule", null, "className");
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
    private HashMap createServerIdMap(File data) throws Exception{
        Digester digester = new Digester();
        digester.setUseContextClassLoader(true);
        
        // Construct server map
        digester.addObjectCreate("config/servers", HashMap.class);
        org.apache.commons.digester.Rule addRule = new CallMethodRule(1, "put", 2);
        
        // Create servers
        digester.addObjectCreate("config/servers/server", null, "className");
        digester.addSetProperties("config/servers/server"); 
        // Add servers 
        digester.addRule("config/servers/server", addRule);        
        digester.addCallParam("config/servers/server", 0, "id");
        digester.addCallParam("config/servers/server", 1, true);
        
        // Create cluster server
        digester.addObjectCreate("config/servers/cluster-server", null, "className");
        digester.addSetProperties("config/servers/cluster-server"); 
        // Create the servers in this cluster
        digester.addObjectCreate("config/servers/cluster-server/server", null, "className");
        digester.addSetProperties("config/servers/cluster-server/server"); 
        digester.addSetNext("config/servers/cluster-server/server", "addServer", "net.sf.j2ep.Server");
        // Add cluster server
        digester.addRule("config/servers/cluster-server", addRule);        
        digester.addCallParam("config/servers/cluster-server", 0, "id");
        digester.addCallParam("config/servers/cluster-server", 1, true);
        
        // Construct server
        return (HashMap) digester.parse(data);
    }
    
    /**
     * Creates a collection with all the servers that want to 
     * do rewriting of absolute links.
     * 
     * @param map A map of all the servers
     * @return The collection with the rewriting servers
     */
    private Collection createServerCollection(HashMap map) {
        Collection col = new LinkedList();
        Iterator itr = map.values().iterator();
        
        while (itr.hasNext()) {
            Server server = (Server) itr.next();
            if (server.isRewriting()) {
                col.add(server);
            }
        }        
        return col;
    }
    
    /**
     * Maps the servers to the rules using the rules specified serverId.
     * The reason that the servers isn't mapped directly on creation
     * of the rules is limitation in the Digester.
     * 
     * @param rules The rules
     * @param servers The servers
     */
    private void mapServersToRules(RuleChain rules, HashMap servers) {
        Iterator itr = rules.getRuleIterator();
        while(itr.hasNext()) {
            log.debug("These are the rule to server mappings");
            Rule rule = (Rule) itr.next();
            Server server = (Server) servers.get(rule.getServerId());
            if (server != null) {
                rule.setServer(server);
                server.setRule(rule);
                log.debug("Rule " + rule + " using server " + server);
            }
        }
    }
}
