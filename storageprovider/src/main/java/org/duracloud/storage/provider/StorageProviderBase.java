/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Apr 20, 2010
 */
public abstract class StorageProviderBase implements StorageProvider {

    protected final Logger log = LoggerFactory.getLogger(StorageProviderBase.class);

    protected abstract void throwIfSpaceNotExist(String spaceId);
    protected abstract void removeSpace(String spaceId);

    /**
     * {@inheritDoc}
     */
    public AccessType getSpaceAccess(String spaceId) {
        log.debug("getSpaceAccess(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        Map<String, String> spaceMetadata = getSpaceMetadata(spaceId);
        String spaceAccess = spaceMetadata.get(METADATA_SPACE_ACCESS);

        if(spaceAccess == null) {
            // No space access set, set to CLOSED
            setSpaceAccess(spaceId, AccessType.CLOSED);
            spaceAccess = AccessType.CLOSED.name();
        }

        return AccessType.valueOf(spaceAccess);
    }

    /**
     * {@inheritDoc}
     */
    public void setSpaceAccess(String spaceId, AccessType access) {
        log.debug("setSpaceAccess(" + spaceId + ", " + access.name() + ")");

        throwIfSpaceNotExist(spaceId);

        Map<String, String> spaceMetadata = getSpaceMetadata(spaceId);
        String spaceAccess = spaceMetadata.get(METADATA_SPACE_ACCESS);

        AccessType currentAccess = null;
        if(spaceAccess != null) {
            try {
                currentAccess = AccessType.valueOf(spaceAccess);
            } catch(IllegalArgumentException e) {
                currentAccess = null;
            }
        }

        if(!access.equals(currentAccess)) {
            spaceMetadata.put(METADATA_SPACE_ACCESS, access.name());
            setSpaceMetadata(spaceId, spaceMetadata);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) {
        log.debug("deleteSpace(" + spaceId + ")");
        throwIfSpaceNotExist(spaceId);

        Map<String, String> spaceMetadata = getSpaceMetadata(spaceId);
        spaceMetadata.put("is-delete", "true");
        setSpaceMetadata(spaceId, spaceMetadata);

        SpaceDeleteWorker deleteThread = getSpaceDeleteWorker(spaceId);
        new Thread(deleteThread).start();
    }

    protected class SpaceDeleteWorker implements Runnable {
        protected final Logger log = LoggerFactory.getLogger(SpaceDeleteWorker.class);

        private String spaceId;

        public SpaceDeleteWorker(String spaceId) {
            this.spaceId = spaceId;
        }

        @Override
        public void run() {
            Iterator<String> contents = getSpaceContents(spaceId, null);
            int count = 0;

            while(contents.hasNext() && count++ < 5) {
                try{
                    Thread.sleep((long)Math.pow(2,count) * 100);
                } catch(InterruptedException e) {
                }

                while(contents.hasNext()) {
                    String contentId = contents.next();
                    log.debug("deleteContent(" + spaceId + ", " + contentId + ") - count=" + count);

                    try {
                        deleteContent(spaceId, contentId);
                    } catch(Exception e) {
                        log.error("Error deleting content " + contentId +
                            " in space " + spaceId, e);
                    }
                }
                contents = getSpaceContents(spaceId, null);
            }

            if(contents.hasNext()) {
                log.debug("deleteSpaceContents(" + spaceId + ") exceeded retries");

                Map<String, String> spaceMetadata = getSpaceMetadata(spaceId);
                spaceMetadata.put("delete-error", "Unable to delete all content items");
                setSpaceMetadata(spaceId, spaceMetadata);
            }
            else {
                log.debug("removeSpace(" + spaceId + ")");
                removeSpace(spaceId);
            }
        }
    }

    public SpaceDeleteWorker getSpaceDeleteWorker(String spaceId) {
        return new SpaceDeleteWorker(spaceId);
    }
}
