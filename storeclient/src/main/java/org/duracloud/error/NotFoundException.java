/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

/**
 * Exception thrown when a requested space or content item does not exist.
 *
 * @author Bill Branan
 */
public class NotFoundException extends ContentStoreException {

    public NotFoundException (String message) {
        super(message);
    }

    public NotFoundException(String task, String spaceId, Exception e) {
        super(task, spaceId, e);
    }

    public NotFoundException(String task,
                             String spaceId,
                             String contentId,
                             Exception e) {
        super(task, spaceId, contentId, e);
    }

}