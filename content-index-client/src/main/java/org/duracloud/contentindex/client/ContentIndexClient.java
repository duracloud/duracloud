/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.contentindex.client;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Erik Paulsson
 *         Date: 3/11/14
 */
public interface ContentIndexClient {

    public Iterator<ContentIndexItem> getSpaceContents(String account,
                                                   String storeId,
                                                   String space);

    public List<ContentIndexItem> getSpaceContents(String account,
                                                       String storeId,
                                                       String space,
                                                       int pageNum,
                                                       int pageSize);

    public Collection<String> getSpaces(String account, String storeId);

    public String save(AccountIndexItem item);

    public Iterator<String> getSpaceContentIds(String account,
                                                     String storeId,
                                                     String space);

    public List<String> getSpaceContentIds(String account,
                                           String storeId,
                                           String space,
                                           int pageNum,
                                           int pageSize);

    public Long getSpaceCount(String account, String storeId, String space);

    public ContentIndexItem get(String account, String storeId, String space,
                                String contentId);

    /**
     * Search all field values for the provided 'text'
     *
     * @param value   The text to find in any field's value
     * @param account The account to search, may be null to search across accounts
     * @param storeId The storeId to search, only used if account supplied.  May be null.
     * @param space   The space to search, may be null to search all spaces.
     * @return
     */
    public List<ContentIndexItem> getItemWithValue(String value, String account,
                                                   String storeId,
                                                   String space);

    /**
     * Saves or updates a ContentIndexItem
     *
     * @param item
     * @return the ID of the ContentIndexItem entity
     */
    public String save(ContentIndexItem item) throws ContentIndexClientValidationException;

    /**
     * Save multiple entities to the datastore in a single request.
     *
     * @param items
     * @throws ContentIndexClientValidationException 
     */
    public void bulkSave(List<ContentIndexItem> items) throws ContentIndexClientValidationException;

    public void addIndex(String index, boolean isAlias);

    public int getPageSize();
    public void setPageSize(int pageSize);
}
