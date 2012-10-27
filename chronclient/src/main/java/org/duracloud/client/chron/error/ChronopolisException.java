/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chron.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Andrew Woods
 *         Date: 10/25/12
 */
public class ChronopolisException extends DuraCloudRuntimeException {

    public ChronopolisException(String msg) {
        super(msg);
    }

    public ChronopolisException(String msg, Throwable t) {
        super(msg, t);
    }
}
