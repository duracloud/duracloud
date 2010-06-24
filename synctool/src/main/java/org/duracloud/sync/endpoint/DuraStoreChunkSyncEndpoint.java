/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.chunk.FileChunker;
import org.duracloud.chunk.FileChunkerOptions;
import org.duracloud.chunk.writer.DuracloudContentWriter;

import java.io.File;

/**
 * @author: Bill Branan
 * Date: Apr 8, 2010
 */
public class DuraStoreChunkSyncEndpoint extends DuraStoreSyncEndpoint {

    private long maxFileSize;
    private FileChunker chunker;

    public DuraStoreChunkSyncEndpoint(String host,
                                      int port,
                                      String context,
                                      String username,
                                      String password,
                                      String spaceId,
                                      boolean syncDeletes,
                                      long maxFileSize) {
        super(host, port, context, username, password, spaceId, syncDeletes);

        if(maxFileSize % 1024 != 0) {
            throw new RuntimeException("Max file size must be a " +
                                       "multiple of 1024");
        }
        this.maxFileSize = maxFileSize;

        DuracloudContentWriter contentWriter =
            new DuracloudContentWriter(getContentStore(), true);
        FileChunkerOptions chunkerOptions =
            new FileChunkerOptions(maxFileSize);
        chunker = new FileChunker(contentWriter, chunkerOptions);
    }

    @Override
    protected void addUpdateContent(String contentId,
                                    String contentChecksum,
                                    File syncFile) {
        chunker.addContent(getSpaceId(), contentId, contentChecksum, syncFile);
    }

}
