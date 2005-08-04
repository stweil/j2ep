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
