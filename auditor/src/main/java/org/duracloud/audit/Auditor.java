/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import org.duracloud.audit.error.AuditLogNotFoundException;
import org.duracloud.client.ContentStoreManager;

import java.util.List;

/**
 * The Auditor is responsible for collecting audit logs over all spaces for
 * content-related events.
 *
 * @author Andrew Woods
 *         Date: 3/17/12
 */
public interface Auditor {

    /**
     * This method initializes the Auditor by providing a handle to the content
     * store.
     *
     * @param storeMgr storage manager
     */
    public void initialize(ContentStoreManager storeMgr);

    /**
     * This method creates an initial audit log, and removes any existing audit
     * log for each space.
     *
     * @param async true if executing asynchronously
     */
    public void createInitialAuditLogs(boolean async);

    /**
     * This method returns the chronologically sorted list of audit logs for the
     * arg space.
     *
     * @param spaceId of the space over which auditing is requested
     * @return set of audit logs for the arg space
     */
    public List<String> getAuditLogs(String spaceId)
        throws AuditLogNotFoundException;

    /**
     * This method stops the work of the Auditor.
     */
    public void stop();
}
