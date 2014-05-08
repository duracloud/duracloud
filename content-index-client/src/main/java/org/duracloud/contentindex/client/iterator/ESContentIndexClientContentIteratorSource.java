/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client.iterator;

import org.duracloud.contentindex.client.ContentIndexItem;
import org.duracloud.contentindex.client.ESContentIndexClient;

import java.util.Collection;

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

    public Collection<ContentIndexItem> getNext() {
        Collection<ContentIndexItem> items = contentIndexClient.getSpaceContents(
            account, storeId, space, pageNum, contentIndexClient.getPageSize());
        if(items.size() > 0) {
            pageNum++;
            return items;
        } else {
            return null;
        }
    }
}
