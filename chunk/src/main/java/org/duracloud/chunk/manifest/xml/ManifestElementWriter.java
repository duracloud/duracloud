/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest.xml;

import org.apache.commons.lang.StringUtils;
import org.duracloud.ChunkType;
import org.duracloud.ChunksManifestType;
import org.duracloud.ChunksType;
import org.duracloud.HeaderType;
import org.duracloud.SourceContentType;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;

import java.util.List;

/**
 * This class is responsible for serializing ChunksManifest objects into
 * ChunksManifest xml documents.
 *
 * @author Andrew Woods
 *         Date: Feb 9, 2010
 */
public class ManifestElementWriter {

    /**
     * This method serializes a ChunksManifest object into a ChunksManifest
     * xml element.
     *
     * @param manifest object to be serialized
     * @return xml ChunksManifest element with content from arg manifest
     */
    public static ChunksManifestType createChunksManifestElementFrom(
        ChunksManifestBean manifest) {
        ChunksManifestType manifestType = ChunksManifestType.Factory
            .newInstance();
        populateElementFromObject(manifestType, manifest);

        return manifestType;
    }

    private static void populateElementFromObject(ChunksManifestType manifestType,
                                                  ChunksManifestBean manifest) {
        HeaderType headerType = manifestType.addNewHeader();
        populateHeaderType(headerType, manifest.getHeader());

        ChunksType chunksType = manifestType.addNewChunks();
        populateChunksType(chunksType, manifest.getEntries());
    }

    private static void populateHeaderType(HeaderType headerType,
                                           ChunksManifestBean.ManifestHeader header) {
        headerType.setSchemaVersion(ChunksManifest.SCHEMA_VERSION);

        SourceContentType sourceContentType = headerType.addNewSourceContent();

        String contentId = header.getSourceContentId();
        if (!StringUtils.isBlank(contentId)) {
            sourceContentType.setContentId(contentId);
        }

        String mime = header.getSourceMimetype();
        if (!StringUtils.isBlank(mime)) {
            sourceContentType.setMimetype(mime);
        }

        String md5 = header.getSourceMD5();
        if (!StringUtils.isBlank(md5)) {
            sourceContentType.setMd5(md5);
        }

        long size = header.getSourceByteSize();
        if (size > -1) {
            sourceContentType.setByteSize(size);
        }

    }

    private static void populateChunksType(ChunksType chunksType,
                                           List<ChunksManifestBean.ManifestEntry> entries) {
        if (null != entries && entries.size() > 0) {
            for (ChunksManifestBean.ManifestEntry entry : entries) {
                ChunkType chunkType = chunksType.addNewChunk();

                String chunkId = entry.getChunkId();
                if (!StringUtils.isBlank(chunkId)) {
                    chunkType.setChunkId(chunkId);
                }

                String md5 = entry.getChunkMD5();
                if (!StringUtils.isBlank(md5)) {
                    chunkType.setMd5(md5);
                }

                long size = entry.getByteSize();
                if (size > -1) {
                    chunkType.setByteSize(size);
                }

                int index = entry.getIndex();
                if (index > -1) {
                    chunkType.setIndex(index);
                }
            }
        }

    }

}