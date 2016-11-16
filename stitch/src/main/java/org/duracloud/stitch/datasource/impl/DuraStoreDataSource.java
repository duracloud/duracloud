/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.datasource.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.duracloud.stitch.datasource.DataSource;
import org.duracloud.stitch.error.DataSourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a DuraStore implementation of the DataSource interface.
 *
 * @author Andrew Woods
 *         Date: 9/2/11
 */
public class DuraStoreDataSource implements DataSource {

    private Logger log = LoggerFactory.getLogger(DuraStoreDataSource.class);

    private ContentStore store;

    public DuraStoreDataSource(ContentStore store) {
        this.store = store;
    }

    @Override
    public Content getContent(String spaceId, String contentId) {
        log.debug("getContent({}, {})", spaceId, contentId);

        try {
            return store.getContent(spaceId, contentId);

        } catch (ContentStoreException e) {
            String msg = "Error getting content: " + e.getMessage();
            if(!(e instanceof NotFoundException)){
                log.error(msg);
            }else{
                log.debug(msg);
            }
            throw new DataSourceException(msg, e);
        }
    }
}
