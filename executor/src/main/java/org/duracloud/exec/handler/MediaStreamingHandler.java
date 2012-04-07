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
import org.duracloud.exec.error.InvalidActionRequestException;
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
import java.util.Map;

/**
 * Manages the running of the Media Streaming service in DuraCloud.
 *
 * @author: Bill Branan
 * Date: 3/2/12
 */
public class MediaStreamingHandler extends BaseServiceHandler {

    private final Logger log =
        LoggerFactory.getLogger(MediaStreamingHandler.class);

    private static final String STATUS_PREFIX = "Media Streamer: ";
    protected static final String HANDLER_NAME = "media-streaming-handler";

    protected static final String HANDLER_STATE_FILE =
        "media-streaming-handler-state.xml";

    private int spacesStreamed;

    public MediaStreamingHandler() {
        super();
        supportedActions.add(START_STREAMING);
        supportedActions.add(STOP_STREAMING);
        supportedActions.add(START_STREAMING_SPACE);
        supportedActions.add(STOP_STREAMING_SPACE);
        spacesStreamed = 0;
    }

    @Override
    public String getName() {
        return HANDLER_NAME;
    }

    @Override
    public void start() {
        status = STATUS_PREFIX + "Idle";
    }

    private void startStreamingService() {
        log.info("Executor: Starting Media Streaming Service");

        status = STATUS_PREFIX + "Starting";
        try {
            ServiceInfo service =
                findAvailableServiceByName(MEDIA_STREAMER_NAME);
            if(null != service) {
                List<UserConfigModeSet> userConfig =
                    service.getUserConfigModeSets();

                Map<String, String> state = getState(HANDLER_STATE_FILE);
                incorporateState(userConfig, state);

                servicesMgr.deployService(service.getId(),
                                          getDeploymentHost(service),
                                          service.getUserConfigVersion(),
                                          userConfig);
                status = STATUS_PREFIX + "Started. Spaces Streamed: " +
                         spacesStreamed;
            }
        } catch(NotFoundException e) {
            setError("Unable to start the Media Streaming service due " +
                     "to a NotFoundException: " + e.getMessage(), e);
        } catch(ServicesException e) {
            setError("Unable to start the Media Streaming service due " +
                     "to a ServicesException: " + e.getMessage(), e);
        }
    }

    private void incorporateState(List<UserConfigModeSet> userConfig,
                                  Map<String, String> state) {
        UserConfig config = userConfig.get(0).getModes().get(0)
                                      .getUserConfigs().get(0);
        if(SOURCE_SPACE_ID.equals(config.getName()) &&
            config instanceof MultiSelectUserConfig) {
            List<Option> spaceOptions =
                ((MultiSelectUserConfig)config).getOptions();

            for(Option option : spaceOptions) {
                if(state.containsKey(option.getValue())) {
                    option.setSelected(true);
                    ++spacesStreamed;
                }
            }
        }
    }

    @Override
    public void stop() {
        log.info("Executor: Stopping Media Streaming Service");

        status = STATUS_PREFIX + "Stopping";
        try {
            ServiceInfo service =
                findDeployedServiceByName(MEDIA_STREAMER_NAME);
            if(null != service) {
                Deployment dep = service.getDeployments().get(0);
                servicesMgr.undeployService(service.getId(), dep.getId());
                status = STATUS_PREFIX + "Stopped";
            }
        } catch(NotFoundException e) {
            setError("Unable to stop the Media Streaming service due " +
                     "to a NotFoundException: " + e.getMessage(), e);
        } catch(ServicesException e) {
            setError("Unable to stop the Media Streaming service due " +
                     "to a ServicesException: " + e.getMessage(), e);
        }
    }

    /**
     * @param actionName supported actions: start-streaming, stop-streaming
     * @param actionParameters spaceId to start or stop streaming
     */
    @Override
    public void performAction(String actionName, String actionParameters)
        throws InvalidActionRequestException {
        log.info("Executor: Performing action: " + actionName +
                 ", with parameters: " + actionParameters);

        if(START_STREAMING.equals(actionName)) {
            startStreamingService();
        } else if(STOP_STREAMING.equals(actionName)) {
            stop();
        } else if(START_STREAMING_SPACE.equals(actionName)) {
            Map<String, String> state = getState(HANDLER_STATE_FILE);
            String spaceId = getSpaceId(actionParameters);
            state.put(spaceId, Boolean.TRUE.toString());
            storeState(HANDLER_STATE_FILE, state);

            setStreaming(spaceId, true);
        } else if(STOP_STREAMING_SPACE.equals(actionName)) {
            Map<String, String> state = getState(HANDLER_STATE_FILE);
            String spaceId = getSpaceId(actionParameters);
            state.remove(spaceId);
            storeState(HANDLER_STATE_FILE, state);

            setStreaming(spaceId, false);
        } else {
            String err = actionName + " is not a valid action";
            throw new InvalidActionRequestException(err);
        }
    }

    private String getSpaceId(String actionParameters)
        throws InvalidActionRequestException {
        if(actionParameters.isEmpty()) {
            String err = "Parameters expected: 'spaceId'";
            throw new InvalidActionRequestException(err);
        }
        return actionParameters;
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

                status = STATUS_PREFIX + "Started. Spaces Streamed: " +
                         spacesStreamed;
            }
        } catch(NotFoundException e) {
            setError("Unable to update streaming for space " + spaceId +
                     " due to a NotFoundException: " + e.getMessage(), e);
        } catch(ServicesException e) {
            setError("Unable to update streaming for space " + spaceId +
                     " due to a ServicesException: " + e.getMessage(), e);
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

    private void setError(String error, Exception e) {
        log.error(error, e);
        status = ERROR_PREFIX + error;
        throw new DuraCloudRuntimeException(error, e);
    }

}