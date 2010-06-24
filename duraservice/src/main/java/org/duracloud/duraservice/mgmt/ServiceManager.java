/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.computeprovider.domain.ComputeProviderType;
import org.duracloud.domain.Content;
import org.duracloud.duraservice.domain.ServiceCompute;
import org.duracloud.duraservice.domain.ServiceComputeInstance;
import org.duracloud.duraservice.domain.ServiceStore;
import org.duracloud.duraservice.domain.UserStore;
import org.duracloud.duraservice.error.NoSuchDeployedServiceException;
import org.duracloud.duraservice.error.NoSuchServiceComputeInstanceException;
import org.duracloud.duraservice.error.NoSuchServiceException;
import org.duracloud.duraservice.error.ServiceException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Performs management functions over services.
 *
 * @author Bill Branan
 */
public class ServiceManager {

    private static final Logger log = LoggerFactory.getLogger(ServiceManager.class);

    protected static final String PRIMARY_HOST_DISPLAY = "Primary Service Instance";
    protected static final String NEW_HOST_DISPLAY = "New Service Instance";

    // The primary services host, generally deployed on the primary user instance
    private static String primaryHost;

    // Port for connection to the services admin at the primary service instance
    private String primaryServicesAdminPort;

    // Context for connection to the services admin at the primary service instance
    private String primaryServicesAdminContext;

    // List of all services from services config file
    private List<ServiceInfo> services = null;

    // List of all deployed services
    private List<ServiceInfo> deployedServices = null;

    // Provides access to service packages
    private ContentStore serviceStoreClient = null;

    // Store in which user content is stored
    private UserStore userStore = null;

    // Store in which services packages reside
    private ServiceStore serviceStore = null;

    // ServiceCompute used to run service compute instances
    private ServiceCompute serviceCompute = null;

    private int serviceDeploymentIds = 0;

    // List of service compute instances
    private List<ServiceComputeInstance> serviceComputeInstances = null;

    private ContentStoreManagerUtil contentStoreManagerUtil;
    private ServiceConfigUtil serviceConfigUtil;
    private ServiceComputeInstanceUtil serviceComputeInstanceUtil;

    public static final String NEW_SERVICE_HOST = "new";


    public ServiceManager(ContentStoreManagerUtil contentStoreManagerUtil,
                          ServiceConfigUtil serviceConfigUtil,
                          ServiceComputeInstanceUtil serviceComputeInstanceUtil) {
        this.contentStoreManagerUtil = contentStoreManagerUtil;
        this.serviceConfigUtil = serviceConfigUtil;
        this.serviceComputeInstanceUtil = serviceComputeInstanceUtil;

        deployedServices = new ArrayList<ServiceInfo>();
        serviceComputeInstances = new ArrayList<ServiceComputeInstance>();
    }

    /**
     * Initializes the service manager with the provided XML document so
     * that the service manager is able to connect to all services
     * service compute instances, and user storage.
     *
     * @param configXml the xml used to initialize the service manager
     */
    public void configure(InputStream configXml) {
        parseManagerConfigXml(configXml);

        try {
            initializeServicesList();
        } catch (ContentStoreException cse) {
            String error = "Could not build services list due " +
            		       "to exception: " + cse.getMessage();
            throw new ServiceException(error, cse);
        }

        ServiceComputeInstance serviceComputeInstance = serviceComputeInstanceUtil
            .createComputeInstance(primaryHost,
                                   primaryServicesAdminPort,
                                   primaryServicesAdminContext,
                                   PRIMARY_HOST_DISPLAY);

        serviceComputeInstances.add(serviceComputeInstance);
    }

    /*
     * Determines if the service mananger has been initialized.
     */
    private void checkConfigured() {
        if(serviceStore == null) {
            throw new ServiceException("The Service Manager must be initialized " +
            		                   "prior to performing any other activities.");
        }
    }

