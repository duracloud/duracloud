/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.duracloud.error.NotFoundException;

/**
 * This interface defines the contract for reading and writing audit logs.
 *
 * @author Daniel Bernstein
 */
public interface AuditLogStore {

    /**
     * This method writes the logItem to the audit log.
     * @param account
     * @param storeId
     * @param spaceId
     * @param contentId
     * @param contentMd5
     * @param mimetype
     * @param contentSize
     * @param user
     * @param action
     * @param properties
     * @param spaceAcls
     * @param timestamp
     * @throws AuditLogWriteFailedException
     */
    public void write(String account,
                      String storeId,
                      String spaceId,
                      String contentId,
                      String contentMd5,
                      String mimetype,
                      String contentSize,
                      String user,
                      String action,
                      String properties,
                      String spaceAcls,
                      String sourceSpaceId,
                      String sourceContentId,
                      Date timestamp) throws AuditLogWriteFailedException;
    
    
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

    /**
     * Retrieves log history for a content item in chronological order
     * @param account
     * @param storeId
     * @param spaceId
     * @param contentId
     * @return
     */
    public AuditLogItem getLatestLogItem(String account,
                                              String storeId,
                                              String spaceId,
                                              String contentId) throws NotFoundException;
    
    /**
     * Adds the following properties to the specified item.
     * @param item
     * @param properties
     * @throws AuditLogWriteFailedException
     */
    public void updateProperties(AuditLogItem item, String properties) throws AuditLogWriteFailedException;
    
}
