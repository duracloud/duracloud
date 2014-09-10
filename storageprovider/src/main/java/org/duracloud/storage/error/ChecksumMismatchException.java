/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * Exception thrown when there is a mismatch in checksum values
 *
 * @author Bill Branan
 *         Date: 9/4/14
 */
public class ChecksumMismatchException extends StorageException {

    private static final long serialVersionUID = 1L;

    public ChecksumMismatchException(String message,
                                     boolean retry) {
        super(message, retry);
    }

    public ChecksumMismatchException(String message,
                                     Throwable throwable) {
        super(message, throwable);
    }

    public ChecksumMismatchException(String message,
                                     Throwable throwable,
                                     boolean retry) {
        super(message, throwable, retry);
    }

}
