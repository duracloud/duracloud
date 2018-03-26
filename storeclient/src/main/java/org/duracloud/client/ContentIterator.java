/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.duracloud.common.util.WaitUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterates over the content list in a DuraCloud space. Handles the chunked
 * nature of long item lists internally, allowing the caller to simply
 * call next() to iterate through the entire content listing, regardless
 * of its length.
 *
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
    private int maxRetries;

    private final Logger log =
        LoggerFactory.getLogger(ContentIterator.class);

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
        this.maxRetries = 7;
        contentList = retryBuildContentList(null);
    }

    public void setMaxRetries(int maxRetries) {
        if (maxRetries >= 0) {
            this.maxRetries = maxRetries;
        }
    }

    public boolean hasNext() {
        if (index < contentList.size()) {
            return true;
        } else {
            if (contentList.size() > 0) {
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
        String lastItem = contentList.get(contentList.size() - 1);
        try {
            contentList = retryBuildContentList(lastItem);
        } catch (ContentStoreException e) {
            throw new RuntimeException(e);
        }
        index = 0;
    }

    private List<String> retryBuildContentList(String lastItem)
        throws ContentStoreException {
        ContentStoreException lastException = null;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                return buildContentList(lastItem);
            } catch (ContentStoreException e) {
                lastException = e;
                log.warn(e.getMessage());
                WaitUtil.wait(i);
            }
        }
        throw lastException;
    }

    private List<String> buildContentList(String lastItem)
        throws ContentStoreException {
        return store.getSpace(spaceId, prefix, maxResults, lastItem)
                    .getContentIds();
    }

}