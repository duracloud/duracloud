/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.common.error;

/**
 * @author Andrew Woods
 *         Date: Nov 30, 2009
 */
public class ServiceRuntimeException extends RuntimeException {

    private int code = -1;

    public ServiceRuntimeException(String message) {
        super(message);
    }

    public ServiceRuntimeException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ServiceRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServiceRuntimeException(String message,
                                   Throwable throwable,
                                   int code) {
        super(message, throwable);
        this.code = code;
    }

    public ServiceRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public ServiceRuntimeException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
