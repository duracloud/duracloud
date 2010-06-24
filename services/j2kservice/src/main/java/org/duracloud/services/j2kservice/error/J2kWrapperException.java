/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.j2kservice.error;

import org.duracloud.services.common.error.ServiceRuntimeException;

/**
 * @author Andrew Woods
 *         Date: Dec 20, 2009
 */
public class J2kWrapperException extends ServiceRuntimeException {

    public J2kWrapperException(String msg) {
        super(msg);
    }

    public J2kWrapperException(String msg, Throwable e) {
        super(msg, e);
    }

    public J2kWrapperException(Exception e) {
        super(e);
    }
}