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

import javax.servlet.http.HttpServletRequest;

/**
 * Regex match
 *
 * @author Tim Funk
 */
public class RegexRule extends BaseRule {

    /**
     * The regex
     */
    private String regex;

    /**
     * Set the regex to match against the URI.
     *
     * @param regex The new regex
     */
    public void setRegex(String regex) {
        if (regex == null)
            throw new IllegalArgumentException(
                "The regex string cannot be null.");

        this.regex = regex;
    }

    /**
     * Returns the regex that
     * this rule will match on.
     *
     * @return The regex string
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Will see if the regex for the incoming URI is the same
     * as this rule is set to match on. Uses the String.matches().
     *
     * @see net.sf.j2ep.Rule#matches(javax.servlet.http.HttpServletRequest)
     */
    public boolean matches(HttpServletRequest request) {
        String uri = request.getContextPath() + request.getServletPath();
        return (uri.matches(regex));
    }

}
