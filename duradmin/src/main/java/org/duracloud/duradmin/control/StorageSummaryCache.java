/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import java.util.List;

/**
 * @author Daniel Bernstein
 * 
 */
public interface StorageSummaryCache {
 
    public void init();
    
    public List<StorageSummary> getSummaries(String storeId, String spaceId);
}
