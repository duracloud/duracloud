/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.queue.task;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a basic unit of work. In essence it describes "what" is to be
 * done. It knows nothing of the "how".
 * 
 * @author Daniel Bernstein
 * 
 */
public class Task {

    public static final String KEY_TYPE = "type";
    public enum Type {
        BIT, BIT_REPORT, BIT_ERROR,  DUP, AUDIT, STORAGE_STATS, NOOP;
    }

    private Type type;
    private Map<String, String> properties = new HashMap<>();
    private Integer visibilityTimeout;
    
    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    public String removeProperty(String key) {
        return properties.remove(key);
    }

    public Integer getVisibilityTimeout() {
        return visibilityTimeout;
    }

    public void setVisibilityTimeout(Integer visibilityTimeout) {
        this.visibilityTimeout = visibilityTimeout;
    }
    
    /**
     * The number of completed attempts to process this task.
     * @return
     */
    public int getAttempts() {
        String attempts = this.properties.get("attempts");
        if(attempts == null){
            attempts = "0";
        }
        
        return Integer.parseInt(attempts);
    }

    /**
     * Increments the attempts property. This method should only be called by
     * TaskQueue implementations.
     */
    public void incrementAttempts() {
        int attempts = getAttempts()+1;
        this.properties.put("attempts", attempts+"");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }



}