    /*
     * Parses the initialization xml and makes the values available for use
     */
    private void parseManagerConfigXml(InputStream xml) {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element servicesConfig = doc.getRootElement();

            Element primaryInstance =
                servicesConfig.getChild("primaryServiceInstance");
            primaryHost = primaryInstance.getChildText("host");
            primaryServicesAdminPort =
                primaryInstance.getChildText("servicesAdminPort");
            primaryServicesAdminContext =
                primaryInstance.getChildText("servicesAdminContext");

            Element userStorage = servicesConfig.getChild("userStorage");
            userStore = new UserStore();
            userStore.setHost(userStorage.getChildText("host"));
            userStore.setPort(userStorage.getChildText("port"));
            userStore.setContext(userStorage.getChildText("context"));
            userStore.setMsgBrokerUrl(userStorage.getChildText("msgBrokerUrl"));

            Element serviceStorage = servicesConfig.getChild("serviceStorage");
            serviceStore = new ServiceStore();
            serviceStore.setHost(serviceStorage.getChildText("host"));
            serviceStore.setPort(serviceStorage.getChildText("port"));
            serviceStore.setContext(serviceStorage.getChildText("context"));
            serviceStore.setSpaceId(serviceStorage.getChildText("spaceId"));

            Element serviceComputeProvider = servicesConfig.getChild("serviceCompute");
            serviceCompute = new ServiceCompute();
            String computeProviderType = serviceComputeProvider.getChildText("type");
            serviceCompute.setType(ComputeProviderType.fromString(computeProviderType));
            serviceCompute.setImageId(serviceComputeProvider.getChildText("imageId"));
            Element computeCredential =
                serviceComputeProvider.getChild("computeProviderCredential");
            String serviceComputeUsername = computeCredential.getChildText("username");
            String serviceComputePassword = computeCredential.getChildText("password");
            serviceCompute.setUsername(serviceComputeUsername);
            serviceCompute.setPassword(serviceComputePassword);

            // FIXME: These credentials should come from a 'service-store' config element.
            serviceStore.setUsername(serviceComputeUsername);
            serviceStore.setPassword(serviceComputePassword);

        } catch (Exception e) {
            String error = "Error encountered attempting to parse DuraService " +
            		       "configuration xml " + e.getMessage();
            log.error(error);
            throw new ServiceException(error, e);
        }
    }

    /*
     * Reviews the list of content available in the DuraCloud
     * service storage location and builds a service registry.
     */
    private void initializeServicesList() throws ContentStoreException {
        ContentStoreManager storeManager;
        storeManager = contentStoreManagerUtil.getContentStoreManager(
            serviceStore,
            serviceStore.getCredential());
        serviceStoreClient = storeManager.getPrimaryContentStore();

        refreshServicesList();
    }

    /*
     * Retrieves the xml file where all service information is contained
     * and rebuilds the list of services.
     */
    private void refreshServicesList() {
        String servicesSpaceId = serviceStore.getSpaceId();
        String configFileName = servicesSpaceId + ".xml";
        log.debug("refreshing services list: "+configFileName);
        try {
            Content servicesInfoFile =
                serviceStoreClient.getContent(servicesSpaceId, configFileName);

            this.services = ServicesConfigDocument.getServiceList(
                servicesInfoFile.getStream());

        } catch (ContentStoreException cse) {
            String error = "Could not retrieve services in space: " +
                            servicesSpaceId + " due to exception: " +
                            cse.getMessage();
            log.error(error);
            throw new ServiceException(error, cse);
        }
    }

    /**
     * Retrieves a listing of services which are available for deployment
     * along with the description and user configuration for each service
     *
     * @return list of available services
     */
    public List<ServiceInfo> getAvailableServices() {
        checkConfigured();
        refreshServicesList();

        List<ServiceInfo> availableServices = new ArrayList<ServiceInfo>();
        for(ServiceInfo service : services) {
            log.debug("service: " + service.getContentId() + " available?");
            // Determine if service has an available deployment option
            boolean availDeployment = false;
            for(DeploymentOption depOp : service.getDeploymentOptions()) {
                if(depOp.getState().equals(DeploymentOption.State.AVAILABLE)) {
                    availDeployment = true;
                }
            }

            if(!underMaxDeployments(service.getId(),
                                    service.getMaxDeploymentsAllowed())) {
                availDeployment = false;
            }

            if(availDeployment) {
                ServiceInfo availService = populateService(service);
                availableServices.add(availService);
            }
        }

        log.debug("getAvailableServices(): "+availableServices.size());
        return availableServices;
    }

    /**
     * Retrieves a listing of services which have been deployed. Each service
     * includes the full set of userconfig options as well as the selected
     * options currently in use in order to allow for reconfiguring the service.
     *
     * @return list of deployed services
     */
    public List<ServiceInfo> getDeployedServices() {
        checkConfigured();

        List<ServiceInfo> populatedDepServices = new ArrayList<ServiceInfo>();
        for(ServiceInfo deployedService : deployedServices) {
            ServiceInfo populatedDepService = populateService(deployedService);
            populatedDepServices.add(populatedDepService);
        }
        return populatedDepServices;
    }

    /*
     * Uses the config util to populate service values
     */
    private ServiceInfo populateService(ServiceInfo service) {
        return serviceConfigUtil.populateService(service,
                                                 serviceComputeInstances,
                                                 userStore,
                                                 primaryHost);
    }

    /*
     * Generates a new deployment identifier.
     */
    private int generateDeploymentId() {
        return ++serviceDeploymentIds;
    }

    /**
     * Attempts to deploy the service with the given ID the the server
     * at the given host. The service is configured based on the provided
     * user configuration as well as a set of system configuration values.
     *
     * @param serviceId the ID of the service to be deployed
     * @param serviceHost the server host on which the service is to be deployed
     * @param userConfigVersion version of the user configuration
     * @param userConfig user configuration settings for the service
     * @return the deployment ID of the newly deployed service
     * @throws NoSuchServiceException if there is no service with ID = serviceId
     * @throws NoSuchServiceComputeInstanceException if there is no services compute instance at serviceHost
     */
    public int deployService(int serviceId,
                             String serviceHost,
                             String userConfigVersion,
                             List<UserConfig> userConfig)
        throws NoSuchServiceException, NoSuchServiceComputeInstanceException  {
        checkConfigured();
        refreshServicesList();

        ServiceInfo srvToDeploy = findService(serviceId);

        checkUserConfigVersion(srvToDeploy, userConfigVersion);

        // Make sure that another deployment of this service is allowed
        int maxDeployments = srvToDeploy.getMaxDeploymentsAllowed();
        if(!underMaxDeployments(serviceId, maxDeployments)) {
            String error = "Service not deployed, the maximum deployment " +
                "limit for this service " + maxDeployments + " has been reached";
            throw new ServiceException(error);
        }

        ServiceComputeInstance computeInstance =
            getServiceHost(serviceId, serviceHost);

        log.info("Deploying service " + serviceId + " to " + serviceHost);

        // Resolve system config
        List<SystemConfig> systemConfig = srvToDeploy.getSystemConfigs();
        if(systemConfig != null) {
            systemConfig = serviceConfigUtil.resolveSystemConfigVars(userStore,
                                                                     systemConfig);
        }

        Map<String, String> serviceConfig =
            createServiceConfig(userConfig, systemConfig);

        serviceConfig = cleanMap(serviceConfig);

        try {
            // Grab file from store
            String contentId = srvToDeploy.getContentId();
            Content content =
                serviceStoreClient.getContent(serviceStore.getSpaceId(),
                                              contentId);
            InputStream serviceStream = content.getStream();
            long length = getContentLength(content);

            // Deploy the service (push file to services admin)
            ServicesAdminClient servicesAdmin =
                computeInstance.getServicesAdmin();
            HttpResponse response =
                servicesAdmin.postServiceBundle(contentId, serviceStream, length);
            String error = "Unable to deploy service bundle." + contentId;
            checkResponse(response, error);

            waitUntilDeployed(contentId, servicesAdmin);

            // Configure the service
            response =
                servicesAdmin.postServiceConfig(contentId, serviceConfig);
            error = "Unable to configure service bundle." + contentId;
            checkResponse(response, error);

            waitUntilConfigured(contentId, serviceConfig, servicesAdmin);

            // Start the service
            response = servicesAdmin.startServiceBundle(contentId);
            error = "Unable to start service bundle." + contentId;
            checkResponse(response, error);
        } catch(Exception e) {
            String error = "Unable to deploy service " + serviceId +
                           " to " + serviceHost + " due to error: " +
                           e.getMessage();
            log.error(error);
            throw new ServiceException(error, e);
        }

        return storeDeployedService(srvToDeploy,
                                    computeInstance.getHostName(),
                                    userConfig,
                                    systemConfig);
    }

    private void waitUntilDeployed(String contentId,
                                   ServicesAdminClient servicesAdmin)
        throws Exception {
        int waitMillis = 2000;
        int maxLoops = 5;
        for (int i = 0; i < maxLoops; i++) {
            if (servicesAdmin.isServiceDeployed(contentId)) {
                return;

            } else {
                sleep(waitMillis);
            }
        }

        StringBuilder sb = new StringBuilder("Service not deployed in ");
        sb.append(maxLoops * waitMillis / 1000);
        sb.append(" secs: ");
        sb.append(contentId);
        log.error(sb.toString());
        throw new ServiceException(sb.toString());
    }

    private void waitUntilConfigured(String configId,
                                     Map<String, String> expected,
                                     ServicesAdminClient servicesAdmin)
        throws Exception {
        log.debug("config expected: " + expected);
        Map<String, String> config;
        int waitMillis = 2000;
        int maxLoops = 5;
        for (int i = 0; i < maxLoops; i++) {
            sleep(waitMillis);
            config = servicesAdmin.getServiceConfig(configId);
            log.debug("config found: " + config);
            if (matches(expected, config)) {
                return;
            }
        }

        StringBuilder sb = new StringBuilder("Service not configured in ");
        sb.append(maxLoops * waitMillis / 1000);
        sb.append(" secs: ");
        sb.append(configId);
        log.error(sb.toString());
        throw new ServiceException(sb.toString());
    }

    private boolean matches(Map<String, String> expected,
                            Map<String, String> config) {
        if (null == config) {
            return false;
        }

        for (String key : expected.keySet()) {
            String val = expected.get(key);
            if (val != null && !val.equalsIgnoreCase(config.get(key))) {
                return false;
            }
        }
        return true;
    }

    private void checkUserConfigVersion(ServiceInfo service,
                                        String userConfigVersion) {
        // Make sure the user config version is current
        String currentConfigVersion = service.getUserConfigVersion();
        if(!userConfigVersion.equals(currentConfigVersion)) {
            String error = "User config version " + userConfigVersion +
                " does not match the most current user config version: " +
                currentConfigVersion + ". These versions must match in order " +
                "to ensure that the service configuration is created " +
                "properly for deployment.";
            throw new ServiceException(error);
        }
    }

    /*
     * Checks to see if a service has hit its max deployment limit
     * @return true if the service is still under the max deployment limit
     */
    private boolean underMaxDeployments(int serviceId, int maxDeployments) {
        if(maxDeployments < 0) {
            return true;
        }

        int deploymentCount = 0;
        for(ServiceInfo deployedService : deployedServices) {
            if(serviceId == deployedService.getId()) {
                List<Deployment> deployments = deployedService.getDeployments();
                if(deployments != null) {
                    deploymentCount += deployments.size();
                }
            }
        }
        return (deploymentCount < maxDeployments);
    }

    /*
     * Removes entries in the map with null keys.
     * Updates map entries with null values to have an empty string value.
     */
    private Map<String, String> cleanMap(Map<String, String> map) {
        Map<String, String> cleanMap = new HashMap<String, String>();
        for(String key : map.keySet()) {
            if(key != null) {
                if(map.get(key) != null) {
                    cleanMap.put(key, map.get(key));
                } else {
                    cleanMap.put(key, "");
                }
            }
        }
        return cleanMap;
    }

    /*
     * Attempts to verify that the service host is valid based on
     * deployment options configured for this service. If the service
     * is to be deployed on a new compute host, makes the call to create
     * the new host.
     *
     * @return the service compute instance for this host
     */
    private ServiceComputeInstance getServiceHost(int serviceId,
                                                  String serviceHost)
        throws NoSuchServiceException,
               NoSuchServiceComputeInstanceException {
        String host = serviceHost;
        if(host == null || host.equals("")) {
            host = primaryHost;
        }

        ServiceInfo service = findService(serviceId);
        List<DeploymentOption> depOptions = service.getDeploymentOptions();

        String error = "Cannot deploy service, the host " + host + " is not " +
            "a valid deployment location for service with ID " + serviceId;

        if(host.equals(primaryHost)) { // Primary Host
            for(DeploymentOption option : depOptions) {
                if(option.getLocationType().equals(
                    DeploymentOption.Location.PRIMARY)) {
                    if(option.getState().equals(
                        DeploymentOption.State.UNAVAILABLE)) {
                        throw new ServiceException(error);
                    }
                }
            }
        } else if(host.equalsIgnoreCase(NEW_SERVICE_HOST)) { // New Host
            for(DeploymentOption option : depOptions) {
                if(option.getLocationType().equals(
                    DeploymentOption.Location.NEW)) {
                    if(option.getState().equals(
                        DeploymentOption.State.AVAILABLE)) {
                        host =
                            createServiceInstance("Service Compute Instance");
                    } else {
                        throw new ServiceException(error);
                    }
                }
            }
        }

        // Existing Host
        ServiceComputeInstance computeInstance =
            getServiceComputeInstanceByHostName(host);
        if(computeInstance.isLocked()) {
            error = "Cannot deploy service, the host " + host + " has " +
                "been locked to ensure no further services are deployed. You " +
                "must unlock this instance prior to deploying " +
                "additional services.";
            throw new ServiceException(error);
        }

        return computeInstance;
    }

    /*
     * Retrieves a service with the given ID from the services list.
     *
     * @throws NoSuchServiceException if there is no service in the list with the given ID
     */
    private ServiceInfo findService(int serviceId)
        throws NoSuchServiceException {
        for(ServiceInfo service : services) {
            if(service.getId() == (serviceId)) {
                return service;
            }
        }
        throw new NoSuchServiceException(serviceId);
    }

    /*
     * Creates a name/value map of user and system config to be used
     * for configuring a service
     */
    private Map<String, String> createServiceConfig(List<UserConfig> userServiceConfig,
                                                    List<SystemConfig> systemServiceConfig) {
        Map<String, String> serviceConfig = new HashMap<String, String>();

        // Add User Config
        if(userServiceConfig != null) {
            for(UserConfig userConfig : userServiceConfig) {
                if(userConfig instanceof TextUserConfig) {
                    TextUserConfig textConfig = (TextUserConfig) userConfig;
                    serviceConfig.put(textConfig.getName(),
                                      textConfig.getValue());
                } else if(userConfig instanceof SingleSelectUserConfig) {
                    SingleSelectUserConfig singleConfig =
                        (SingleSelectUserConfig) userConfig;
                    String value = null;
                    for(Option option : singleConfig.getOptions()) {
                        if(option.isSelected()) {
                            value = option.getValue();
                        }
                    }
                    if(value != null) {
                        serviceConfig.put(singleConfig.getName(), value);
                    }
                } else if(userConfig instanceof MultiSelectUserConfig) {
                    MultiSelectUserConfig multiConfig =
                        (MultiSelectUserConfig) userConfig;
                    StringBuilder valueBuilder = new StringBuilder();
                    for(Option option : multiConfig.getOptions()) {
                        if(option.isSelected()) {
                            if(valueBuilder.length() > 0) {
                                valueBuilder.append(",");
                            }
                            valueBuilder.append(option.getValue());
                        }
                    }
                    if(valueBuilder.length() > 0) {
                        serviceConfig.put(multiConfig.getName(),
                                          valueBuilder.toString());
                    }
                }
            }
        }

        // Add System Config
        if(systemServiceConfig != null) {
            for(SystemConfig systemConfig : systemServiceConfig) {
                String value = systemConfig.getValue();
                if(value == null || value.isEmpty()) {
                    value = systemConfig.getDefaultValue();
                }
                serviceConfig.put(systemConfig.getName(), value);
            }
        }

        return serviceConfig;
    }

    /*
     * Attempts to determine the length of content. If the length
     * cannot be determined, returns 0.
     */
    private long getContentLength(Content content) {
        Map<String, String> metadata = content.getMetadata();
        String size = metadata.get(ContentStore.CONTENT_SIZE);

        long length = 0;
        if (size != null) {
            try {
                length = Long.parseLong(size);
            } catch (NumberFormatException e) {
                log.warn("Unable to determine size: " + size + ", " + content.getId());
                length = 0;
            }
        }
        return length;
    }

    /*
     * Stores all of the information regarding a service which has been
     * deployed, including the default configuration options, the user selected
     * configuration values, the completed system configuration, the
     * deployment host, and a freshly generated deployment ID.
     *
     * @return deployment ID
     */
    private int storeDeployedService(ServiceInfo serviceToStore,
                                      String serviceHost,
                                      List<UserConfig> userConfig,
                                      List<SystemConfig> systemConfig) {
        ServiceInfo service = serviceToStore.clone();
        int deploymentId = generateDeploymentId();

        Deployment deployment = new Deployment();
        deployment.setId(deploymentId);
        deployment.setHostname(serviceHost);
        deployment.setUserConfigs(userConfig);
        deployment.setSystemConfigs(systemConfig);
        deployment.setStatus(Deployment.Status.STARTED);

        // Determine if there is already a service of this type deployed
        ServiceInfo existingServiceDeployment = null;
        for(ServiceInfo deployedService : deployedServices) {
            if(service.getId() == deployedService.getId()) {
                if(deployedService.getUserConfigVersion().
                    equals(service.getUserConfigVersion())) {
                    existingServiceDeployment = deployedService;
                    break;
                }
            }
        }

        if(existingServiceDeployment != null) { // Add to deployment list
            List<Deployment> deployments =
                existingServiceDeployment.getDeployments();
            deployments.add(deployment);
            existingServiceDeployment.setDeployments(deployments);
        } else { // Add service with new deployment list
            List<Deployment> deployments = new ArrayList<Deployment>();
            deployments.add(deployment);
            service.setDeployments(deployments);
            deployedServices.add(service);
        }

        return deploymentId;
    }

    /**
     * Gets a service which has been deployed.
     * This provides the full set of userconfig options as well as the
     * selected options currently in use a particular deployment.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the service deployment to retrieve
     * @return the service with a single deployment
     * @throws NoSuchDeployedServiceException if either service or deployment does not exist
     */
    public ServiceInfo getDeployedService(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException {
        checkConfigured();

        ServiceInfo deployedService =
            findDeployedService(serviceId, deploymentId);

        ServiceInfo populatedDepService = populateService(deployedService);

        List<Deployment> deployments = populatedDepService.getDeployments();
        for(Deployment deployment : deployments) {
            if(deployment.getId() != deploymentId) {
                deployments.remove(deployment);
            }
        }

        return populatedDepService;
    }

    /**
     * Gets the properties of a service which has been deployed.
     *
     * @param serviceId the ID of the service to retrieve
     * @param deploymentId the ID of the service deployment to retrieve
     * @return the properties of the service
     * @throws NoSuchDeployedServiceException if either service or deployment does not exist
     */
    public Map<String, String> getDeployedServiceProps(int serviceId,
                                                       int deploymentId)
        throws NoSuchDeployedServiceException {
        checkConfigured();

        ServiceInfo deployedService =
            findDeployedService(serviceId, deploymentId);
        Deployment serviceDeployment =
            findServiceDeployment(deployedService, deploymentId);
        String hostName = serviceDeployment.getHostname();

        ServiceComputeInstance serviceComputeInstance;
        try {
            serviceComputeInstance = getServiceComputeInstanceByHostName(hostName);
        } catch (NoSuchServiceComputeInstanceException e) {
            String error = "Host name " + hostName +
                " is not valid for deployment " + deploymentId +
                " of service " + serviceId;
            throw new ServiceException(error);
        }

        ServicesAdminClient servicesAdmin = serviceComputeInstance.getServicesAdmin();
        Map<String, String> serviceProps;
        try {
            serviceProps =
                servicesAdmin.getServiceProps(deployedService.getContentId());
        } catch(Exception e) {
            String error = "Could not get properties for deployment " +
                deploymentId + " of service " + serviceId +
                " due to error: " + e.getMessage();
            throw new ServiceException(error);
        }

        return serviceProps;
    }

    /*
     * Finds a deployed service in the list based on the service
     * and deployment IDs
     */
    private ServiceInfo findDeployedService(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException {
        for(ServiceInfo service : deployedServices) {
            if(service.getId() == serviceId) {
                if(findServiceDeployment(service, deploymentId) != null) {
                    return service;
                }
            }
        }
        throw new NoSuchDeployedServiceException(serviceId, deploymentId);
    }

    /*
     * Finds a particular deployment for a given service
     */
    private Deployment findServiceDeployment(ServiceInfo service,
                                             int deploymentId)
        throws NoSuchDeployedServiceException {
        for(Deployment deployment : service.getDeployments()) {
            if(deployment.getId() == deploymentId) {
                return deployment;
            }
        }
        throw new NoSuchDeployedServiceException(service.getId(), deploymentId);
    }

    /**
     * Updates the configuration of a service which has already been deployed
     *
     * @param serviceId the ID of the service to update
     * @param deploymentId the ID of the service deployment to update
     * @param userConfigVersion version of the user configuration
     * @param userConfig the updated user configuration for this service deployment
     * @throws NoSuchDeployedServiceException if either service or deployment does not exist
     */
    public void updateServiceConfig(int serviceId,
                                    int deploymentId,
                                    String userConfigVersion,
                                    List<UserConfig> userConfig)
        throws NoSuchDeployedServiceException {
        checkConfigured();

        ServiceInfo deployedService =
            findDeployedService(serviceId, deploymentId);
        Deployment serviceDeployment =
            findServiceDeployment(deployedService, deploymentId);

        checkUserConfigVersion(deployedService, userConfigVersion);

        log.info("Configuring service: " + serviceId);

        Map<String, String> config =
            createServiceConfig(userConfig, deployedService.getSystemConfigs());

        config = cleanMap(config);

        try {
            ServicesAdminClient servicesAdmin =
                getServicesAdmin(serviceDeployment.getHostname());
            String contentId = deployedService.getContentId();

            HttpResponse response = servicesAdmin.stopServiceBundle(contentId);
            String error = "Unable to stop service bundle." + contentId;
            checkResponse(response, error);

            response = servicesAdmin.postServiceConfig(contentId, config);
            error = "Unable to update service bundle config." + contentId;
            checkResponse(response, error);

            response = servicesAdmin.startServiceBundle(contentId);
            error = "Unable to re-start service bundle." + contentId;
            checkResponse(response, error);

        } catch(Exception e) {
            String error = "Unable to update service configuration  for " +
                "deployment " + deploymentId + " of service " + serviceId +
                "due to error " + e.getMessage();
            throw new ServiceException(error, e);
        }

        List<Deployment> deployments = deployedService.getDeployments();
        deployments.remove(serviceDeployment);
        serviceDeployment.setUserConfigs(userConfig);
        deployments.add(serviceDeployment);
    }

    private void checkResponse(HttpResponse response,
                               String error) {
        int statusCode = response.getStatusCode();
        String responseBody = "";
        try {
            responseBody = response.getResponseBody();
        } catch(IOException e) {
            responseBody = "none";
        }
        if(statusCode != HttpURLConnection.HTTP_OK) {
            String err = error + " Services Admin response code: " +
                statusCode + ", response message: " + responseBody;
            throw new ServiceException(err);
        }
    }

    /**
     * Gets a service. Includes user configuration options, description, etc.
     * as well as all deployments of the service.
     * Does not guarantee that the service is available for deployment.
     *
     * @param serviceId the ID of the service to be retrieved
     * @return a service
     * @throws NoSuchServiceException if the service does not exist
     */
    public ServiceInfo getService(int serviceId)
        throws NoSuchServiceException {
        checkConfigured();
        refreshServicesList();

        // See if this is a deployed service
        ServiceInfo service = null;
        for(ServiceInfo depService : deployedServices) {
            if(depService.getId() == serviceId) {
                service = depService;
            }
        }

        // If not deployed, get from the services list
        if(service == null) {
            service = findService(serviceId);
        }

        return populateService(service);
    }

    /**
     * Undeploys all of the deployed services.
     */
    public void undeployAllServices() {
        // Create the list of deployments first so that we don't end up
        // trying to reference a service which has been removed from the list,
        // which occurs when there are no further deployments of a service
        List<DeploymentInstance> deployments =
            new ArrayList<DeploymentInstance>();
        for(ServiceInfo service : deployedServices) {
            for(Deployment deployment : service.getDeployments()) {
                deployments.add(new DeploymentInstance(service.getId(),
                                                       deployment.getId()));
            }
        }

        for(DeploymentInstance deployment : deployments) {
            try {
                undeployService(deployment.serviceId, deployment.deploymentId);
            } catch(NoSuchDeployedServiceException e) {
                String error = "Could not undeploy a service in the deployed " +
                    "services list. ServiceID: " + deployment.serviceId +
                    ", DeploymentID: " + deployment.deploymentId;
                log.error(error);
            }
        }
    }

    /*
     * Simple data structure to contain the serviceId and deploymentId pair
     */
    private class DeploymentInstance {
        int serviceId;
        int deploymentId;

        public DeploymentInstance(int serviceId, int deploymentId) {
            this.serviceId = serviceId;
            this.deploymentId = deploymentId;
        }
    }

    /**
     * Stops and un-deploys a service
     *
     * @param serviceId the ID of the service to undeploy
     * @param deploymentId the ID of the service deployment to undeploy
     * @throws NoSuchDeployedServiceException if either service or deployment does not exist
     */
    public void undeployService(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException {
        checkConfigured();

        ServiceInfo deployedService =
            findDeployedService(serviceId, deploymentId);
        Deployment serviceDeployment =
            findServiceDeployment(deployedService, deploymentId);

        String serviceHost = serviceDeployment.getHostname();
        String contentId = deployedService.getContentId();

        log.info("UnDeploying service: " + serviceId + " from " + serviceHost);

        try {
            ServicesAdminClient servicesAdmin = getServicesAdmin(serviceHost);
            servicesAdmin.stopServiceBundle(contentId);
            HttpResponse response = servicesAdmin.deleteServiceBundle(contentId);
            if(response.getStatusCode() != HttpURLConnection.HTTP_OK) {
                throw new Exception("Services Admin response code was " +
                                    response.getStatusCode());
            }

            // Wait to allow undeployment to complete
            int maxLoops = 5;
            for(int i=0; i < maxLoops; i++) {
                if(servicesAdmin.isServiceDeployed(contentId)) {
                    Thread.sleep(2000);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            String error = "Unable to undeploy service " + serviceId +
                           " from " + serviceHost + " due to error: " +
                           e.getMessage();
            log.error(error);
            throw new ServiceException(error, e);
        }

        removeDeployedService(serviceId, deploymentId);
    }

    /*
     * Removes a service deployment from the deployed services list
     */
    private void removeDeployedService(int serviceId, int deploymentId)
        throws NoSuchDeployedServiceException {
        ServiceInfo deployedService =
            findDeployedService(serviceId, deploymentId);
        Deployment serviceDeployment =
            findServiceDeployment(deployedService, deploymentId);

        List<Deployment> deployments = deployedService.getDeployments();
        deployments.remove(serviceDeployment);

        if(deployments.size() <= 0) {
            deployedServices.remove(deployedService);
        }
    }

    /*
     * Retrieves a connection to the services administrator on the given host
     */
    private ServicesAdminClient getServicesAdmin(String instanceHost)
        throws NoSuchServiceComputeInstanceException {
        ServiceComputeInstance instance =
            getServiceComputeInstanceByHostName(instanceHost);
        return instance.getServicesAdmin();
    }

    /**
     * Locks a service compute instance, meaning that no further services
     * should be deployed on this instance until the lock is removed.
     *
     * @param instanceHost the host name of the compute instance to lock
     * @throws NoSuchServiceComputeInstanceException if no such compute instance exists
     */
    public void lockServiceComputeInstance(String instanceHost)
        throws NoSuchServiceComputeInstanceException {
        checkConfigured();
        ServiceComputeInstance instance =
            getServiceComputeInstanceByHostName(instanceHost);
        instance.lock();
    }

    /**
     * Unlocks a services compute instance, indicating that the instance is
     * available for further service deployments.
     *
     * @param instanceHost the host name of the compute instance to unlock
     * @throws NoSuchServiceComputeInstanceException if no such compute instance exists
     */
    public void unlockServiceComputeInstance(String instanceHost)
        throws NoSuchServiceComputeInstanceException {
        checkConfigured();
        ServiceComputeInstance instance =
            getServiceComputeInstanceByHostName(instanceHost);
        instance.unlock();
    }

    /*
     * Retrieves a service compute instance based on its host name 
     */
    private ServiceComputeInstance getServiceComputeInstanceByHostName(String hostName)
        throws NoSuchServiceComputeInstanceException {
        if(hostName == null || hostName.isEmpty()) {
            String error = "instanceHost may not be null or empty";
            throw new IllegalArgumentException(error);
        }

        for(ServiceComputeInstance instance : serviceComputeInstances) {
            if(instance.getHostName().equals(hostName)) {
                return instance;
            }
        }
        throw new NoSuchServiceComputeInstanceException(hostName);
    }

    /**
     * Starts up a new services compute instance.
     *
     * @param displayName the name to display to indicate the new instance
     * @return the hostName of the new instance
     */
    public String createServiceInstance(String displayName) {
        checkConfigured();
        //TODO: Make call to start services instance through compute manager
        //ServiceInstance serviceInstance = startNewServiceInstance();
        //addServiceComputeInstance(serviceInstance.getHostName(),
        //                          serviceInstance.getServicesAdminPort(),
        //                          serviceInstance.getServicesAdminContext(),
        //                          displayName);
        //return serviceInstance.getHostName();
        return primaryHost;
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

}
