/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * Exception thrown by StorageProvider implementations when a requested
 * space or content item does not exist.
 *
 * @author Bill Branan
 */
public class NotFoundException extends StorageException {

    private static final long serialVersionUID = 1L;

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    @Override
    public boolean isRetry() {
        return false;
    }

}