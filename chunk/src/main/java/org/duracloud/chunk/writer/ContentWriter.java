/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.writer;

import org.duracloud.chunk.ChunkableContent;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.stream.ChunkInputStream;

import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Feb 5, 2010
 */
public interface ContentWriter {

    /**
     * This method writes the ChunkableContent to the arg space.
     *
     * @param spaceId   destination where arg chunkable content will be written
     * @param chunkable content to be written
     * @return ChunksManifest of written content
     * @throws NotFoundException on error
     */
    public ChunksManifest write(String spaceId, ChunkableContent chunkable)
        throws NotFoundException;

    /**
     * This method writes the arg Chunk to the arg space.
     * It is intended for use when when the arg chunk is actually a complete
     * piece of content
     *
     * @param spaceId destination where arg chunk content will be written
     * @param chunkChecksum md5 checksum of the chunk if known, null otherwise     
     * @param chunk   content to be written
     * @return MD5 of content
     * @throws NotFoundException on error
     */
    public String writeSingle(String spaceId,
                              String chunkChecksum,
                              ChunkInputStream chunk)
        throws NotFoundException;

    /**
     * This method helps with the book-keeping of which files are ignored.
     *
     * @param spaceId     destination where arg content was assigned to be written
     * @param contentId   of content
     * @param contentSize of content
     */
    public void ignore(String spaceId, String contentId, long contentSize);

    /**
     * This method returns an item-by-item list of results for the write
     * requests.
     *
     * @return List of results
     */
    public List<AddContentResult> getResults();

}
