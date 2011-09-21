/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.cloudsync.error;

import org.duracloud.services.common.error.ServiceRuntimeException;

/**
 * @author Andrew Woods
 *         Date: 9/20/11
 */
public class CloudSyncWrapperException extends ServiceRuntimeException {

    public CloudSyncWrapperException(String msg) {
        super(msg);
    }
}
