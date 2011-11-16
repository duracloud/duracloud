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

import java.util.HashMap;
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

        Map<String, String> spaceProperties = getSpaceProperties(spaceId);
        String spaceAccess = spaceProperties.get(PROPERTIES_SPACE_ACCESS);

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

        Map<String, String> spaceProperties = getSpaceProperties(spaceId);
        String spaceAccess = spaceProperties.get(PROPERTIES_SPACE_ACCESS);

        AccessType currentAccess = null;
        if(spaceAccess != null) {
            try {
                currentAccess = AccessType.valueOf(spaceAccess);
            } catch(IllegalArgumentException e) {
                currentAccess = null;
            }
        }

        if(!access.equals(currentAccess)) {
            spaceProperties.put(PROPERTIES_SPACE_ACCESS, access.name());
            setSpaceProperties(spaceId, spaceProperties);
        }
    }

    public Map<String, String> getSpaceACLs(String spaceId) {
        Map<String, String> acls = new HashMap<String, String>();
        Map<String, String> spaceProps = getSpaceProperties(spaceId);

        for (String name : spaceProps.keySet()) {
            if (name.startsWith(PROPERTIES_SPACE_ACL)) {
                acls.put(name, spaceProps.get(name));
            }
        }

        return acls;
    }

    public void setSpaceACLs(String spaceId, Map<String, String> spaceACLs) {
        Map<String, String> newProps = new HashMap<String, String>();
        Map<String, String> spaceProps = getSpaceProperties(spaceId);

        // add existing non ACLs properties
        for (String key : spaceProps.keySet()) {
            if (!key.startsWith(PROPERTIES_SPACE_ACL)) {
                newProps.put(key, spaceProps.get(key));
            }
        }

        // ONLY add new ACLs
        if (null != spaceACLs) {
            for (String key : spaceACLs.keySet()) {
                if (key.startsWith(PROPERTIES_SPACE_ACL)) {
                    newProps.put(key, spaceACLs.get(key));
                }
            }
        }

        // save
        setSpaceProperties(spaceId, newProps);
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) {
        log.debug("deleteSpace(" + spaceId + ")");
        throwIfSpaceNotExist(spaceId);

        Map<String, String> spaceProperties = getSpaceProperties(spaceId);
        spaceProperties.put("is-delete", "true");
        setSpaceProperties(spaceId, spaceProperties);

        SpaceDeleteWorker deleteThread = getSpaceDeleteWorker(spaceId);
        new Thread(deleteThread).start();
    }

    protected class SpaceDeleteWorker implements Runnable {
        protected final Logger log =
            LoggerFactory.getLogger(SpaceDeleteWorker.class);

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
                    log.debug("deleteContent(" + spaceId + ", " +
                              contentId + ") - count=" + count);

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
                log.debug("deleteSpaceContents(" + spaceId +
                          ") exceeded retries");

                Map<String, String> spaceProperties =
                    getSpaceProperties(spaceId);
                spaceProperties.put("delete-error",
                                    "Unable to delete all content items");
                setSpaceProperties(spaceId, spaceProperties);
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
