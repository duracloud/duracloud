/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hellowebappwrapper.error;

import org.duracloud.services.common.error.ServiceRuntimeException;

/**
 * @author Andrew Woods
 *         Date: Dec 10, 2009
 */
public class WebappWrapperException extends ServiceRuntimeException {

    public WebappWrapperException(String msg) {
        super(msg);
    }

    public WebappWrapperException(String msg, Throwable e) {
        super(msg, e);
    }

    public WebappWrapperException(Exception e) {
        super(e);
    }
}