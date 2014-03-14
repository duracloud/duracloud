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
     * Returns a list of matching log events 
     * @param account
     * @param spaceId 
     * @param storeId If null, returns log events for all storage providers
     * @return
     */
    public Iterator<AuditLogItem> getLogItems(String account,
                                              String spaceId,
                                              String storeId);
}
