/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.duraservice.error.NoSuchDeployedServiceException;
import org.duracloud.duraservice.error.NoSuchServiceComputeInstanceException;
import org.duracloud.duraservice.error.NoSuchServiceException;

import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public interface ServiceResource {
    void configureManager(InputStream configXml);

    String getDeployedServices();

    String getAvailableServices();

    String getService(int serviceId) throws NoSuchServiceException;

    String getDeployedService(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException;

    String getDeployedServiceProps(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException;

    int deployService(int serviceId, String serviceHost, InputStream serviceXml)
        throws NoSuchServiceException, NoSuchServiceComputeInstanceException;

    void updateServiceConfig(int serviceId,
                             int deploymentId,
                             InputStream serviceXml)
        throws NoSuchDeployedServiceException;

    void undeployService(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException;
}
