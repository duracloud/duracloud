/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import java.util.Iterator;
import java.util.List;

import org.duracloud.chunk.manifest.ChunksManifest;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
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

        this.specifiedContentIds = specifiedContentIds;
    }

    @Override
    protected void getNextSpace() {
        if (spaceIds.hasNext()) {
            currentSpaceId = spaceIds.next();
            currentContentList = specifiedContentIds;
        }
    }

    @Override
    protected Content doGetContent(ContentItem item, RetrievalListener listener) {
        try {
            return contentStore.getContent(item.getSpaceId(),
                                           item.getContentId());
        } catch (ContentStoreException cse) {
            log.info("Error retrieving content ID: " + item.getContentId() +
                     ".  Trying to get this content again by checking for " +
                     "a chunk manifest for this content ID.");
            // Create a new ContentItem representing the manifest file content ID
            // for the passed in ContentItem to this method.
            ContentItem manifestItem = new ContentItem(item.getSpaceId(),
                                                       item.getContentId() + ChunksManifest.manifestSuffix);
            try {
                return doGetContentFromManifest(manifestItem, listener);
            } catch (MissingContentException mse) {
                throw mse;
            }
        }
    }
}
