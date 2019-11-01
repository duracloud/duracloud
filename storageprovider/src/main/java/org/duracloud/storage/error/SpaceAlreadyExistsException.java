/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * Exception thrown on space creation if the space already exists
 *
 * @author Bill Branan
 * Date: Oct 30, 2019
 */
public class SpaceAlreadyExistsException extends StorageException {

    public SpaceAlreadyExistsException(String spaceId) {
        super("Error: Space already exists: " + spaceId);
    }

}
