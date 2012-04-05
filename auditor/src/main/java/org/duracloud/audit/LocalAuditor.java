/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import org.duracloud.client.ContentStoreManager;

/**
 * The Auditor is responsible for collecting audit logs over all spaces for
 * content-related events.
 *
 * @author Andrew Woods
 *         Date: 3/17/12
 */
public interface LocalAuditor extends Auditor {

    /**
     * This method initializes the Auditor by providing a handle to the content
     * store.
     *
     * @param storeMgr storage manager
     */
    public void initialize(ContentStoreManager storeMgr);

}
