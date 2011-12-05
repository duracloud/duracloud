/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

import org.duracloud.common.model.AclType;
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

    /**
     * This method adds all of the existing ACLs to the properties to be set.
     *
     * @param spaceId    - ID of the space
     * @param spaceProps - Updated space properties
     */
    public void setSpaceProperties(String spaceId,
                                   Map<String, String> spaceProps) {
        Map<String, String> allProps = new HashMap<String, String>();

        // Although allProps contains only ACLs at first, arg props will be added.
        Map<String, AclType> acls = getSpaceACLs(spaceId);
        for (String key : acls.keySet()) {
            allProps.put(key, acls.get(key).name());
        }

        // ONLY add non ACL properties
        if (null != spaceProps) {
            for (String key : spaceProps.keySet()) {
                if (!key.startsWith(PROPERTIES_SPACE_ACL)) {
                    allProps.put(key, spaceProps.get(key));
                }
            }
        }

        doSetSpaceProperties(spaceId, allProps);
    }

    /**
     * {@inheritDoc}
     */
    public AccessType getSpaceAccess(String spaceId) {
        log.debug("getSpaceAccess(" + spaceId + ")");

        throwIfSpaceNotExist(spaceId);

        Map<String, String> spaceProperties = getAllSpaceProperties(spaceId);
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

        Map<String, String> allProps = getAllSpaceProperties(spaceId);
        String spaceAccess = allProps.get(PROPERTIES_SPACE_ACCESS);

        AccessType currentAccess = null;
        if(spaceAccess != null) {
            try {
                currentAccess = AccessType.valueOf(spaceAccess);
            } catch(IllegalArgumentException e) {
                currentAccess = null;
            }
        }

        if(!access.equals(currentAccess)) {
            allProps.put(PROPERTIES_SPACE_ACCESS, access.name());
            doSetSpaceProperties(spaceId, allProps);
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
