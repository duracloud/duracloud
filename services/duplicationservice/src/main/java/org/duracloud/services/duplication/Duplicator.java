/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * Performs duplication activities
 *
 * @author Bill Branan
 */
public class Duplicator {

    private static final Logger log = LoggerFactory.getLogger(Duplicator.class);

    private ContentStore fromStore;
    private ContentStore toStore;

    public Duplicator(String host,
                      String port,
                      String context,
                      Credential credential,
                      String fromStoreId,
                      String toStoreId) {
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(host, port, context);

        storeManager.login(credential);
        try {
            fromStore = storeManager.getContentStore(fromStoreId);
            toStore = storeManager.getContentStore(toStoreId);
        } catch(ContentStoreException cse) {
            String error = "Unable to create connections to content " +
            		       "stores for duplication " + cse.getMessage();
            log.error(error);
        }
    }

    public void duplicateSpace(String spaceId) {
        if(log.isDebugEnabled()) {
            log.debug("Performing Duplication for " + spaceId +
                      " from " + fromStore.getStorageProviderType() +
                      " to " + toStore.getStorageProviderType());
        }

        try {
            toStore.getSpaceMetadata(spaceId);
            log.info("toSpace: " + spaceId);

            Map<String, String> spaceMeta = fromStore.getSpaceMetadata(spaceId);
            toStore.setSpaceMetadata(spaceId, spaceMeta);
        } catch(ContentStoreException cse) {
            log.info("Space " + spaceId + " does not exist at " +
                               toStore.getStorageProviderType());

            try {
                Map<String, String> spaceMeta = fromStore.getSpaceMetadata(spaceId);
                toStore.createSpace(spaceId, spaceMeta);
            } catch (ContentStoreException cs) {
                String error = "Unable to duplicate space " + spaceId +
                               " due to error: " + cse.getMessage();
                log.error(error, cs);
                deleteSpace(spaceId);
            }
        }
    }

    public void deleteSpace(String spaceId) {
        if(log.isDebugEnabled()) {
            log.debug("Deleting space " + spaceId +
                      " from " + toStore.getStorageProviderType());
        }

        try {
            toStore.deleteSpace(spaceId);
        } catch (ContentStoreException cse) {
            String error = "Unable to delete space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);
        }
    }

    public void duplicateContent(String spaceId, String contentId) {
        if(log.isDebugEnabled()) {
            log.debug("Performing Duplication for " + spaceId + "/" + contentId +
                      " from " + fromStore.getStorageProviderType() +
                      " to " + toStore.getStorageProviderType());
        }

        duplicateSpace(spaceId);

        Content content = null;

        try {
            content = fromStore.getContent(spaceId, contentId);
        } catch(ContentStoreException cse) {
            String error = "Content " + contentId + " does not exist in space " +
                           spaceId;
            log.error(error, cse);

            try {
                toStore.deleteContent(spaceId, contentId);
            } catch(ContentStoreException cs) {
                error = "Unable to delete content " + contentId + " in space " +
                                spaceId + " due to error: " + cse.getMessage();
                log.error(error, cs);
            }
        }

        try {
            InputStream contentStream = content.getStream();
            if(contentStream != null) {
                Map<String, String> metadata = content.getMetadata();

                String mimeType = "application/octet-stream";
                long contentSize = 0;
                String checksum = null;

                if(metadata != null) {
                    mimeType = metadata.get(ContentStore.CONTENT_MIMETYPE);

                    String size = metadata.get(ContentStore.CONTENT_SIZE);
                    if(size != null) {
                        try {
                            contentSize = Long.valueOf(size);
                        } catch(NumberFormatException nfe) {
                            log.warn("Could not convert stream size header " +
                            		 "value '" + size + "' to a number");
                            contentSize = 0;
                        }
                    }

                    checksum = metadata.get(ContentStore.CONTENT_CHECKSUM);
                }

                toStore.addContent(spaceId,
                                   contentId,
                                   contentStream,
                                   contentSize,
                                   mimeType,
                                   checksum,
                                   metadata);
            } else {
                throw new ContentStoreException("The content stream retrieved " +
                                                "from the store was null.");
            }
        } catch(ContentStoreException cse) {
            String error = "Unable to duplicate content " + contentId + " in space " +
                           spaceId + " due to error: " + cse.getMessage();
            log.error(error, cse);
        }

        if(log.isDebugEnabled()) {
            log.debug("Duplication Completed");
        }
    }

}
