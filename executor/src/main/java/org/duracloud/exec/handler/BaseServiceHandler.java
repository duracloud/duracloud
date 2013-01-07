/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.notification.NotificationManager;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.IOUtil;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.exec.ServiceHandler;
import org.duracloud.exec.error.InvalidActionRequestException;
import org.duracloud.execdata.ExecConstants;
import org.duracloud.manifest.ManifestGenerator;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: Bill Branan
 * Date: 3/2/12
 */
public abstract class BaseServiceHandler
    implements ServiceHandler, ExecConstants {

    private final Logger log =
        LoggerFactory.getLogger(BaseServiceHandler.class);

    protected String host;

    protected ContentStoreManager storeMgr;
    protected ServicesManager servicesMgr;
    protected ManifestGenerator manifestGenerator;
    protected NotificationManager notifier;

    protected Set<String> supportedActions;

    protected String status;

    public BaseServiceHandler() {
        supportedActions = new HashSet<String>();
    }

    @Override
    public void initialize(String host,
                           ContentStoreManager storeMgr,
                           ServicesManager servicesMgr,
                           ManifestGenerator manifestGenerator,
                           NotificationManager notifier) {
        this.host = host;
        this.storeMgr = storeMgr;
        this.servicesMgr = servicesMgr;
        this.manifestGenerator = manifestGenerator;
        this.notifier = notifier;

        verifyStateSpaceExists();
    }

    /**
     * Verifies the existance of the space which is to store state
     * for service handlers.
     */
    protected void verifyStateSpaceExists() {
        try {
            ContentStore store = storeMgr.getPrimaryContentStore();
            try {
                store.getSpaceProperties(HANDLER_STATE_SPACE);
            } catch(ContentStoreException e) {
                store.createSpace(HANDLER_STATE_SPACE);
            }
        } catch(ContentStoreException e) {
            String error = "Could not connect to space " + HANDLER_STATE_SPACE +
                           " due to: " + e.getMessage();
            throw new DuraCloudRuntimeException(error);
        }
    }

    @Override
    public abstract String getName();

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public abstract void start();

    @Override
    public abstract void stop();

    @Override
    public Set<String> getSupportedActions() {
        return supportedActions;
    }

    @Override
    public abstract void performAction(String actionName,
                                       String actionParameters)
        throws InvalidActionRequestException;


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
    public String getDeploymentHost(ServiceInfo service) {
        // Assume a single deployment option
        DeploymentOption depOption = service.getDeploymentOptions().get(0);
        return depOption.getHostname();
    }

    /**
     * Retrieves state information in the form of a map of values from the
     * given state file.
     *
     * @param stateFileName the name of the file where state may exist
     * @return map of state, may be an empty map if no state exists
     */
    public Map<String, String> getState(String stateFileName) {
        Map<String, String> state;
        try {
            Content contentItem = storeMgr.getPrimaryContentStore()
                                          .getContent(HANDLER_STATE_SPACE,
                                                      stateFileName);
            String serializedMap =
                IOUtil.readStringFromStream(contentItem.getStream());
            state = SerializationUtil.deserializeMap(serializedMap);
        } catch(ContentStoreException e) {
            log.info("Could not get handler state from file " + stateFileName +
                     " due to ContentStoreException: " + e.getMessage());
            state = new HashMap<String, String>();
        } catch(IOException e) {
            log.warn("Could not get handler state from file " + stateFileName +
                     " due to IOException: " + e.getMessage());
            state = new HashMap<String, String>();
        }

        return state;
    }

    /**
     * Persists state information for a handler as stored in a Map to the
     * given file name.
     *
     * @param stateFileName the name of the file in which to store state
     * @param state information to store
     */
    public void storeState(String stateFileName, Map<String, String> state) {
        String xml = SerializationUtil.serializeMap(state);
        storeFile(stateFileName, xml, "text/xml");
    }

    protected void storeFile(String contentId, String value, String mimetype) {
        ChecksumUtil checksumUtil = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String checksum = checksumUtil.generateChecksum(value);

        try {
            ContentStore store = storeMgr.getPrimaryContentStore();
            boolean success = false;
            int attempts = 0;
            while(!success && attempts < 5) {
                try {
                    store.addContent(HANDLER_STATE_SPACE,
                                     contentId,
                                     IOUtil.writeStringToStream(value),
                                     value.length(),
                                     mimetype,
                                     checksum,
                                     null);
                    success = true;
                } catch(ContentStoreException e) {
                    log.warn("Failed to store file due to: " +
                             e.getMessage());
                }
                ++attempts;
            }
            if(!success) {
                String err = "Exceeded allowable attempts to store file";
                throw new DuraCloudRuntimeException(err);
            }
        } catch(Exception e) {
            String error = "Not able to store file " + contentId + " due to " +
                           e.getClass().getName() + ": " + e.getMessage();
            throw new DuraCloudRuntimeException(error, e);
        }
    }

    /**
     * Clears state information for a handler.
     *
     * @param stateFileName the name of the file to clear
     */
    public void clearState(String stateFileName) {
        Map<String, String> state = new HashMap<String, String>();
        storeState(stateFileName, state);
    }

}
