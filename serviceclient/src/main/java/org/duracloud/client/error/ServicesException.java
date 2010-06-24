/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * Exception thrown by the Services Manager.
 *
 * @author Bill Branan
 */
public class ServicesException extends DuraCloudCheckedException {

    private static final long serialVersionUID = 1L;

    public ServicesException (String message) {
        super(message);
    }

    public ServicesException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ServicesException(Throwable throwable) {
        super(throwable);
    }

}
