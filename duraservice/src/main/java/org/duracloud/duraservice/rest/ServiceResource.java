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
    public void configureManager(InputStream configXml);

    public boolean isConfigured();

    public String getDeployedServices();

    public String getAvailableServices();

    public String getService(int serviceId) throws NotFoundException;

    public String getDeployedService(int serviceId, int deploymentId)
        throws NotFoundException;

    public String getDeployedServiceProps(int serviceId, int deploymentId)
        throws NotFoundException;

    public int deployService(int serviceId,
                             String serviceHost,
                             InputStream serviceXml)
        throws NotFoundException, ServicesException;

    public void updateServiceConfig(int serviceId,
                             int deploymentId,
                             InputStream serviceXml)
        throws NotFoundException, ServicesException;

    public void undeployService(int serviceId, int deploymentId)
        throws NotFoundException, ServicesException;

}
