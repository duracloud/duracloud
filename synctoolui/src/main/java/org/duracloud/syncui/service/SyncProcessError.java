/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import java.util.Date;

/**
 * 
 * @author Daniel Bernstein
 */
public class SyncProcessError {
    private Date time;

    private String detail; 
    private String descriptionMessageKey;

    private String suggestedResolutionMessgeKey;

    public Date getTime() {
        return time;
    }

    public String getDescriptionMessageKey() {
        return descriptionMessageKey;
    }

    public String getSuggestedResolutionMessgeKey() {
        return suggestedResolutionMessgeKey;
    }

    public SyncProcessError(String detail){
        this(detail, null,null);
    }

    public SyncProcessError(
        String detail, 
        String descriptionMessageKey, String suggestedResolutionMessgeKey) {
        super();
        this.detail = detail;
        this.time = new Date();
        this.descriptionMessageKey = descriptionMessageKey;
        this.suggestedResolutionMessgeKey = suggestedResolutionMessgeKey;
    }
    
    public String getDetail(){
        return this.detail;
    }

}
