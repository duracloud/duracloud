/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.services.duplication.StoreCaller;
import org.duracloud.services.duplication.SpaceDuplicator;
import org.duracloud.services.duplication.error.DuplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Performs space replication activities
 *
 * @author Kristen Cannava
 */
public class SpaceDuplicatorImpl implements SpaceDuplicator {

    private static final Logger log = LoggerFactory.getLogger(
        SpaceDuplicatorImpl.class);

    private ContentStore fromStore;
    private ContentStore toStore;

    private int waitMillis;

    public SpaceDuplicatorImpl(ContentStore fromStore, ContentStore toStore) {
        this(fromStore, toStore, 1000);
    }

    public SpaceDuplicatorImpl(ContentStore fromStore,
                               ContentStore toStore,
                               int waitMillis) {
        this.fromStore = fromStore;
        this.toStore = toStore;
        this.waitMillis = waitMillis;
    }

    @Override
    public void createSpace(String spaceId) {
        logDebug("Creating", spaceId);

        if (spaceId == null) {
            String err = "Space to create is null.";
            log.warn(err);
            return;
        }

        Map<String, String> properties = getSpaceProperties(spaceId);
        if (null == properties) {
            StringBuilder err = new StringBuilder();
            err.append("Unable to get space properties for :");
            err.append(spaceId);
            log.error(err.toString());
            throw new DuplicationException(err.toString());
        }

        boolean success = doCreateSpace(spaceId, properties);
        if (!success) {
            String error = "Unable to create space " + spaceId;
            log.error(error);
            throw new DuplicationException(error);
        }
    }

    @Override
    public void updateSpace(String spaceId) {
        logDebug("Updating", spaceId);

        if (spaceId == null) {
            String err = "Space to update is null.";
            log.warn(err);
            return;
        }

        // Set space properties
        Map<String, String> properties = getSpaceProperties(spaceId);
        if (null == properties) {
            StringBuilder err = new StringBuilder();
            err.append("Unable to get space properties for :");
            err.append(spaceId);
            log.error(err.toString());
            throw new DuplicationException(err.toString());
        }

        boolean success = setSpaceProperties(spaceId, properties);
        if (!success) {
            String error = "Unable to set space properties: " + spaceId;
            log.error(error);
            throw new DuplicationException(error);
        }

        // Set Space access
        ContentStore.AccessType spaceAccess = getSpaceAccess(spaceId);
        if (null == spaceAccess) {
            StringBuilder err = new StringBuilder();
            err.append("Unable to get space access for :");
            err.append(spaceId);
            log.error(err.toString());
            throw new DuplicationException(err.toString());
        }

        success = setSpaceAccess(spaceId, spaceAccess);
        if (!success) {
            String err = "Unable to set space access for: {}";
            log.error(err, spaceId);
            throw new DuplicationException(err.toString());
        }
    }

    @Override
    public void deleteSpace(String spaceId) {
        logDebug("Deleting", spaceId);

        if (spaceId == null) {
            String err = "Space to delete is null.";
            log.warn(err);
            return;
        }

        boolean success = doDeleteSpace(spaceId);
        if (!success) {
            String err = "Unable to delete space: {}";
            log.error(err, spaceId);
            throw new DuplicationException(err);
        }
    }

    private boolean doCreateSpace(final String spaceId,
                                  final Map<String, String> properties) {
        try {
            return new StoreCaller<Boolean>(waitMillis) {
                protected Boolean doCall() throws Exception {
                    toStore.createSpace(spaceId, properties);
                    return true;
                }
            }.call();

        } catch (Exception e) {
            log.error("Error creating space: {}, due to: {}",
                      spaceId,
                      e.getMessage());
            return false;
        }
    }

    private Map<String, String> getSpaceProperties(final String spaceId) {
        try {
            return new StoreCaller<Map<String, String>>(waitMillis) {
                protected Map<String, String> doCall() throws Exception {
                    return fromStore.getSpaceProperties(spaceId);
                }
            }.call();

        } catch (Exception e) {
            log.error("Error getting space properties: {}, due to: {}",
                      spaceId,
                      e.getMessage());
            return null;
        }
    }

    private boolean setSpaceProperties(final String spaceId,
                                       final Map<String, String> properties) {
        try {
            return new StoreCaller<Boolean>(waitMillis) {
                protected Boolean doCall() throws Exception {
                    toStore.setSpaceProperties(spaceId, properties);
                    return true;
                }
            }.call();

        } catch (Exception e) {
            String err = "Error setting space properties for space: {}, " +
                "props: {}, due to: {}";
            log.error(err, new Object[]{spaceId, properties, e.getMessage()});
            return false;
        }
    }

    private ContentStore.AccessType getSpaceAccess(final String spaceId) {
        try {
            return new StoreCaller<ContentStore.AccessType>(waitMillis) {
                protected ContentStore.AccessType doCall() throws Exception {
                    return fromStore.getSpaceAccess(spaceId);
                }
            }.call();

        } catch (Exception e) {
            log.error("Error getting space access for: {}, due to: {}",
                      spaceId,
                      e.getMessage());
            return null;
        }
    }

    private boolean setSpaceAccess(final String spaceId,
                                   final ContentStore.AccessType accessType) {
        try {
            return new StoreCaller<Boolean>(waitMillis) {
                protected Boolean doCall() throws Exception {
                    toStore.setSpaceAccess(spaceId, accessType);
                    return true;
                }
            }.call();

        } catch (Exception e) {
            log.error("Error setting space access for: {}, due to: {}",
                      spaceId,
                      e.getMessage());
            return false;
        }
    }

    private boolean doDeleteSpace(final String spaceId) {
        try {
            return new StoreCaller<Boolean>(waitMillis) {
                protected Boolean doCall() throws Exception {
                    toStore.deleteSpace(spaceId);
                    return true;
                }
            }.call();

        } catch (Exception e) {
            log.error("Error deleting space: {}, due to: {}",
                      spaceId,
                      e.getMessage());
            return false;
        }
    }

    private void logDebug(String action, String spaceId) {
        log.debug("{} space {} from {} to {}",
                  new Object[]{action,
                               spaceId,
                               fromStore.getStorageProviderType(),
                               toStore.getStorageProviderType()});
    }

}