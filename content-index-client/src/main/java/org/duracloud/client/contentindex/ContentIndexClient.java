/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.contentindex;

import java.util.List;

/**
 * @author Erik Paulsson
 *         Date: 3/11/14
 */
public interface ContentIndexClient {

    public List<ContentIndexItem> getSpaceContents(String account,
                                                   int storeId,
                                                   String space);

    public List<ContentIndexItem> getSpaceContentIds(String account,
                                           int storeId,
                                           String space);

    public Long getSpaceCount(String account,
                                 int storeId,
                                 String space);

    public ContentIndexItem get(String account,
                                                int storeId,
                                                String space,
                                                String contentId);

    /**
     * Saves or updates a ContentIndexItem
     * @param item
     * @return the ID of the ContentIndexItem entity
     */
    public String save(ContentIndexItem item);

    /**
     * Save multiple entities to the datastore in a single request.
     * @param items
     */
    public void bulkSave(List<ContentIndexItem> items);

    public void addIndex(String index, boolean isAlias);
}
