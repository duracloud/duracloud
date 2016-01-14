/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.error.RetryFlaggableException;

/**
 * Exception thrown by StorageProvider implementations.
 *
 * @author Bill Branan
 */
public class StorageException extends RetryFlaggableException {

    private static final long serialVersionUID = 1L;


    public StorageException(String message) {
        this(message, RetryFlaggableException.NO_RETRY);
    }

    public StorageException(String message, boolean retry) {
        super(message, null, retry);
    }

    public StorageException(String message, Throwable throwable) {
        this(message, throwable, RetryFlaggableException.NO_RETRY);
    }

    public StorageException(String message, Throwable throwable, boolean retry) {
        super(message, throwable, retry);
    }

    public StorageException(Throwable throwable) {
        this(throwable, RetryFlaggableException.NO_RETRY);
    }

    public StorageException(Throwable throwable, boolean retry) {
        this(null, throwable,retry);
        this.retry = retry;
    }

    public boolean isRetry() {
        return retry;
    }

}
