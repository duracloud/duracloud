/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.retrieval.mgmt.RetrievalListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class DuraStoreRetrievalSource implements RetrievalSource {

    private final Logger logger =
        LoggerFactory.getLogger(DuraStoreRetrievalSource.class);

    protected ContentStore contentStore = null;
    protected Iterator<String> spaceIds = null;
    protected String currentSpaceId = null;
    protected Iterator<String> currentContentList = null;

    public DuraStoreRetrievalSource(ContentStore store,
                                    List<String> spaces,
                                    boolean allSpaces) {
        this.contentStore = store;

        if(allSpaces) {
            try {
                spaces = contentStore.getSpaces();
            } catch(ContentStoreException e) {
                throw new RuntimeException("Unable to acquire list of spaces");
            }
        }

        if(spaces != null && spaces.size() > 0) {
            try {
                // check if provided spaces exist
                List<String> spaceList = store.getSpaces();
                List<String> nonExistantSpaces = new ArrayList<String>();
                for(String space: spaces) {
                    if(! spaceList.contains(space)) {
                        nonExistantSpaces.add(space);
                    }
                }
                if(! nonExistantSpaces.isEmpty()) {
                    String error = "The following provided spaces do not exist: " +
                            StringUtils.join(nonExistantSpaces, ", ");
                    throw new DuraCloudRuntimeException(error);
                }

                spaceIds = spaces.iterator();
            } catch(ContentStoreException cse) {
                throw new DuraCloudRuntimeException("Error retrieving spaces list", cse);
            }
        } else {
            throw new RuntimeException("Spaces list is empty, there is " +
                                       "no content to retrieve");
        }
    }

    @Override
    public ContentItem getNextContentItem() {
        if(currentContentList != null && currentContentList.hasNext()) {
            return new ContentItem(currentSpaceId, currentContentList.next());
        } else if(spaceIds.hasNext()) {
            getNextSpace();
            return getNextContentItem();
        } else {
            return null;
        }
    }

    protected void getNextSpace() {
        if(spaceIds.hasNext()) {
            currentSpaceId = spaceIds.next();
            try {
                currentContentList =
                    contentStore.getSpaceContents(currentSpaceId);
            } catch(ContentStoreException e) {
                logger.error("Unable to get contents of space: " +
                    currentSpaceId + " due to error: " + e.getMessage());
                currentContentList = null;
            }
        }
    }

    @Override
    public Map<String,String> getSourceProperties(ContentItem contentItem) {
        try {
            Map<String, String> properties =
                contentStore.getContentProperties(contentItem.getSpaceId(),
                                                  contentItem.getContentId());
            return properties;
        } catch(ContentStoreException e) {
            throw new RuntimeException("Unable to get checksum for " +
                                           contentItem.toString() + " due to: " +
                                           e.getMessage());
        }
    }

    @Override
    public String getSourceChecksum(ContentItem contentItem) {
        return getSourceProperties(contentItem).
            get(ContentStore.CONTENT_CHECKSUM);
    }

    @Override
    public ContentStream getSourceContent(ContentItem contentItem, RetrievalListener listener) {
        Content content = doGetContent(contentItem, listener);
        return new ContentStream(content.getStream(),
                                 content.getProperties());
    }

    protected Content doGetContent(ContentItem contentItem, RetrievalListener listener) {
        try {
            return contentStore.getContent(contentItem.getSpaceId(),
                                           contentItem.getContentId());
        } catch (ContentStoreException e) {
            throw new RuntimeException(
                "Unable to get content for " + contentItem.toString() +
                    " due to: " + e.getMessage());
        }
    }

}
