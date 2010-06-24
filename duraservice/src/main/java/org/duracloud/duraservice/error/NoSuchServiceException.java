/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author: Bill Branan
 * Date: Nov 13, 2009
 */
public class NoSuchServiceException extends DuraCloudCheckedException {

    private static final String messageKey =
        "duracloud.error.duraservice.nosuchservice";

    public NoSuchServiceException(int serviceId) {
        super("There is no service with service ID  " +  serviceId, messageKey);
        setArgs(new Integer(serviceId).toString());
    }
}