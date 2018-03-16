/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import java.util.Iterator;

import org.duracloud.chunk.util.ChunkUtil;

/**
 * This class exposes an iterator to a String listing that excludes content
 * chunks (as defined in ChunksManifest.java) and replaces items named as
 * chunk manifests (as defined in ChunksManifest.java) with their original
 * content name.
 *
 * @author Andrew Woods
 * Date: 9/9/11
 */
public class ChunkFilteredIterator implements Iterator<String> {

    private Iterator<String> iterator;
    private String nextItem;
    private ChunkUtil chunkUtil;

    public ChunkFilteredIterator(Iterator<String> iterator) {
        this.iterator = iterator;
        this.nextItem = null;
        this.chunkUtil = new ChunkUtil();
    }

    @Override
    public boolean hasNext() {
        if (null == nextItem && iterator.hasNext()) {
            nextItem = getNextFilteredItem();
        }

        return null != nextItem;
    }

    private String getNextFilteredItem() {
        String item = null;
        while (iterator.hasNext() && null == item) {
            item = iterator.next();
            if (chunkUtil.isChunk(item)) {
                item = null;

            } else if (chunkUtil.isChunkManifest(item)) {
                item = chunkUtil.preChunkedContentId(item);
            }
        }
        return item;
    }

    @Override
    public String next() {
        if (null == nextItem) {
            nextItem = getNextFilteredItem();
        }

        String item = nextItem;
        nextItem = getNextFilteredItem();

        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() not supported!");
    }

}
