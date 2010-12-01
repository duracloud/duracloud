/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * This class retains the results of the initial call to both:
 * getContentStores() &
 * getPrimaryContentStore()
 * <p/>
 * and returns these cached values on subsequent calls.
 *
 * @author Andrew Woods
 *         Date: Nov 30, 2010
 */
public class CachingContentStoreManagerImpl extends ContentStoreManagerImpl {

    private static final Logger log = LoggerFactory.getLogger(
        CachingContentStoreManagerImpl.class);

    private Map<String, ContentStore> contentStores;
    private ContentStore primaryContentStore;

    public CachingContentStoreManagerImpl(String host,
                                          String port,
                                          String context) {
        super(host, port, context);
    }

    @Override
    public Map<String, ContentStore> getContentStores()
        throws ContentStoreException {
        log.debug("enter: getContentStores()");
        if (null == contentStores) {
            log.debug("populating cache.");
            this.contentStores = super.getContentStores();
        }
        return this.contentStores;
    }

    @Override
    public ContentStore getPrimaryContentStore() throws ContentStoreException {
        log.debug("enter: getPrimaryContentStore()");
        if (null == this.primaryContentStore) {
            log.debug("populating cache.");
            this.primaryContentStore = super.getPrimaryContentStore();
        }
        return this.primaryContentStore;
    }


    protected ContentStore newContentStoreImpl(StorageAccount acct) {
        return new CachingContentStoreImpl(getBaseURL(),
                                           acct.getType(),
                                           acct.getId(),
                                           getRestHelper());
    }

    protected void setRestHelper(RestHttpHelper restHelper) {
        super.setRestHelper(restHelper);
    }
}
