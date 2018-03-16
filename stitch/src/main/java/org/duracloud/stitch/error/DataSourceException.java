/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Andrew Woods
 * Date: 9/2/11
 */
public class DataSourceException extends DuraCloudRuntimeException {

    public DataSourceException(String msg, Throwable throwable) {
        super(msg, throwable);
    }
}
