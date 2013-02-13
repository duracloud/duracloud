/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import org.duracloud.client.ContentStore;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Oct 12, 2010
 */
public class DuraStoreRetrievalSource implements RetrievalSource {

    private final Logger logger =
        LoggerFactory.getLogger(DuraStoreRetrievalSource.class);

    private ContentStore contentStore = null;
    private Iterator<String> spaceIds = null;
    private String currentSpaceId = null;
    private Iterator<String> currentContentList = null;

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
            spaceIds = spaces.iterator();
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

    private void getNextSpace() {
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
    public String getSourceChecksum(ContentItem contentItem) {
        try {
            Map<String, String> properties =
                contentStore.getContentProperties(contentItem.getSpaceId(),
                                                  contentItem.getContentId());
            return properties.get(ContentStore.CONTENT_CHECKSUM);
        } catch(ContentStoreException e) {
            throw new RuntimeException("Unable to get checksum for " +
                                       contentItem.toString() + " due to: " +
                                       e.getMessage());
        }
    }

    @Override
    public ContentStream getSourceContent(ContentItem contentItem) {
        Content content = doGetContent(contentItem);

        Map<String, String> props = content.getProperties();
        String checksum = props.get(ContentStore.CONTENT_CHECKSUM);
        String dateCreated = props.get(ContentStore.CONTENT_FILE_CREATED);
        String dateLastAccessed = props.get(ContentStore.CONTENT_FILE_ACCESSED);
        String dateLastModified = props.get(ContentStore.CONTENT_FILE_MODIFIED);

        return new ContentStream(content.getStream(),
                                 checksum,
                                 dateCreated,
                                 dateLastAccessed,
                                 dateLastModified);
    }

    protected Content doGetContent(ContentItem contentItem) {
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
