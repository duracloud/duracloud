/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.service;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.duracloud.durareport.error.ReportBuilderException;
import org.duracloud.durareport.service.metrics.RunningService;
import org.duracloud.durareport.storage.StorageReportScheduler;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.duracloud.services.ComputeService.SYSTEM_PREFIX;

/**
 * @author: Bill Branan
 * Date: 6/22/11
 */
public class ServiceReportBuilder {

    private final Logger log =
        LoggerFactory.getLogger(StorageReportScheduler.class);

    private ServicesManager servicesMgr;

    public ServiceReportBuilder(ServicesManager servicesMgr) {
        this.servicesMgr = servicesMgr;
    }

    public String buildServiceReport() {
        List<RunningService> runningServices =  collectRunningServiceInfo();
        XStream xstream = new XStream(new DomDriver());
        return xstream.toXML(runningServices);
    }

    protected List<RunningService> collectRunningServiceInfo()
        throws ReportBuilderException {
        try {
            List<ServiceInfo> deployedServices =
                servicesMgr.getDeployedServices();

            List<RunningService> runningServices =
                new ArrayList<RunningService>();
            for(ServiceInfo deployedService : deployedServices) {
                for(Deployment serviceDep : deployedService.getDeployments()) {
                    RunningService service = new RunningService();
                    int serviceId = deployedService.getId();
                    int deploymentId = serviceDep.getId();

                    service.setId(serviceId);
                    service.setDeploymentId(deploymentId);
                    service.setName(deployedService.getDisplayName());
                    service.setVersion(deployedService.getServiceVersion());
                    service.setConfig(getServiceConfig(serviceDep));
                    service.setProperties(getServiceProps(serviceId,
                                                          deploymentId));
                    runningServices.add(service);
                }
            }

            return runningServices;
        } catch(ServicesException e) {
            String error = "Unable to collect information about running " +
                           "services due to: " + e.getMessage();
            throw new ReportBuilderException(error, e);
        }
    }

    protected Map<String, String> getServiceConfig(Deployment serviceDep) {
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

    private void collectCompletedServiceInfo() {
        // TODO: Pull in completed service listing
    }

}
