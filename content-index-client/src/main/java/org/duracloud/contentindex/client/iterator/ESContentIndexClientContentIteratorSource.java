/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client.iterator;

import java.util.Collection;

import org.duracloud.contentindex.client.ContentIndexItem;
import org.duracloud.contentindex.client.ESContentIndexClient;

/**
 * @author Erik Paulsson
 *         Date: 5/8/14
 */
public class ESContentIndexClientContentIteratorSource extends AbstractESContentIndexClientIteratorSource {

    public ESContentIndexClientContentIteratorSource(
        ESContentIndexClient contentIndexClient, String account, String storeId,
        String space) {
        super(contentIndexClient, account, storeId, space);
    }

    protected Collection<ContentIndexItem> getNextImpl() {
        return contentIndexClient.getSpaceContents(
            account, storeId, space, pageNum, contentIndexClient.getPageSize());
    }
}
