/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.error.ContentStoreException;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author: Bill Branan
 * Date: Dec 23, 2009
 */
public class ContentIterator implements Iterator<String> {

    private ContentStore store;
    private String spaceId;
    private String prefix;

    private int index;
    private List<String> contentList;
    private long maxResults;

    public ContentIterator(ContentStore store,
                           String spaceId,
                           String prefix) throws ContentStoreException {
        this(store, spaceId, prefix, StorageProvider.DEFAULT_MAX_RESULTS);
    }

    public ContentIterator(ContentStore store,
                           String spaceId,
                           String prefix,
                           long maxResults) throws ContentStoreException {
        index = 0;
        this.store = store;
        this.spaceId = spaceId;
        this.prefix = prefix;
        this.maxResults = maxResults;
        contentList = buildContentList(null);
    }

    public boolean hasNext() {
        if (index < contentList.size()) {
            return true;
        } else {
            if (contentList.size() >= maxResults) {
                updateList();
                return contentList.size() > 0;
            } else {
                return false;
            }
        }
    }

    public String next() {
        if (hasNext()) {
            String next = contentList.get(index);
            ++index;
            return next;
        } else {
            throw new NoSuchElementException();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

    private void updateList() {
        String lastItem = contentList.get(contentList.size()-1);
        try {
            contentList = buildContentList(lastItem);
        } catch(ContentStoreException e) {
            throw new RuntimeException(e);
        }
        index = 0;
    }

    private List<String> buildContentList(String lastItem)
        throws ContentStoreException {
        return store.getSpace(spaceId, prefix, maxResults, lastItem)
            .getContentIds();
    }

}