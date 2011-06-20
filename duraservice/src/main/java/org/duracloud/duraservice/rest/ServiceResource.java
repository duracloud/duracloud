/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;

import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public interface ServiceResource {
    void configureManager(InputStream configXml);

    String getDeployedServices();

    String getAvailableServices();

    String getService(int serviceId) throws NotFoundException;

    String getDeployedService(int serviceId, int deploymentId)
        throws NotFoundException;

    String getDeployedServiceProps(int serviceId, int deploymentId)
        throws NotFoundException;

    int deployService(int serviceId, String serviceHost, InputStream serviceXml)
        throws NotFoundException, ServicesException;

    void updateServiceConfig(int serviceId,
                             int deploymentId,
                             InputStream serviceXml)
        throws NotFoundException, ServicesException;

    void undeployService(int serviceId, int deploymentId)
        throws NotFoundException, ServicesException;
}
