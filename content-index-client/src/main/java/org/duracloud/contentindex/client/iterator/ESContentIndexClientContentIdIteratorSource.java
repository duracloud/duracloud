/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client.iterator;

import org.duracloud.contentindex.client.ESContentIndexClient;

import java.util.Collection;

/**
 * @author Erik Paulsson
 *         Date: 5/7/14
 */
public class ESContentIndexClientContentIdIteratorSource extends AbstractESContentIndexClientIteratorSource {

    public ESContentIndexClientContentIdIteratorSource(
        ESContentIndexClient contentIndexClient, String account, String storeId,
        String space) {
        super(contentIndexClient, account, storeId, space);
    }

    public Collection<String> getNext() {
        Collection<String> contentIds = contentIndexClient.getSpaceContentIds(
            account, storeId, space, pageNum, contentIndexClient.getPageSize());
        if(contentIds.size() > 0) {
            pageNum++;
            return contentIds;
        } else {
            return null;
        }
    }
}
