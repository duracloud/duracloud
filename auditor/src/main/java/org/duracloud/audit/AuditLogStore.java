/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import java.util.Iterator;

import org.duracloud.audit.dynamodb.AuditLogItem;

/**
 * This interface defines the contract for reading and writing audit logs.
 *
 * @author Daniel Bernstein
 */
public interface AuditLogStore {

    /**
     * This method writes the logItem to the audit log.
     * @param logItem to be logged
     * @throws AuditLogWriteFailedException
     */
    public void write(AuditLogItem logItem) throws AuditLogWriteFailedException;
    
    /**
     * Returns a list of matching log events for the specified space across providers
     * @param account
     * @param spaceId 
     * @return
     */
    public Iterator<AuditLogItem> getLogItems(String account,
                                              String spaceId);
    
    /**
     * Retrieves log history for a content item.
     * @param account
     * @param storeId
     * @param spaceId
     * @param contentId
     * @return
     */
    public Iterator<AuditLogItem> getLogItems(String account,
                                              String storeId,
                                              String spaceId,
                                              String contentId);

}
