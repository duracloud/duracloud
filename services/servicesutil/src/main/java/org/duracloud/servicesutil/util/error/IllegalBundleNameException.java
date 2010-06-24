/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesutil.util.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * @author Andrew Woods
 *         Date: Dec 11, 2009
 */
public class IllegalBundleNameException extends DuraCloudRuntimeException {

    public IllegalBundleNameException(String msg) {
        super(msg);
    }
}
