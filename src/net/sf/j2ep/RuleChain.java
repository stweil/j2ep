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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;


/**
 * A RuleChain is a list of rules
 * considered in order.  The first
 * rule to succeed stops the evaluation
 * of rules.
 *
 * @author Yoav Shapira
 */
public class RuleChain{
    
    /**
     * The list of rules to evaluate.
     */
    private List<Rule> rules;

    /**
     * Constructor.
     */
    public RuleChain() {
        rules = new ArrayList<Rule>();
    }

    /**
     * Returns the list of rules
     * to evaluate.
     *
     * @return List
     */
    protected List<Rule> getRules() {
        return rules;
    }

    /**
     * Returns an iterator over
     * the list of rules to evaluate.
     *
     * @return Iterator
     */
    protected Iterator<Rule> getRuleIterator() {
        return getRules().iterator();
    }

    /**
     * Adds a rule to evaluate.
     *
     * @param theRule The rule to add
     */
    public void addRule(Rule theRule) {
        if (theRule == null) {
            throw new IllegalArgumentException("The rule cannot be null.");
        } else {
            getRules().add(theRule);
        }
    }

    /**
     * Evaluates the given request to see if
     * any of the rules matches.  Returns the
     * the first matching rule.
     * Returns null if no rule matched the request.
     *
     * @param request The request
     * @return URL The first matching rule URL
     * @see Rule#matches(HttpServletRequest)
     */
    public Rule evaluate(HttpServletRequest request) {
        Iterator<Rule> iter = getRuleIterator();

        Rule currentRule = null;
        boolean currentMatches = false;

        while (iter.hasNext() && !currentMatches) {
            currentRule = iter.next();
            currentMatches = currentRule.matches(request);
        }

        return currentRule;
    }

    /**
     * Returns a String representation of this object.
     *
     * @return String
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[");
        buffer.append(getClass().getName());
        buffer.append(": ");

        Iterator iter = getRuleIterator();
        Rule currentRule = null;

        while (iter.hasNext()) {
            currentRule = (Rule) iter.next();
            buffer.append(currentRule);

            if (iter.hasNext()) {
                buffer.append(", ");
            }
        }

        buffer.append("]");

        return buffer.toString();
    }
}
