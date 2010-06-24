/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk.manifest.xml;

import org.duracloud.ChunkType;
import org.duracloud.ChunksManifestDocument;
import org.duracloud.ChunksManifestType;
import org.duracloud.ChunksType;
import org.duracloud.HeaderType;
import org.duracloud.SourceContentType;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.manifest.ChunksManifestBean;
import org.duracloud.common.error.DuraCloudRuntimeException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for binding ChunksManifest xml documents to
 * ChunksManifest objects.
 *
 * @author Andrew Woods
 *         Date: Feb 9, 2010
 */
public class ManifestElementReader {

    /**
     * This method binds a ChunksManifest xml document to a ChunksManifest
     * object
     *
     * @param doc ChunksManifest xml document
     * @return ChunksManifest object
     */
    public static ChunksManifest createManifestFrom(ChunksManifestDocument doc) {
        ChunksManifestType manifestType = doc.getChunksManifest();

        HeaderType headerType = manifestType.getHeader();
        ChunksManifest.ManifestHeader header = createHeaderFromElement(
            headerType);

        ChunksType chunksType = manifestType.getChunks();
        List<ChunksManifestBean.ManifestEntry> entries = createEntriesFromElement(
            chunksType);

        ChunksManifestBean manifestBean = new ChunksManifestBean();
        manifestBean.setHeader(header);
        manifestBean.setEntries(entries);

        return new ChunksManifest(manifestBean);
    }

    private static ChunksManifestBean.ManifestHeader createHeaderFromElement(
        HeaderType headerType) {
        checkSchemaVersion(headerType.getSchemaVersion());

        SourceContentType sourceContentType = headerType.getSourceContent();
        String contentId = sourceContentType.getContentId();
        String mime = sourceContentType.getMimetype();
        String md5 = sourceContentType.getMd5();
        long size = sourceContentType.getByteSize();

        ChunksManifestBean.ManifestHeader header = new ChunksManifestBean.ManifestHeader(
            contentId,
            mime,
            size);
        header.setSourceMD5(md5);

        return header;
    }

    private static void checkSchemaVersion(String schemaVersion) {
        if (!schemaVersion.equals(ChunksManifest.SCHEMA_VERSION)) {
            // FIXME: add proper runtime exception
            throw new DuraCloudRuntimeException(
                "Unsupported schema version: " + schemaVersion);
        }
    }

    private static List<ChunksManifestBean.ManifestEntry> createEntriesFromElement(
        ChunksType chunksType) {
        List<ChunksManifestBean.ManifestEntry> entries = new ArrayList<ChunksManifestBean.ManifestEntry>();

        ChunksManifestBean.ManifestEntry entry;
        ChunkType[] chunkTypes = chunksType.getChunkArray();
        if (null != chunkTypes && chunkTypes.length > 0) {
            for (ChunkType chunkType : chunkTypes) {

                String chunkId = chunkType.getChunkId();
                String md5 = chunkType.getMd5();
                int index = chunkType.getIndex();
                long size = chunkType.getByteSize();
                entry = new ChunksManifestBean.ManifestEntry(chunkId,
                                                             md5,
                                                             index,
                                                             size);
                entries.add(entry);
            }
        }

        return entries;
    }

}