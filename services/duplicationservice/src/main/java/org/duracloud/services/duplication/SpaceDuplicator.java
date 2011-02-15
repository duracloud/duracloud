/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Performs space replication activities
 *
 * @author Kristen Cannava
 */
public class SpaceDuplicator {

    private static final Logger log =
        LoggerFactory.getLogger(SpaceDuplicator.class);

    private ContentStore fromStore;
    private ContentStore toStore;
    
    public SpaceDuplicator(ContentStore fromStore,
                           ContentStore toStore) {
        this.fromStore = fromStore;
        this.toStore = toStore;
    }

    public void createSpace(String spaceId) {
        logDebug("Creating", spaceId);

        if(spaceId == null)
            return;

        try {
            Map<String, String> spaceMeta = fromStore.getSpaceMetadata(spaceId);

            retryCreateSpace(spaceId, spaceMeta);
        } catch (ContentStoreException cse) {
            String error = "Unable to create space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);
        }
    }

    public void updateSpace(String spaceId) {
        logDebug("Updating", spaceId);

        if(spaceId == null)
            return;

        try {
            // Set Space metadata
            Map<String, String> spaceMeta = fromStore.getSpaceMetadata(spaceId);

            setSpaceMetadata(spaceId, spaceMeta);

            // Set Space access
            ContentStore.AccessType spaceAccess = fromStore.getSpaceAccess(spaceId);
            toStore.setSpaceAccess(spaceId, spaceAccess);
        } catch (ContentStoreException cse) {
            String error = "Unable to update space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);
        }
    }

    public void deleteSpace(String spaceId) {
        logDebug("Deleting", spaceId);

        if(spaceId == null)
            return;

        try {
            toStore.deleteSpace(spaceId);
        } catch (ContentStoreException cse) {
            String error = "Unable to delete space " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);
        }
    }

    private void setSpaceMetadata(String spaceId, Map<String, String> spaceMeta) {
        try {
            toStore.setSpaceMetadata(spaceId, spaceMeta);
        } catch(NotFoundException nfe) {
            String error = "Unable to update space " + spaceId +
                       " due to not found error: " + nfe.getMessage();
            log.error(error, nfe);

            retryCreateSpace(spaceId, spaceMeta);
        } catch (ContentStoreException cse) {
            String error = "Unable to update space metadata " + spaceId +
                           " due to error: " + cse.getMessage();
            log.error(error, cse);

            retrySetSpaceMetadata(spaceId, spaceMeta);
        }
    }

    private void retryCreateSpace(final String spaceId,
                                  final Map<String, String> spaceMeta) {
        new RetryDuplicate() {
            protected void doReplicate() throws Exception {
                toStore.createSpace(spaceId, spaceMeta);
            }
        }.replicate();
    }

    private void retrySetSpaceMetadata(final String spaceId,
                                       final Map<String, String> spaceMeta) {
        new RetryDuplicate() {
            protected void doReplicate() throws Exception {
                toStore.setSpaceMetadata(spaceId, spaceMeta);
            }
        }.replicate();
    }

    private void logDebug(String action, String spaceId) {
        if(log.isDebugEnabled()) {
            log.debug(action + " space " + spaceId +
                      " from " + fromStore.getStorageProviderType() + " to " +
                      toStore.getStorageProviderType());
        }
    }
}