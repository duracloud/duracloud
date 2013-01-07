/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.common.model.AclType;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.error.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.duracloud.storage.error.StorageException.NO_RETRY;

/**
 * @author: Bill Branan
 * Date: Apr 20, 2010
 */
public abstract class StorageProviderBase implements StorageProvider {

    protected final Logger log = LoggerFactory.getLogger(StorageProviderBase.class);

    protected abstract boolean spaceExists(String spaceId);
    protected abstract void removeSpace(String spaceId);
    protected abstract Map<String, String> getAllSpaceProperties(String spaceId);
    protected abstract void doSetSpaceProperties(String spaceId,
                                                 Map<String, String> spaceProps);

    /**
     * This method returns all of the space properties EXCEPT the ACLs
     *
     * @param spaceId - ID of the space
     * @return map of space properties
     */
    public Map<String, String> getSpaceProperties(String spaceId) {
        Map<String, String> spaceProps = new HashMap<String, String>();
        Map<String, String> allProps = getAllSpaceProperties(spaceId);

        // ONLY include non-ACL properties.
        for (String name : allProps.keySet()) {
            if (!name.startsWith(PROPERTIES_SPACE_ACL)) {
                spaceProps.put(name, allProps.get(name));
            }
        }

        return spaceProps;
    }

    protected void setNewSpaceProperties(String spaceId,
                                         Map<String, String> spaceProperties) {
        boolean success = false;
        int maxLoops = 6;
        for (int loops = 0; !success && loops < maxLoops; loops++) {
            try {
                doSetSpaceProperties(spaceId, spaceProperties);
                success = true;
            } catch (NotFoundException e) {
                success = false;
            }
        }

        if (!success) {
            throw new StorageException(
                "Properties for space " + spaceId + " could not be created. " +
                    "The space cannot be found.");
        }
    }

    public Map<String, AclType> getSpaceACLs(String spaceId) {
        Map<String, AclType> acls = new HashMap<String, AclType>();
        Map<String, String> allProps = getAllSpaceProperties(spaceId);

        for (String name : allProps.keySet()) {
            if (name.startsWith(PROPERTIES_SPACE_ACL)) {
                String val = allProps.get(name);
                try {
                    acls.put(name, AclType.valueOf(val));
                    
                } catch (IllegalArgumentException e) {
                    log.error("Invalid ACL: {}, space: {}, error: {}",
                              new Object[]{val, spaceId, e});
                }
            }
        }

        return acls;
    }

    public void setSpaceACLs(String spaceId, Map<String, AclType> spaceACLs) {
        Map<String, String> newProps = new HashMap<String, String>();
        Map<String, String> spaceProps = getSpaceProperties(spaceId);

        // add existing non ACLs properties
        newProps.putAll(spaceProps);

        // ONLY add new ACLs
        if (null != spaceACLs) {
            for (String key : spaceACLs.keySet()) {
                if (key.startsWith(PROPERTIES_SPACE_ACL)) {
                    AclType acl = spaceACLs.get(key);
                    newProps.put(key, acl.name());
                }
            }
        }

        // save
        doSetSpaceProperties(spaceId, newProps);
    }

    protected void throwIfSpaceExists(String spaceId) {
        if (spaceExists(spaceId)) {
            String msg = "Error: Space already exists: " + spaceId;
            throw new StorageException(msg, NO_RETRY);
        }
    }

    protected void throwIfSpaceNotExist(String spaceId) {
        throwIfSpaceNotExist(spaceId, true);
    }

    protected void throwIfSpaceNotExist(String spaceId, boolean wait) {
        if (!spaceExists(spaceId)) {
            String msg = "Error: Space does not exist: " + spaceId;
            if (wait) {
                waitForSpaceAvailable(spaceId);
                if (!spaceExists(spaceId)) {
                    throw new NotFoundException(msg);
                }
            } else {
                throw new NotFoundException(msg);
            }
        }
    }

    private void waitForSpaceAvailable(String spaceId) {
        int maxLoops = 6;
        for (
            int loops = 0; !spaceExists(spaceId) && loops < maxLoops; loops++) {
            try {
                log.debug(
                    "Waiting for space " + spaceId + " to be available, loop " +
                        loops);
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteSpace(String spaceId) {
        log.debug("deleteSpace(" + spaceId + ")");
        throwIfSpaceNotExist(spaceId);

        Map<String, String> allProps = getAllSpaceProperties(spaceId);
        allProps.put("is-delete", "true");
        doSetSpaceProperties(spaceId, allProps);

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

                Map<String, String> allProps = getAllSpaceProperties(spaceId);
                allProps.put("delete-error", "Unable to delete all contents");
                doSetSpaceProperties(spaceId, allProps);
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
