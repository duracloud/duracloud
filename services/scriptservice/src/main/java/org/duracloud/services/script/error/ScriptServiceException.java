/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.script.error;

import org.duracloud.services.common.error.ServiceRuntimeException;

/**
 * @author Bill Branan
 *         Date: Dec 11, 2009
 */
public class ScriptServiceException extends ServiceRuntimeException {

    public ScriptServiceException(String msg) {
        super(msg);
    }

    public ScriptServiceException(String msg, Throwable e) {
        super(msg, e);
    }

    public ScriptServiceException(Exception e) {
        super(e);
    }
}