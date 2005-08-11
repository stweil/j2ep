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

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

/**
 * A simple rule that checks the hour. If the hour
 * is in the specified range we will allow the 
 * request.
 *
 * @author Anders Nyman
 */
public class TimeRule extends BaseRule {
    
    /** 
     * The start hour
     */
    private int startTime;
    
    /** 
     * The end hour
     */
    private int endTime;

    /**
     * @see net.sf.j2ep.Rule#matches(javax.servlet.http.HttpServletRequest)
     */
    public boolean matches(HttpServletRequest request) {
        int currentTime = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return (currentTime >= startTime && currentTime <= endTime);        
    }
    
    /**
     * Sets the start hour that requests will be allowed.
     * 
     * @param time The hour
     */
    public void setStartTime(String time) {
        if (time == null) {
            throw new IllegalArgumentException("The start time cannot be null");
        } else {
            startTime = Integer.parseInt(time)%24;
        }
    }
    
    /**
     * Sets the end hour that request will be allowed.
     * 
     * @param time The hour
     */
    public void setEndTime(String time) {
        if (time == null) {
            throw new IllegalArgumentException("The end time cannot be null");
        } else {
            endTime = Integer.parseInt(time)%24;
        }
    }

}
