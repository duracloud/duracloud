/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.client.error.NotFoundException;
import org.duracloud.client.error.ServicesException;
import org.duracloud.common.model.Securable;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.UserConfig;

import java.util.List;
import java.util.Map;

/**
 * Allows for communication with DuraService
 *
 * @author Bill Branan
 */
public interface ServicesManager extends Securable {

    public String getBaseURL();

    /**
     * Provides a listing of available services, that is, services which
     * can be deployed.
     *
     * @return List of available services
     * @throws ServicesException if available services cannot be retrieved
     */
    public List<ServiceInfo> getAvailableServices() throws ServicesException;

    /**
     * Provides a listing of all deployed services.
     *
     * @return List of deployed services
     * @throws ServicesException if deployed services cannot be retrieved
     */
    public List<ServiceInfo> getDeployedServices() throws ServicesException;

    /**
     * Gets a service. This includes configuration options and deployment
     * options for a potential deployment as well as a list of all current
     * deployments of this service. There is no guarantee with this
     * call that the service returned can be deployed. Use getAvailableServices
     * to get the list of services which are available for deployment.
     *
     * @param serviceId the ID of the service to retrieve
     * @return a service
     * @throws NotFoundException if the service cannot be found
     * @throws ServicesException if the service cannot be retrieved
     */
    public ServiceInfo getService(int serviceId)
        throws NotFoundException, ServicesException;

    /**
     * Gets a deployed service. This includes the selected configuration
     * for the deployment as well as the user configuration options for
     * potential reconfiguration.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the service deployment to retrieve
     * @return a service which has been deployed
     * @throws NotFoundException if either the service or deployment cannot be found
     * @throws ServicesException if the service cannot be retrieved
     */
    public ServiceInfo getDeployedService(int serviceId, int deploymentId)
        throws NotFoundException, ServicesException;

    /**
     * Gets runtime properties for a deployed service.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the service deployment to retrieve
     * @return a map of service properties
     * @throws NotFoundException if either the service or deployment cannot be found
     * @throws ServicesException if the service properties cannot be retrieved
     */
    public Map<String, String> getDeployedServiceProps(int serviceId,
                                                       int deploymentId)
        throws NotFoundException, ServicesException;

    /**
     * Deploys a service.
     *
     * @param serviceId the ID of the service to deploy
     * @param userConfigVersion the version of the user configuration
     * @param userConfigs a list of user configuration options
     * @param deploymentSelection the selected deployment option
     * @return the deploymentID of the newly deployed service
     * @throws NotFoundException if the service cannot be found
     * @throws ServicesException if the service cannot be deployed
     */
    public int deployService(int serviceId,
                             String userConfigVersion,
                             List<UserConfig> userConfigs,
                             DeploymentOption deploymentSelection)
        throws NotFoundException, ServicesException;

    /**
     * Updates the configuration of a deployed service.
     *
     * @param serviceId the ID of the service to update
     * @param deploymentId the ID of the service deployment to update
     * @param userConfigVersion the version of the user configuration
     * @param userConfigs updated user configuration options
     * @throws NotFoundException if either the service or deployment cannot be found
     * @throws ServicesException if the service configuration cannot be updated
     */
    public void updateServiceConfig(int serviceId,
                                    int deploymentId,
                                    String userConfigVersion,
                                    List<UserConfig> userConfigs)
        throws NotFoundException, ServicesException;

    /**
     * UnDeploys a service.
     *
     * @param serviceId the ID of the service to undeploy
     * @param deploymentId the ID of the service deployment to undeploy
     * @throws NotFoundException if either the service or deployment cannot be found
     * @throws ServicesException if the service cannot be undeployed
     */
    public void undeployService(int serviceId,
                                int deploymentId)
        throws NotFoundException, ServicesException;

}
