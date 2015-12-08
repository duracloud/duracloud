/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import java.util.List;

/**
 * A Task Provider performs tasks which are specific to a particular provider
 * implementation, and thus cannot be generalized as part of StorageProvider.
 *
 * @author: Bill Branan
 *          Date: May 20, 2010
 */
public interface TaskProvider {

    public List<String> getSupportedTasks();    

    public String performTask(String taskName, String taskParameters);
    /**
     * The storeId with which the TaskProvider is associated.
     * @return The storeId
     */
    public String getStoreId();

}
