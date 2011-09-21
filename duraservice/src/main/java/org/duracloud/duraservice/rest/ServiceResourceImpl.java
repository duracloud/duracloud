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
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.duraservice.mgmt.LocalServicesManager;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Provides interaction with content
 *
 * @author Bill Branan
 */
public class ServiceResourceImpl implements ServiceResource {

    private static final Logger log = LoggerFactory.getLogger(
        ServiceResourceImpl.class);

    private LocalServicesManager servicesManager;

    public ServiceResourceImpl(LocalServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    @Override
    public void configureManager(InputStream configXml) {
        servicesManager.configure(configXml);
    }

    @Override
    public boolean isConfigured() {
        return servicesManager.isConfigured();
    }

    @Override
    public String getDeployedServices() {
        List<ServiceInfo> deployedServices = doGetDeployedServices();
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        return configDoc.getServiceListAsXML(deployedServices);
    }

    private List<ServiceInfo> doGetDeployedServices() {
        List<ServiceInfo> deployedServices;
        try {
            deployedServices = servicesManager.getDeployedServices();

        } catch (ServicesException e) {
            log.warn("Error getDeployedServices: {}", e.getMessage());
            deployedServices = new ArrayList<ServiceInfo>();
        }
        return deployedServices;
    }

    @Override
    public String getAvailableServices() {
        List<ServiceInfo> availableServices = doGetAvailableServices();
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        return configDoc.getServiceListAsXML(availableServices);
    }

    private List<ServiceInfo> doGetAvailableServices() {
        List<ServiceInfo> services;
        try {
            services = servicesManager.getAvailableServices();

        } catch (ServicesException e) {
            log.warn("Error getAvailableServices: {}", e.getMessage());
            services = new ArrayList<ServiceInfo>();
        }
        return services;
    }

    @Override
    public String getService(int serviceId) throws NotFoundException {
        ServiceInfo service = null;
        try {
            service = servicesManager.getService(serviceId);
        } catch (ServicesException e) {
            log.error("Error getService({})", serviceId);
            throw new DuraCloudRuntimeException(e);
        }
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        return configDoc.getServiceAsXML(service);
    }

    @Override
    public String getDeployedService(int serviceId, int deploymentId)
        throws NotFoundException {
        ServiceInfo service = null;
        try {
            service = servicesManager.getDeployedService(serviceId,
                                                         deploymentId);
        } catch (ServicesException e) {
            log.error("Error getDeployedService({}, {})",
                      serviceId,
                      deploymentId);
            throw new DuraCloudRuntimeException(e);
        }
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        return configDoc.getServiceAsXML(service);
    }

    @Override
    public String getDeployedServiceProps(int serviceId, int deploymentId)
        throws NotFoundException {
        Map<String, String> serviceProperties = null;
        try {
            serviceProperties = servicesManager.getDeployedServiceProps(
                serviceId,
                deploymentId);

        } catch (ServicesException e) {
            log.error("Error getDeployedServiceProps({}, {})",
                      serviceId,
                      deploymentId);
            throw new DuraCloudRuntimeException(e);
        }

        //TODO: Update to use a more well structure serialization
        return SerializationUtil.serializeMap(serviceProperties);
    }

    @Override
    public int deployService(int serviceId,
                                     String serviceHost,
                                     InputStream serviceXml)
        throws NotFoundException, ServicesException {
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        ServiceInfo service = configDoc.getService(serviceXml);
        return servicesManager.deployService(serviceId,
                                            serviceHost,
                                            service.getUserConfigVersion(),
                                            service.getUserConfigModeSets());
    }

    @Override
    public void updateServiceConfig(int serviceId,
                                           int deploymentId,
                                           InputStream serviceXml)
        throws NotFoundException, ServicesException {
        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        ServiceInfo service = configDoc.getService(serviceXml);
        servicesManager.updateServiceConfig(serviceId,
                                           deploymentId,
                                           service.getUserConfigVersion(),
                                           service.getUserConfigModeSets());
    }

    @Override
    public void undeployService(int serviceId, int deploymentId)
        throws NotFoundException, ServicesException {
        servicesManager.undeployService(serviceId, deploymentId);
    }

}
