/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.duradmin.config.DuradminConfig;

public class ContentStoreManagerFactoryImpl
        implements ContentStoreManagerFactory {

    public ContentStoreManager create() throws Exception {
        return new ContentStoreManagerImpl(DuradminConfig.getDuraStoreHost(),
                                           DuradminConfig.getDuraStorePort(),
                                           DuradminConfig.getDuraStoreContext());
    }
}
