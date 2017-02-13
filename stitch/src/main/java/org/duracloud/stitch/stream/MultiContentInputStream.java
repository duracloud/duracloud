/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.stream;

import org.apache.commons.io.IOUtils;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.stitch.datasource.DataSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

/**
 * This class exposes a single InputStream composed of the sequence of content
 * streams read on-demand from the provided list of ContentItems.
 *
 * @author Andrew Woods
 *         Date: 9/8/11
 */
public class MultiContentInputStream extends InputStream {
    
    private DataSource dataSource;
    private Iterator<ContentItem> contents;
    private InputStream currentStream;
    private MultiContentInputStreamListener listener;
    private ContentItem currentItem;

    public MultiContentInputStream(DataSource dataSource,
                                   List<ContentItem> contentItems,
                                   MultiContentInputStreamListener listener) {
        this.dataSource = dataSource;
        this.contents = contentItems.iterator();
        this.currentStream = null;
        this.listener = listener;
    }

    public MultiContentInputStream(DataSource dataSource,
                                   List<ContentItem> contentItems) {
        this(dataSource, contentItems, null);
    }

    @Override
    public int read() throws IOException {
        // initialize current stream
        if (null == currentStream) {
            if (contents.hasNext()) {
                currentStream = nextStream();

            } else {
                return -1;
            }
        }

        int bite = currentStream.read();
        if (-1 == bite) {
            if(contents.hasNext()){
                currentStream = nextStream();
                bite = currentStream.read();
            }else {
                notifyContentIdRead();
            }
        }

        return bite;
    }

    private InputStream nextStream() {
        if(this.currentStream != null){
            IOUtils.closeQuietly(this.currentStream);
            notifyContentIdRead();
        }
        currentItem = contents.next();
        return getStream(currentItem);
    }

    protected void notifyContentIdRead() {
        if(this.listener != null && this.currentItem != null){
            this.listener.contentIdRead(this.currentItem.getContentId());
            this.currentItem = null;
        }
    }

    private InputStream getStream(ContentItem contentItem) {
        Content content = dataSource.getContent(contentItem.getSpaceId(),
                                                contentItem.getContentId());
        return content.getStream();
    }

}
