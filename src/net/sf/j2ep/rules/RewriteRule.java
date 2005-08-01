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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RewriteRule extends BaseRule {
    
    private Pattern matchPattern;
    private String rewriteTo;
    private Pattern revertPattern;
    private boolean isRewriting;
    private boolean isReverting;
    
    /** 
     * Logging element supplied by commons-logging.
     */
    private static Log log;
    
    public RewriteRule() {
        isRewriting = false;
        log = LogFactory.getLog(RewriteRule.class);
    }

    public boolean matches(HttpServletRequest request) {
        String uri = getURI(request);       
        Matcher matcher = matchPattern.matcher(uri);
        return matcher.matches();
    }
    
    public String process(String uri) {
        Matcher matcher = matchPattern.matcher(uri);
        String replaced = matcher.replaceAll(rewriteTo);
        log.debug("Rewriting URI \n" + uri + " >> " + replaced);
        
        return replaced;
    }
    
    
    public void setFrom(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("From pattern cannot be null.");
        } else {
            matchPattern = Pattern.compile(regex);
        }
    }
    
    public void setTo(String to) {
        if (to == null) {
            throw new IllegalArgumentException("To string cannot be null.");
        } else {
            rewriteTo = to;
            isRewriting = true;
        }
    }
    
    public void setRevert(String regex) {
        if (regex == null) {
            throw new IllegalArgumentException("Revert pattern cannot be null.");
        } else {
            revertPattern = Pattern.compile(regex);
            isReverting = true;
        }
    }
    
    /**
     * Will build a URI but including the Query String. That means that it really
     * isn't a URI, but quite near.
     * 
     * @param httpRequest Request to get the URI and query string from
     * @return The URI for this request including the query string
     */
    private String getURI(HttpServletRequest httpRequest) {
        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI().substring(contextPath.length());
        if (httpRequest.getQueryString() != null) {
            uri += "?" + httpRequest.getQueryString();
        }
        return uri;
    }

}
