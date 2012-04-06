/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import java.util.List;
import java.util.Map;

import org.duracloud.execdata.mediastreaming.MediaStreamingConstants;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigModeSet;

/**
 * 
 * @author Daniel Bernstein
 * 
 */
public class ServiceUtil {
    public static ServiceInfo
        findDeployedServiceByName(ServicesManager servicesMgr,
                                  String serviceName)
            throws NotFoundException,
                ServicesException {

        List<ServiceInfo> deployedServices = servicesMgr.getDeployedServices();
        for (ServiceInfo service : deployedServices) {
            if (serviceName.equals(service.getDisplayName())) {
                return service;
            }
        }

        throw new NotFoundException("Could not find service by name "
            + serviceName + " among deployed services");
    }

    public static ServiceInfo
        findMediaStreamingService(ServicesManager servicesManager)
            throws ServicesException,
                NotFoundException {
        return findDeployedServiceByName(servicesManager,
                                         MediaStreamingConstants.MEDIA_STREAMER_NAME);
    }

    public static boolean isMediaStreamingServiceEnabled(ServiceInfo info,
                                                         String spaceId) {
        List<Deployment> deployments = info.getDeployments();
        if (deployments.size() > 0) {
            Deployment deployment = deployments.get(0);
            deployment.getUserConfigModeSets();

            List<UserConfigModeSet> userConfig =
                deployment.getUserConfigModeSets();
            UserConfig config =
                userConfig.get(0).getModes().get(0).getUserConfigs().get(0);
            if (MediaStreamingConstants.SOURCE_SPACE_ID.equals(config.getName())
                && config instanceof MultiSelectUserConfig) {
                List<Option> spaceOptions =
                    ((MultiSelectUserConfig) config).getOptions();
                for (Option option : spaceOptions) {
                    if (option.getValue().equals(spaceId) && option.isSelected()) {
                        return true;
                    }
                }
            }
        }

        return false;

    }
    
    public static String getStreamingHost(ServicesManager servicesManager, ServiceInfo service, String spaceId) throws NotFoundException, ServicesException{
        int deploymentId =  service.getDeployments().get(0).getId();
        int serviceId = service.getId();
        Map<String,String> props = servicesManager.getDeployedServiceProps(serviceId, deploymentId);
        return props.get("Streaming Host for " + spaceId);
    }
}
