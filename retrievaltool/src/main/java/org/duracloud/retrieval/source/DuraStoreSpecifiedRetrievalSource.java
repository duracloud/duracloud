/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.chunk.util.ChunkUtil;
import org.duracloud.client.ContentStore;
import org.duracloud.common.constant.ManifestFormat;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.manifest.ManifestFormatter;
import org.duracloud.manifest.impl.ManifestFormatterFactory;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.retrieval.mgmt.RetrievalListener;
import org.duracloud.stitch.error.MissingContentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the RetrievalSource interface with support for retrieving
 * content by content IDs that are specified in a file.  The format of the file
 * containing content IDs should be one content ID per line.
 * This retrieval source only operates on one store space.
 * Since this class extends DuraStoreStitchingRetrievalSource it can handle retrieving
 * chunked content.
 *
 * @author Erik Paulsson
 * Date: 7/2/13
 */
public class DuraStoreSpecifiedRetrievalSource extends DuraStoreStitchingRetrievalSource {

    private final Logger log = LoggerFactory.getLogger(
        DuraStoreSpecifiedRetrievalSource.class);

    private Iterator<String> specifiedContentIds;

    private ChunkUtil chunkUtil = null;

    public DuraStoreSpecifiedRetrievalSource(ContentStore store,
                                             List<String> singleSpaceList,
                                             Iterator<String> specifiedContentIds) {
        super(store, singleSpaceList, false);
        if (singleSpaceList == null) {
            throw new DuraCloudRuntimeException("The space list specified for " +
                                                "DuraStoreSpecifiedRetrievelSource must not be NULL.");
        } else if (singleSpaceList.isEmpty()) {
            throw new DuraCloudRuntimeException("The space list specified for " +
                                                "DuraStoreSpecifiedRetrievelSource must contain 1 space ID.");
        } else if (singleSpaceList.size() > 1) {
            throw new DuraCloudRuntimeException("The space list specified for " +
                                                "DuraStoreSpecifiedRetrievelSource must contain only 1 space ID.");
        }

        this.chunkUtil = new ChunkUtil();
        this.specifiedContentIds = specifiedContentIds;
        this.reviewSpecifiedContentIdsForChunkedContent(singleSpaceList);
    }

    protected void reviewSpecifiedContentIdsForChunkedContent(List<String> singleSpaceList) {
        log.debug("enter reviewSpecifiedContentIdsForChunkedContent()");

        List<String> retrievalContentIds = new ArrayList<String>();
        while (specifiedContentIds.hasNext()) {
            String specifiedContentId = specifiedContentIds.next();
            retrievalContentIds.add(specifiedContentId);
        }
        log.debug("total contentIds in list-file: " + retrievalContentIds.size());

        List<String> retrievalSpaceContentIds = new ArrayList<String>();

        Iterator<String> retrievalSpaceIds = verifySpaceIds(singleSpaceList);
        if (retrievalSpaceIds.hasNext()) {
            String currentRetrievalSpaceId = retrievalSpaceIds.next();
            log.debug("searching for contentIds in space: " + currentRetrievalSpaceId);
            try {
                InputStream manifest = contentStore.getManifest(currentRetrievalSpaceId, ManifestFormat.TSV);
                ManifestFormatter formatter = new ManifestFormatterFactory().create(ManifestFormat.TSV);
                String header = formatter.getHeader();
                BufferedReader reader = new BufferedReader(new InputStreamReader(manifest));
                String line = null;
                ManifestItem item = null;

                try {
                    while ((line = reader.readLine()) != null) {
                        // ignore any whitespace
                        if (line.trim().length() == 0) {
                            continue;
                        }

                        // ignore header line
                        if (line.equals(header)) {
                            continue;
                        }

                        try {
                            item = formatter.parseLine(line);
                        } catch (ParseException e) {
                            throw new IOException(e);
                        }

                        String contentId = item.getContentId();

                        // search for chunks or chunk manifests for contentId
                        String manifestContentId = contentId;
                        if (chunkUtil.isChunkManifest(contentId)) {
                            manifestContentId = StringUtils.removeEnd(contentId, ChunksManifest.manifestSuffix);
                        }

                        if (retrievalContentIds.contains(manifestContentId)) {
                            log.debug("found chunked content manifest for contentId in list-file: " + contentId);
                            retrievalSpaceContentIds.add(contentId);
                        }
                    }
                } catch (IOException ex) {
                    log.error("Error reading space manifest.");
                }
            } catch (ContentStoreException cse) {
                log.error("Unable to retrieve space manifest. If files-list.txt contains chunked files and the " +
                          "retrieval fails the local content dir will need to be empty.");
            }
        }

        if (!retrievalSpaceContentIds.isEmpty()) {
            Collections.sort(retrievalSpaceContentIds);
            log.debug("List of contentIds to retrieve has increased from " + retrievalContentIds.size() + " to " +
                     retrievalSpaceContentIds.size() + " after chunked content is included.");
            this.specifiedContentIds = retrievalSpaceContentIds.iterator();
        }
    }

    @Override
    protected void getNextSpace() {
        if (spaceIds.hasNext()) {
            currentSpaceId = spaceIds.next();
            currentContentList = specifiedContentIds;
        }
    }
}
