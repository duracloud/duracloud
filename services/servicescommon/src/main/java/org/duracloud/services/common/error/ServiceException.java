/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.common.error;

public class ServiceException
        extends Exception {

    private static final long serialVersionUID = 2901277411381377963L;

    private int code = -1;

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, int code) {
        super(message);
        this.code = code;
    }

    public ServiceException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServiceException(String message, Throwable throwable, int code) {
        super(message, throwable);
        this.code = code;
    }

    public ServiceException(Throwable throwable) {
        super(throwable);
    }

    public ServiceException(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
