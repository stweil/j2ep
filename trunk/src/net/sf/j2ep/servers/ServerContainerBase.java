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

package net.sf.j2ep.servers;

import net.sf.j2ep.Rule;
import net.sf.j2ep.ServerContainer;

public abstract class ServerContainerBase implements ServerContainer {

    /** 
     * The id for the rule we are mapped to.
     */
    private String ruleId;
    
    /** 
     * The rule we are mapped to.
     */
    private Rule rule;

    /**
     * @see net.sf.j2ep.ServerContainer#getRuleId()
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * @see net.sf.j2ep.ServerContainer#getRule()
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * @see net.sf.j2ep.ServerContainer#setRule(net.sf.j2ep.Rule)
     */
    public void setRule(Rule rule) {
        this.rule = rule;
    }
    
    /**
     * Sets the id for the rule this container is using.
     * 
     * @param ruleId The rule id
     */
    public void setRuleId(String ruleId) {
        if (ruleId == null) {
            throw new IllegalArgumentException("The rule id cannot be null");
        } else {
            this.ruleId = ruleId;
        }
    }

}
