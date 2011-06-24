/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.error.ServiceSummaryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.services.ComputeService.SYSTEM_PREFIX;

/**
 * @author: Bill Branan
 * Date: 6/23/11
 */
public class ServiceSummarizerImpl implements ServiceSummarizer {

    private final Logger log =
        LoggerFactory.getLogger(ServiceSummarizerImpl.class);

    private ServicesManager servicesMgr;

    public ServiceSummarizerImpl(ServicesManager servicesMgr) {
        this.servicesMgr = servicesMgr;
    }

    public List<ServiceSummary> summarizeServices(List<ServiceInfo> services) {
        List<ServiceSummary> serviceSummaries = new ArrayList<ServiceSummary>();
        for(ServiceInfo service : services) {
            serviceSummaries.addAll(summarizeService(service));
        }
        return serviceSummaries;
    }

    public List<ServiceSummary> summarizeService(ServiceInfo service) {
        List<ServiceSummary> serviceSummaries = new ArrayList<ServiceSummary>();
        for(Deployment serviceDep : service.getDeployments()) {
            serviceSummaries.add(summarizeServiceDeployment(service, serviceDep));
        }
        return serviceSummaries;
    }

    public ServiceSummary summarizeService(int serviceId, int deploymentId)
        throws ServiceSummaryException {
        ServiceInfo service;
        String error = "Could not build summary for service with id: " +
            serviceId + " and deployment id: " + deploymentId;
        try {
            service = servicesMgr.getDeployedService(serviceId, deploymentId);
        } catch (DuraCloudCheckedException e) {
            error += " due to: " + e.getMessage();
            throw new ServiceSummaryException(error, e);
        }

        Deployment serviceDep = null;
        for(Deployment dep : service.getDeployments()) {
            if(deploymentId == dep.getId()) {
                serviceDep = dep;
            }
        }
        if(null != serviceDep) {
            return summarizeServiceDeployment(service, serviceDep);
        } else {
            error += " because deployment does not exist";
            throw new ServiceSummaryException(error);
        }
    }

    private ServiceSummary summarizeServiceDeployment(ServiceInfo service,
                                                      Deployment serviceDep) {
        return summarizeServiceDeployment(service.getId(),
                                          service.getDisplayName(),
                                          service.getServiceVersion(),
                                          serviceDep);
    }

    private ServiceSummary summarizeServiceDeployment(int serviceId,
                                                     String serviceName,
                                                     String serviceVersion,
                                                     Deployment serviceDep) {
        ServiceSummary summary = new ServiceSummary();
        int deploymentId = serviceDep.getId();

        summary.setId(serviceId);
        summary.setDeploymentId(deploymentId);
        summary.setName(serviceName);
        summary.setVersion(serviceVersion);
        summary.setConfigs(getServiceConfig(serviceDep));
        summary.setProperties(getServiceProps(serviceId, deploymentId));
        return summary;
    }

    private Map<String, String> getServiceConfig(Deployment serviceDep) {
        Map<String, String> config = new HashMap<String, String>();
        return getModeSetConfig(config, serviceDep.getUserConfigModeSets());
    }

    protected Map<String, String> getModeSetConfig(Map<String, String> config,
                                                   List<UserConfigModeSet> modeSets) {
        if(null != modeSets) {
            for(UserConfigModeSet modeSet : modeSets) {
                for(UserConfigMode mode : modeSet.getModes()) {
                    if(mode.isSelected()) {
                        if(!mode.getName().equals(
                            UserConfigModeSet.DEFAULT_MODE_NAME)) {
                            config.put(modeSet.getDisplayName(),
                                       mode.getDisplayName());
                        }

                        for(UserConfig userConfig : mode.getUserConfigs()) {
                            config.put(userConfig.getDisplayName(),
                                       userConfig.getDisplayValue());
                        }

                        getModeSetConfig(config, mode.getUserConfigModeSets());
                     }
                }
            }
        }
        return config;
    }

    protected Map<String, String> getServiceProps(int serviceId,
                                                  int deploymentId) {
        Map<String, String> filteredProps = new HashMap<String, String>();
        Map<String, String> serviceProps;
        try {
            serviceProps =
                servicesMgr.getDeployedServiceProps(serviceId, deploymentId);
        } catch(Exception e) {
            log.error("Unable to get properties for service with id: " +
                      serviceId + " and deploymentId " + deploymentId);
            serviceProps = null;
        }

        if(null != serviceProps) {
            for(String key : serviceProps.keySet()){
                if(!key.startsWith(SYSTEM_PREFIX)) {
                    filteredProps.put(key, serviceProps.get(key));
                }
            }
        }
        return filteredProps;
    }

}
