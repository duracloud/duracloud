/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import org.duracloud.storage.provider.StorageProvider;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @author: Bill Branan
 * Date: Dec 22, 2009
 */
public class ContentIterator implements Iterator<String> {

    private StorageProvider provider;
    private String spaceId;
    private String prefix;

    private int index;
    private List<String> contentList;
    private long maxResults;

    public ContentIterator(StorageProvider provider,
                           String spaceId,
                           String prefix) {
        this(provider, spaceId, prefix, StorageProvider.DEFAULT_MAX_RESULTS);
    }

    public ContentIterator(StorageProvider provider,
                           String spaceId,
                           String prefix,
                           long maxResults) {
        index = 0;
        this.provider = provider;
        this.spaceId = spaceId;
        this.prefix = prefix;
        this.maxResults = maxResults;
        contentList = provider.getSpaceContentsChunked(spaceId,
                                                       prefix,
                                                       maxResults,
                                                       null);
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
        String lastItem = contentList.get(contentList.size()-1);
        contentList = provider.getSpaceContentsChunked(spaceId,
                                                       prefix,
                                                       maxResults,
                                                       lastItem);
        index = 0;
    }

}
