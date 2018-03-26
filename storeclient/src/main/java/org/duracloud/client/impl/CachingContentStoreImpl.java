/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.impl;

import java.util.List;

import org.duracloud.client.ContentStoreImpl;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class retains the results of the initial call to:
 * getSpaces()
 * <p/>
 * and returns this cached value on subsequent calls.
 *
 * @author Andrew Woods
 * Date: Nov 30, 2010
 */
public class CachingContentStoreImpl extends ContentStoreImpl {

    private static final Logger log = LoggerFactory.getLogger(
        CachingContentStoreImpl.class);

    private List<String> spaces;

    public CachingContentStoreImpl(String baseURL,
                                   StorageProviderType type,
                                   String storeId,
                                   RestHttpHelper restHelper) {
        super(baseURL, type, storeId, restHelper);
    }

    @Override
    public List<String> getSpaces() throws ContentStoreException {
        log.debug("enter: getSpaces()");
        if (null == this.spaces) {
            log.debug("populating cache.");
            this.spaces = super.getSpaces();
        }
        return this.spaces;
    }

}
