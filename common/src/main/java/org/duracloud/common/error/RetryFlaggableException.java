/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 *
 * @author Bill Branan
 */
public class RetryFlaggableException extends DuraCloudRuntimeException {

    private static final long serialVersionUID = 1L;

    public static final boolean RETRY = true;
    public static final boolean NO_RETRY = false;

    protected boolean retry;


    public RetryFlaggableException(String message, Throwable throwable, boolean retry) {
        super(message, throwable);
        this.retry = retry;
    }

    public boolean isRetry() {
        return retry;
    }

}
