/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.exec.handler;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.error.ContentStoreException;
import org.duracloud.exec.error.UnsupportedActionException;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the running of the Media Streaming service in DuraCloud.
 *
 * @author: Bill Branan
 * Date: 3/2/12
 */
public class MediaStreamingHandler extends BaseServiceHandler {

    private final Logger log =
        LoggerFactory.getLogger(MediaStreamingHandler.class);

    protected static final String HANDLER_NAME = "media-streaming-handler";
    protected static final String START_STREAMING = "start-streaming";
    protected static final String STOP_STREAMING = "stop-streaming";

    protected static final String MEDIA_STREAMER_NAME = "Media Streamer";
    protected static final String SOURCE_SPACE_ID = "mediaSourceSpaceId";

    private int spacesStreamed;

    public MediaStreamingHandler() {
        super();
        supportedActions.add(START_STREAMING);
        supportedActions.add(STOP_STREAMING);
        status = "Media Streamer: Initialized";
        spacesStreamed = 0;
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public void start() {
        log.info("Executor: Starting Media Streaming Service");

        status = "Media Streamer: Starting";
        try {
            ServiceInfo service =
                findAvailableServiceByName(MEDIA_STREAMER_NAME);
            if(null != service) {
                List<UserConfigModeSet> userConfig =
                    service.getUserConfigModeSets();
                servicesMgr.deployService(service.getId(),
                                          getDeploymentHost(service),
                                          service.getUserConfigVersion(),
                                          userConfig);
                status = "Media Streamer: Started. Spaces Streamed: " +
                         spacesStreamed;
            }
        } catch(NotFoundException e) {
            setError("Unable to start the Media Streaming service due " +
                     "to a NotFoundException: " + e.getMessage());
        } catch(ServicesException e) {
            setError("Unable to start the Media Streaming service due " +
                     "to a ServicesException: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        log.info("Executor: Stopping Media Streaming Service");

        status = "Media Streamer: Stopping";
        try {
            ServiceInfo service =
                findDeployedServiceByName(MEDIA_STREAMER_NAME);
            if(null != service) {
                Deployment dep = service.getDeployments().get(0);
                servicesMgr.undeployService(service.getId(), dep.getId());
                status = "Media Streamer: Stopped";
            }
        } catch(NotFoundException e) {
            setError("Unable to stop the Media Streaming service due " +
                     "to a NotFoundException: " + e.getMessage());
        } catch(ServicesException e) {
            setError("Unable to stop the Media Streaming service due " +
                     "to a ServicesException: " + e.getMessage());
        }
    }

    @Override
    public void performAction(String actionName, String actionParameters) {
        log.info("Executor: Performing action: " + actionName +
                 ", with parameters: " + actionParameters);

        if(START_STREAMING.equals(actionName)) {
            setStreaming(actionParameters, true);
        } else if(STOP_STREAMING.equals(actionName)) {
            setStreaming(actionParameters, false);
        } else {
            throw new UnsupportedActionException(actionName);
        }
    }

    private void setStreaming(String spaceId, boolean enabled) {
        try {
            ServiceInfo service =
                findDeployedServiceByName(MEDIA_STREAMER_NAME);

            if(null != service) {
                boolean changeNeeded = false;
                Deployment dep = service.getDeployments().get(0);
                List<UserConfigModeSet> userConfig =
                    dep.getUserConfigModeSets();
                UserConfig config = userConfig.get(0).getModes().get(0)
                                              .getUserConfigs().get(0);
                if(SOURCE_SPACE_ID.equals(config.getName()) &&
                    config instanceof MultiSelectUserConfig) {
                    List<Option> spaceOptions =
                        ((MultiSelectUserConfig)config).getOptions();
                    boolean optionFound = false;
                    for(Option option : spaceOptions) {
                        if(option.getValue().equals(spaceId)) {
                            optionFound = true;
                            if(enabled != option.isSelected()) {
                                option.setSelected(enabled);
                                changeNeeded = true;
                            }
                            break;
                        }
                    }
                    if(!optionFound) { // Space not found as an option
                        if(enabled) {
                            // Space may have been created after the service
                            // was deployed. Add the space as a new option.
                            verifySpace(spaceId);
                            Option newOption =
                                new Option(spaceId, spaceId, enabled);
                            List<Option> updatedOptions =
                                new ArrayList<Option>();
                            updatedOptions.addAll(spaceOptions);
                            updatedOptions.add(newOption);
                            ((MultiSelectUserConfig)config)
                                .setOptions(updatedOptions);
                            changeNeeded = true;
                        }
                    }
                } else {
                    throw new ServicesException("Unable to find expected " +
                        "config item in deployed Media Streaming service");
                }

                if(changeNeeded) {
                    String configVersion = service.getUserConfigVersion();
                    servicesMgr.updateServiceConfig(service.getId(),
                                                    dep.getId(),
                                                    configVersion,
                                                    userConfig);
                    if(enabled) {
                        ++spacesStreamed;
                    } else {
                        --spacesStreamed;
                    }
                }

                status = "Media Streamer: Started. Spaces Streamed: " +
                         spacesStreamed;
            }
        } catch(NotFoundException e) {
            setError("Unable to update streaming for space " + spaceId +
                     " due to a NotFoundException: " + e.getMessage());
        } catch(ServicesException e) {
            setError("Unable to update streaming for space " + spaceId +
                     " due to a ServicesException: " + e.getMessage());
        }
    }

    private void verifySpace(String spaceId) throws ServicesException {
        try {
            storeMgr.getPrimaryContentStore().getSpaceProperties(spaceId);
        } catch(ContentStoreException e) {
            throw new ServicesException("Space with ID " + spaceId +
                                        " does not exist!");
        }
    }

    private void setError(String error) {
        log.error(error);
        status = error;
        throw new DuraCloudRuntimeException(error);
    }

}