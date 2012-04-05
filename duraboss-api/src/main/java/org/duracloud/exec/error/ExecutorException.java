/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * Exception thrown is there is an error encountered when working with the
 * Executor.
 *
 * @author: Bill Branan
 * Date: 4/4/12
 */
public class ExecutorException extends DuraCloudRuntimeException {
    private static final long serialVersionUID = 1L;

    public ExecutorException(String message) {
        super(message);
    }

    public ExecutorException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ExecutorException(Throwable throwable) {
        super(throwable);
    }
}
