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

import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Apr 20, 2010
 */
public abstract class StorageProviderBase implements StorageProvider {

    protected final Logger log = LoggerFactory.getLogger(StorageProviderBase.class);

    protected abstract void throwIfSpaceNotExist(String spaceId);

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

}
