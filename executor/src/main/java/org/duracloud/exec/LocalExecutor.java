/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.serviceapi.ServicesManager;

/**
 * @author Andrew Woods
 *         Date: 4/5/12
 */
public interface LocalExecutor extends Executor {

    /**
     * Provides the Executor and Handlers access to storage and services.
     *
     * @param storeMgr storage manager
     * @param servicesMgr services manager
     */
    public void initialize(ContentStoreManager storeMgr,
                           ServicesManager servicesMgr);
}
