/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.util;

import org.apache.commons.collections.CollectionUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.execdata.ExecConstants;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigModeSet;

import java.util.List;
import java.util.Map;

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
                                         ExecConstants.MEDIA_STREAMER_NAME);
    }

    /**
     * 
     * @param servicesManager
     * @return Image server base url if the service is running, otherwise null
     * @throws ServicesException
     */
    public static String
        findImageServerUrlIfAvailable(ServicesManager servicesManager) {
        try {
            String url = null;

            List<ServiceInfo> deployedServices =
                servicesManager.getDeployedServices();
            for (ServiceInfo service : deployedServices) {
                if (service.getContentId().startsWith("j2kservice")) {
                    List<Deployment> deployments = service.getDeployments();

                    if (!CollectionUtils.isEmpty(deployments)) {
                        Deployment deployment = deployments.get(0);
                        Map<String, String> props =
                            servicesManager.getDeployedServiceProps(service.getId(),
                                                                    deployment.getId());
                        if(props != null){
                            url = props.get("url");
                        }
                    }

                    break;
                }
            }

            return url;
        } catch (Exception e) {
            throw new DuraCloudRuntimeException(e);
        }
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
            if (ExecConstants.SOURCE_SPACE_ID.equals(config.getName())
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
