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
 * Date: Nov 12, 2009
 */
public class NoSuchDeployedServiceException extends DuraCloudCheckedException {

    private static final String messageKey =
        "duracloud.error.duraservice.nosuchdeployedservice";

    public NoSuchDeployedServiceException(int serviceId, int deploymentId) {
        super("There is no deployed service with service ID  " +  serviceId +
              " and deployment ID " + deploymentId, messageKey);
        setArgs(new Integer(serviceId).toString(),
                new Integer(deploymentId).toString());
    }
}