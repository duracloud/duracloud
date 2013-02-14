/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncui.service;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.error.ContentStoreException;
import org.duracloud.syncui.domain.DuracloudConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
@Component("contentStoreManagerFactory")
public class ContentStoreManagerFactoryImpl
    implements ContentStoreManagerFactory {
    private SyncConfigurationManager syncConfigurationManager;

    @Autowired
    public ContentStoreManagerFactoryImpl(
        @Qualifier("syncConfigurationManager") SyncConfigurationManager syncConfigurationManager) {
        this.syncConfigurationManager = syncConfigurationManager;
    }

    @Override
    public ContentStoreManager create() throws ContentStoreException {
        DuracloudConfiguration dc =
            this.syncConfigurationManager.retrieveDuracloudConfiguration();
        ContentStoreManager csm =
            new ContentStoreManagerImpl(dc.getHost(),
                                        String.valueOf(dc.getPort()));
        return csm;

    }

}
