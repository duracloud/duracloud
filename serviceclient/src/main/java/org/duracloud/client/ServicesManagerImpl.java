/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.client.error.NotFoundException;
import org.duracloud.client.error.ServicesException;
import org.duracloud.client.error.UnexpectedResponseException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.Securable;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.duracloud.serviceconfig.user.UserConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Allows for communication with DuraService
 *
 * @author Bill Branan
 */
public class ServicesManagerImpl implements ServicesManager, Securable {

    private static enum ServicesList {
        DEPLOYED ("deployed"),
        AVAILABLE ("available");

        public String type;

        ServicesList(String type) {
            this.type = type;
        }
    }

    private static final String DEFAULT_CONTEXT = "duraservice";

    private String baseURL = null;

    private RestHttpHelper restHelper;

    public ServicesManagerImpl(String host, String port) {
        this(host, port, DEFAULT_CONTEXT);
    }

    public ServicesManagerImpl(String host, String port, String context) {
        if (host == null || host.equals("")) {
            throw new IllegalArgumentException("Host must be a valid " +
                                               "server host name");
        }

        if (context == null) {
            context = DEFAULT_CONTEXT;
        }

        if (port == null || port.equals("")) {
            baseURL = "http://" + host + "/" + context;
        } else if (port.equals("443")) {
            baseURL = "https://" + host + "/" + context;
        } else {
            baseURL = "http://" + host + ":" + port + "/" + context;
        }
    }

    public String getBaseURL() {
        return baseURL;
    }

    private String buildURL(String relativeURL) {
        return baseURL + relativeURL;
    }

    private String buildGetServicesURL(ServicesList servicesList) {
        if(servicesList == null) {
            servicesList = ServicesList.AVAILABLE;
        }
        return buildURL("/services?show=" + servicesList.type);
    }

    private String buildServiceURL(int serviceId) {
        return buildURL("/" + serviceId);
    }

    private String buildDeployedServiceURL(int serviceId,
                                           int deploymentId) {
        return buildURL("/" + serviceId + "/" + deploymentId);
    }

    private String buildDeployedServicePropsURL(int serviceId,
                                                int deploymentId) {
        return buildURL("/" + serviceId + "/" + deploymentId + "/properties");
    }

    /**
     * Provides a listing of available services, that is, services which
     * can be deployed.
     *
     * @return List of available services
     * @throws org.duracloud.client.error.ServicesException if available services cannot be retrieved
     */
    public List<ServiceInfo> getAvailableServices() throws ServicesException {
        return getServices(buildGetServicesURL(ServicesList.AVAILABLE));
    }

    /**
     * Provides a listing of all deployed services.
     *
     * @return List of deployed services
     * @throws org.duracloud.client.error.ServicesException if deployed services cannot be retrieved
     */
    public List<ServiceInfo> getDeployedServices() throws ServicesException {
        return getServices(buildGetServicesURL(ServicesList.DEPLOYED));
    }

    private List<ServiceInfo> getServices(String url) throws ServicesException {
        try {
            HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream servicesXml = response.getResponseStream();

            ServicesConfigDocument configDoc = new ServicesConfigDocument();
            return configDoc.getServiceList(servicesXml);
        } catch (Exception e) {
            throw new ServicesException("Could not get spaces due to: " +
                                        e.getMessage(), e);
        }
    }

