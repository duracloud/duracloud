/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

/**
 * Exception thrown when a space or content ID is invalid.
 *
 * @author Bill Branan
 */
public class InvalidIdException extends ContentStoreException {

    public InvalidIdException(String message) {
        super(message);
    }

    public InvalidIdException(String task, String spaceId, Exception e) {
        super(task, spaceId, e);
    }

    public InvalidIdException(String task,
                              String spaceId,
                              String contentId,
                              Exception e) {
        super(task, spaceId, contentId, e);
    }

    public InvalidIdException(String task,
                              String srcSpaceId,
                              String srcContentId,
                              String destSpaceId,
                              String destContentId,
                              Exception e) {
        super(task, srcSpaceId, srcContentId, destSpaceId, destContentId, e);
    }
}