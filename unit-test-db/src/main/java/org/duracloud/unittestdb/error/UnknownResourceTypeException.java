/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Andrew Woods
 *         Date: Mar 15, 2010
 */
public class UnknownResourceTypeException extends DuraCloudRuntimeException {

    public UnknownResourceTypeException(String msg) {
        super(msg);
    }
}