    /**
     * Gets a service. This includes configuration options and deployment
     * options for a potential deployment as well as a list of all current
     * deployments of this service. There is no guarantee with this
     * call that the service returned can be deployed. Use getAvailableServices
     * to get the list of services which are available for deployment.
     *
     * @param serviceId the ID of the service to retrieve
     * @return a service
     * @throws org.duracloud.client.error.NotFoundException if the service cannot be found
     * @throws org.duracloud.client.error.ServicesException if the service cannot be retrieved
     */
    public ServiceInfo getService(int serviceId)
        throws NotFoundException, ServicesException {
        String url = buildServiceURL(serviceId);
        try {
            HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream serviceXml = response.getResponseStream();

            ServicesConfigDocument configDoc = new ServicesConfigDocument();
            return configDoc.getService(serviceXml);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicesException("Could not get service" + serviceId +
                                        " due to: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a deployed service. This includes the selected configuration
     * for the deployment as well as the user configuration options for
     * potential reconfiguration.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the service deployment to retrieve
     * @return a service which has been deployed
     * @throws org.duracloud.client.error.NotFoundException if either the service or deployment cannot be found
     * @throws org.duracloud.client.error.ServicesException if the service cannot be retrieved
     */
    public ServiceInfo getDeployedService(int serviceId, int deploymentId)
        throws NotFoundException, ServicesException {
        String url = buildDeployedServiceURL(serviceId, deploymentId);
        try {
            HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            InputStream serviceXml = response.getResponseStream();

            ServicesConfigDocument configDoc = new ServicesConfigDocument();
            return configDoc.getService(serviceXml);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            String error = "Could not get deployment " + deploymentId +
                " for service " + serviceId + " due to: " + e.getMessage();
            throw new ServicesException(error, e);
        }
    }

    /**
     * Gets runtime properties for a deployed service.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the service deployment to retrieve
     * @return a map of service properties
     * @throws org.duracloud.client.error.NotFoundException if either the service or deployment cannot be found
     * @throws org.duracloud.client.error.ServicesException if the service properties cannot be retrieved
     */
    public Map<String, String> getDeployedServiceProps(int serviceId,
                                                       int deploymentId)
        throws NotFoundException, ServicesException {
        String url = buildDeployedServicePropsURL(serviceId, deploymentId);
        try {
            HttpResponse response = getRestHelper().get(url);
            checkResponse(response, HttpStatus.SC_OK);
            String responseBody = response.getResponseBody();
            return SerializationUtil.deserializeMap(responseBody);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            String error = "Could not get properties for deployment " +
                deploymentId + " of service " + serviceId +
                " due to: " + e.getMessage();
            throw new ServicesException(error, e);
        }
    }

    /**
     * Deploys a service.
     *
     * @param serviceId the ID of the service to deploy
     * @param userConfigVersion the version of the user configuration
     * @param userConfigs a list of user configuration options
     * @param deploymentSelection the selected deployment option
     * @return the deploymentID of the newly deployed service
     * @throws org.duracloud.client.error.NotFoundException if the service cannot be found
     * @throws org.duracloud.client.error.ServicesException if the service cannot be deployed
     */
    public int deployService(int serviceId,
                             String userConfigVersion,
                             List<UserConfig> userConfigs,
                             DeploymentOption deploymentSelection)
        throws NotFoundException, ServicesException {
        String url = buildServiceURL(serviceId);

        String hostName = null;
        if(deploymentSelection != null) {
            hostName = deploymentSelection.getHostname();
            if(hostName != null) {
                url += ("?serviceHost=" + hostName);
            }
        }

        // Create service for deployment
        ServiceInfo serviceToDeploy = new ServiceInfo();
        serviceToDeploy.setUserConfigs(userConfigs);
        serviceToDeploy.setUserConfigVersion(userConfigVersion);

        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        String serviceXml = configDoc.getServiceAsXML(serviceToDeploy);

        try {
            HttpResponse response = getRestHelper().put(url, serviceXml, null);
            checkResponse(response, HttpStatus.SC_CREATED);
            String responseBody =
                response.getResponseHeader("Location").getValue();
            return extractDeploymentId(responseBody);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicesException("Could not deploy service " + serviceId +
                                        " to host " + hostName +
                                        " due to: " + e.getMessage(), e);
        }
    }

    private int extractDeploymentId(String deploymentUrl) {
        String[] deploymentUrlSplit = deploymentUrl.split("/");
        String depId = deploymentUrlSplit[deploymentUrlSplit.length-1];
        return Integer.valueOf(depId);
    }

    /**
     * Updates the configuration of a deployed service.
     *
     * @param serviceId the ID of the service to update
     * @param deploymentId the ID of the service deployment to update
     * @param userConfigVersion the version of the user configuration
     * @param userConfigs updated user configuration options
     * @throws org.duracloud.client.error.NotFoundException if either the service or deployment cannot be found
     * @throws org.duracloud.client.error.ServicesException if the service configuration cannot be updated
     */
    public void updateServiceConfig(int serviceId,
                                    int deploymentId,
                                    String userConfigVersion,
                                    List<UserConfig> userConfigs)
        throws NotFoundException, ServicesException {
        String url = buildDeployedServiceURL(serviceId, deploymentId);

        // Create service for config update
        ServiceInfo serviceForConfig = new ServiceInfo();
        serviceForConfig.setUserConfigs(userConfigs);
        serviceForConfig.setUserConfigVersion(userConfigVersion);

        ServicesConfigDocument configDoc = new ServicesConfigDocument();
        String serviceXml = configDoc.getServiceAsXML(serviceForConfig);

        try {
            HttpResponse response = getRestHelper().post(url, serviceXml, null);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicesException("Could not undeploy service " + serviceId +
                                        " due to: " + e.getMessage(), e);
        }
    }

    /**
     * UnDeploys a service.
     *
     * @param serviceId the ID of the service to undeploy
     * @param deploymentId the ID of the service deployment to undeploy
     * @throws org.duracloud.client.error.NotFoundException if either the service or deployment cannot be found
     * @throws org.duracloud.client.error.ServicesException if the service cannot be undeployed
     */
    public void undeployService(int serviceId,
                                int deploymentId)
        throws NotFoundException, ServicesException {
        String url = buildDeployedServiceURL(serviceId, deploymentId);
        try {
            HttpResponse response = getRestHelper().delete(url);
            checkResponse(response, HttpStatus.SC_OK);
        } catch (NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new ServicesException("Could not undeploy service " + serviceId +
                                        " due to: " + e.getMessage(), e);
        }
    }

    private void checkResponse(HttpResponse response, int expectedCode)
            throws NotFoundException, ServicesException {
        if (response == null) {
            throw new ServicesException("Could not complete request due to " +
                                        "error: Response was null.");
        }
        int statusCode = response.getStatusCode();
        if (statusCode != expectedCode) {
            String errorMessage;
            try {
                errorMessage = response.getResponseBody();
            } catch(IOException e) {
                errorMessage = "";
            }

            if(statusCode == HttpStatus.SC_NOT_FOUND) {
                throw new NotFoundException(errorMessage);
            } else {
                StringBuilder builder = new StringBuilder();
                builder.append("Could not complete request due to error: ");
                builder.append("Response code was ");
                builder.append(statusCode);
                builder.append(", expected value was ");
                builder.append(expectedCode);
                builder.append(". Error message: ");
                builder.append(errorMessage);
                throw new UnexpectedResponseException(builder.toString(),
                                                      statusCode,
                                                      expectedCode,
                                                      errorMessage);
            }
        }
    }

    public void login(Credential credential) {
        setRestHelper(new RestHttpHelper(credential));
    }

    public void logout() {
        setRestHelper(new RestHttpHelper());
    }

    private RestHttpHelper getRestHelper() {
        if (null == restHelper) {
            restHelper = new RestHttpHelper();
        }
        return restHelper;
    }

    private void setRestHelper(RestHttpHelper restHelper) {
        this.restHelper = restHelper;
    }
}