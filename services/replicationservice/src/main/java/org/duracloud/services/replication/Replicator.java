/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.replication;

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
 * Performs replication activities
 *
 * @author Bill Branan
 */
public class Replicator {

    private static final Logger log =
        LoggerFactory.getLogger(ReplicationService.class);

    private ContentStore fromStore;
    private ContentStore toStore;

    public Replicator(String host,
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
            		       "stores for replication " + cse.getMessage();
            log.error(error);
        }
    }

    public void replicateSpace(String spaceId) {
        if(log.isDebugEnabled()) {
            log.debug("Performing Replication for " + spaceId +
                      " from " + fromStore.getStorageProviderType() +
                      " to " + toStore.getStorageProviderType());
        }

        try {
            Map<String, String> spaceMeta = fromStore.getSpaceMetadata(spaceId);
            toStore.createSpace(spaceId, spaceMeta);
        } catch (ContentStoreException cse) {
            String error = "Unable to replicate space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);
        }
    }

    public void replicateContent(String spaceId, String contentId) {
        if(log.isDebugEnabled()) {
            log.debug("Performing Replication for " + spaceId + "/" + contentId +
                      " from " + fromStore.getStorageProviderType() +
                      " to " + toStore.getStorageProviderType());
        }

        try {
            toStore.getSpaceMetadata(spaceId);
            log.info("toSpace: " + spaceId);
        } catch(ContentStoreException cse) {
            log.info("Space " + spaceId + " does not exist at " +
                               toStore.getStorageProviderType());
            replicateSpace(spaceId);
        }

        try {
            Content content = fromStore.getContent(spaceId, contentId);
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
            String error = "Unable to replicate content " + contentId + " in space " +
                           spaceId + " due to error: " + cse.getMessage();
            log.error(error, cse);
        }

        if(log.isDebugEnabled()) {
            log.debug("Replication Completed");
        }
    }

}
