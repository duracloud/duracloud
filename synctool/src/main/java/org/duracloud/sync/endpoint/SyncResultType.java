/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

/**
 * 
 * @author Daniel Bernstein
 *
 */
public enum SyncResultType {
    ADDED ("A"),
    UPDATED ("U"),
    DELETED ("D"),
    UPDATE_IGNORED ("I"), 
    BACKED_UP ("B"),
    ALREADY_IN_SYNC ("S"),
    FAILED ("X");
    
    private String abbreviation;
    private SyncResultType(String abbreviation){
        this.abbreviation = abbreviation;
    }
    
    public String getAbbreviation() {
        return abbreviation;
    }
}
