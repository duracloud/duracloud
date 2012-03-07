/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.exec.ServiceHandler;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: Bill Branan
 * Date: 3/2/12
 */
public abstract class BaseServiceHandler implements ServiceHandler {

    protected ContentStoreManager storeMgr;
    protected ServicesManager servicesMgr;

    protected Set<String> supportedActions;

    protected String status;

    public BaseServiceHandler() {
        supportedActions = new HashSet<String>();
    }

    @Override
    public void initialize(ContentStoreManager storeMgr,
                           ServicesManager servicesMgr) {
        this.storeMgr = storeMgr;
        this.servicesMgr = servicesMgr;
    }

    public abstract String getName();

    @Override
    public String getStatus() {
        return status;
    }

    public abstract void start();

    public abstract void stop();

    @Override
    public Set<String> getSupportedActions() {
        return supportedActions;
    }

    public abstract void performAction(String actionName,
                                       String actionParameters);


    /**
     * Looks in the list of available services to attempt to find a service
     * with the given name.
     *
     * @param serviceName the name of the service for which to search
     * @return the service if found
     * @throws NotFoundException if the service is not found
     * @throws ServicesException if an error occurs retrieving services
     */
    protected ServiceInfo findAvailableServiceByName(String serviceName)
        throws NotFoundException, ServicesException {

        List<ServiceInfo> availableServices =
            servicesMgr.getAvailableServices();
        for(ServiceInfo service : availableServices) {
            if(serviceName.equals(service.getDisplayName())) {
                return service;
            }
        }

        throw new NotFoundException("Could not find service by name " +
                                    serviceName +
                                    " among available services");
    }

    /**
     * Looks in the list of deployed services to attempt to find a service
     * with the given name.
     *
     * @param serviceName the name of the service for which to search
     * @return the service if found
     * @throws NotFoundException if the service is not found
     * @throws ServicesException if an error occurs retrieving services
     */
    protected ServiceInfo findDeployedServiceByName(String serviceName)
        throws NotFoundException, ServicesException {

        List<ServiceInfo> deployedServices =
            servicesMgr.getDeployedServices();
        for(ServiceInfo service : deployedServices) {
            if(serviceName.equals(service.getDisplayName())) {
                return service;
            }
        }

        throw new NotFoundException("Could not find service by name " +
                                    serviceName +
                                    " among deployed services");
    }


    /**
     * Determines the deployment host to which a service should be deployed.
     *
     * @param service which will be deployed
     * @return deployment host
     */
    protected String getDeploymentHost(ServiceInfo service) {
        // Assume a single deployment option
        DeploymentOption depOption = service.getDeploymentOptions().get(0);
        return depOption.getHostname();
    }

}
