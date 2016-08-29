/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.stream;

import org.duracloud.common.util.MimetypeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * This class provides the ability to limit the number of bytes read from the
 * provided InputStream to maxChunkSize.
 *
 * @author Andrew Woods
 *         Date: Feb 2, 2010
 */
public class ChunkInputStream extends InputStream {
    private final Logger log = LoggerFactory.getLogger(ChunkInputStream.class);
    private static final MimetypeUtil mimeUtil = new MimetypeUtil();

    private String chunkId;
    private CountingDigestInputStream stream;
    private long chunkSize;
    private String mimetype;
    private boolean preserveMD5;

    public ChunkInputStream(String chunkId,
                            InputStream inputStream,
                            long chunkSize,
                            boolean preserveMD5) {
        this.stream = new CountingDigestInputStream(inputStream, preserveMD5);
        this.chunkId = chunkId;
        this.chunkSize = chunkSize;
        this.preserveMD5 = preserveMD5;

        mimetype = mimeUtil.getMimeType(chunkId);
    }

    /**
     * This method reads up to chunkSize number of bytes from the stream.
     * When either chunkSize bytes have been read, or the end of the stream
     * is reached, -1 is return.
     *
     * @return current byte or -1 if eof reached
     * @throws IOException on error
     */
    public int read() throws IOException {
        if (stream.getByteCount() >= chunkSize) {
            return -1;
        }

        return stream.read();
    }
    
    
    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    
    
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        long byteCount = stream.getByteCount();
        
        if (byteCount >= chunkSize) {
            return -1;
        }
        
        long bytesRemainingInChunk = chunkSize - byteCount;
        
        if(bytesRemainingInChunk <= len){
            len = (int)bytesRemainingInChunk;
        }
        return stream.read(b, off, len);

    }
 
    public void close() throws IOException {
        // do not allow the wrapped stream to be closed.
    }

    public String getMD5() {
        return stream.getMD5();
    }

    public long numBytesRead() {
        return stream.getByteCount();
    }

    public String getChunkId() {
        return chunkId;
    }

    public String getMimetype() {
        return mimetype;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public boolean md5Preserved() {
        return preserveMD5;
    }
}
