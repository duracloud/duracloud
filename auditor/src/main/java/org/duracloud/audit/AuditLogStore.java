/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import org.duracloud.client.ContentStore;
import org.duracloud.storage.aop.ContentMessage;

import java.util.Iterator;
import java.util.List;

/**
 * This interface defines the contract provided by content stores that manage
 * audit logs.
 *
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public interface AuditLogStore {

    /**
     * This method ingests the underlying ContentStore
     *
     * @param contentStore of auditor
     */
    public void initialize(ContentStore contentStore);

    /**
     * This method returns the contentIds of all audit logs associated with
     * the arg spaceId.
     *
     * @param spaceId of space that was audited
     * @return iterator of contentIds
     */
    public Iterator<String> logs(String spaceId);

    /**
     * This method removes the log with the arg contentId
     *
     * @param logContentId of log to remove
     */
    public void removeLog(String logContentId);

    /**
     * This method writes the arg events to the audit log. The spaceId of the
     * audit log is determined by the arg events. All storage providers that
     * with the same spaceId will be written to the same audit log.
     * If not all arg events are on the same spaceId, an exception is thrown.
     *
     * @param events to be logged
     */
    public void write(List<ContentMessage> events);
}
