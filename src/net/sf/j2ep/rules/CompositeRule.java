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

package net.sf.j2ep.rules;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import net.sf.j2ep.Rule;

/**
 * This rule consists of many other rules using the 
 * composite design pattern. The rule is matches if
 * all the included rules are matched.
 *
 * @author Anders Nyman
 */
public class CompositeRule extends BaseRule {

    /** 
     * The list of rules.
     */
    private LinkedList<Rule> rules;
    
    /**
     * Empty constructur, will only create the list of rules.
     */
    public CompositeRule() {
        rules = new LinkedList<Rule>();
    }
    
    /**
     * Used to add a rule to the list.
     * @param rule The rule to be added
     */
    public void addRule(Rule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("Rule to add cannot be null.");
        } else {
            rules.add(rule);
        }
    }
    
    /**
     * @see net.sf.j2ep.Rule#matches(javax.servlet.http.HttpServletRequest)
     * 
     * Iterate over all the rules in the list checking that they all match.
     * In the case where the composite rule consists of no rules this will return
     * true.
     */
    public boolean matches(HttpServletRequest request) {
        Iterator<Rule> itr = rules.iterator();
        boolean matches = true;
        while (itr.hasNext() && matches) {
            Rule rule = itr.next();
            matches = rule.matches(request);
        }
        
        return matches;
    }
    
    /**
     * @see net.sf.j2ep.Rule#process(java.lang.String)
     * 
     * Process all the rules in the list, allowing them all to change
     * the URI.
     */
    public String process(String uri) {
        String returnString = uri;
        for (Rule rule : rules) {
            returnString = rule.process(returnString);
        }
        return returnString;
    }
    
    /**
     * @see net.sf.j2ep.Rule#revert(java.lang.String)
     * 
     * Will do the oposite of process, that is revert all URIs to there default
     * value. This method will call all rules in the rule list and call revert on them.
     * Rules are called in a reversed order in comparisin with process.
     */
    public String revert(String uri) {
        String returnString = uri;
        ListIterator<Rule> itr = rules.listIterator();
        while (itr.hasPrevious()) {
            Rule rule = itr.previous();
            returnString = rule.revert(returnString);
        }
        return returnString;
    }

    /**
     * Returns a String representation of this object.
     *
     * @return String
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[");
        buffer.append("CompositeRule containing ");
        for(Rule rule: rules) {
            buffer.append("(");
            buffer.append(rule.getClass().getName());
            buffer.append(") ");
        }
        buffer.append(": ");

        buffer.append("]");

        return buffer.toString();
    }

}
