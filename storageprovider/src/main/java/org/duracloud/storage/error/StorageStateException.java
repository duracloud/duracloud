/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * Exception thrown to indicate that a StorageException is due to the state
 * of the stored content.
 *
 * @author Bill Branan
 * Date: Jan 31, 2013
 */
public class StorageStateException extends StorageException {

    private static final long serialVersionUID = 1L;

    public StorageStateException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
