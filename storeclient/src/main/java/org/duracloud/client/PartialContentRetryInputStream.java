/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import java.io.IOException;
import java.io.InputStream;

import org.duracloud.common.retry.Retrier;
import org.duracloud.common.web.RestHttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides midstream retries of content stream should any interruption occur.
 * If an IOException occurs will reading the underlying stream, range requests will be retried up to three times from
 * each point of failure with exponential back off (the last attempt will  be tried 9 seconds after the penultimate).
 * In other words, if a failure occurs the class will make three attempts to re-acquire the stream.  If it is
 * successful, streaming will resume. If a second failure occurs, the three new attempts will be made before throwing an
 * IOException.
 *
 * @author dbernstein
 */
class PartialContentRetryInputStream extends InputStream {
    private static final Logger log = LoggerFactory.getLogger(PartialContentRetryInputStream.class);
    private ContentStoreImpl contentStore;
    private String spaceId;
    private String contentId;
    private InputStream currentStream;
    private Long startByte;
    private Long endByte;

    private long nextBytePos;

    /**
     * Constructor
     * @param contentStore The content store to use for retries
     * @param spaceId The spaceId of the content item to retry
     * @param contentId The content id of the item to retry
     * @param currentStream The initial content stream
     * @param startByte The starting byte offset of the specified stream
     * @param endByte The last byte offset of the specified stream. If streaming to the end of the file, use null.
     */
    PartialContentRetryInputStream(ContentStoreImpl contentStore, String spaceId, String contentId,
                                   InputStream currentStream, Long startByte, Long endByte) {
        this.currentStream = currentStream;
        this.spaceId = spaceId;
        this.contentId = contentId;
        this.startByte = startByte;
        this.endByte = endByte;
        this.contentStore = contentStore;
        this.nextBytePos = startByte;
    }

    @Override
    public int read() throws IOException {
        try {
            //read the next byte;
            int b = this.currentStream.read();
            nextBytePos++;
            return b;
        } catch (IOException ex) {
            log.warn(
                "Failed to read byte at position {} (space: {}, contentId: {}, startByte:{}, endByte: {}. " +
                "Starting attempts to re-acquire stream from current position.",
                nextBytePos, spaceId, contentId, startByte, endByte);
            try {
                //exponential backoff on retries
                new Retrier(5, 2000, 2).execute(() -> {
                    RestHttpHelper.HttpResponse response = contentStore.doGetContent(spaceId, contentId,
                                                                                     nextBytePos, endByte);
                    currentStream = response.getResponseStream();
                    log.info(
                        "Successfully  re-acquired stream (space: {}, contentId: {}, nextBytePos:{}," +
                        " endByte: {}. ",
                        spaceId, contentId, nextBytePos, endByte);
                    return null;
                });

                //stream re-acquired, start reading again.
                return read();
            } catch (Exception e) {
                log.error(
                    "Exhausted max retries to re-acquire stream (space: {}, contentId: {}, nextBytePos:{}," +
                    " endByte: {}. ",
                    spaceId, contentId, nextBytePos, endByte, e);
                throw new IOException(e.getMessage(), e);
            }
        }
    }
}
