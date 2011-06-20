/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.error;

import org.duracloud.serviceapi.error.ServicesException;

/**
 * @author: Bill Branan
 * Date: Nov 11, 2009
 */
public class NoSuchServiceComputeInstanceException extends ServicesException {

    private static final String messageKey =
        "duracloud.error.duraservice.nosuchservicecomputeinstance";

    public NoSuchServiceComputeInstanceException(String instanceHost) {
        super("No service compute instance is available on host " +  instanceHost,
              messageKey);
        setArgs(instanceHost);
    }
}
