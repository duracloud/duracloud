/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.apache.commons.lang.StringUtils;
import org.duracloud.*;
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

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for binding service-config xml documents to
 * ServiceInfo objects.
 *
 * @author Andrew Woods
 *         Date: Nov 19, 2009
 */
public class ServiceElementReader {

    /**
     * This method binds a multi-service xml document to a list of ServiceInfo
     * objects
     *
     * @param doc service-config xml document
     * @return list of ServiceInfo objects
     */
    public static List<ServiceInfo> createServiceListFrom(ServicesDocument doc) {
        List<ServiceInfo> serviceInfos = new ArrayList<ServiceInfo>();

        ServicesDocument.Services servicesType = doc.getServices();
        checkSchemaVersion(servicesType.getSchemaVersion());

        ServiceType[] serviceTypes = servicesType.getServiceArray();
        if (null != serviceTypes && serviceTypes.length > 0) {
            for (ServiceType serviceType : serviceTypes) {
                serviceInfos.add(createServiceFromElement(serviceType));
            }
        }

        return serviceInfos;
    }

    /**
     * This method binds a single-service xml document to a single ServiceInfo
     * object
     *
     * @param doc service-config xml document
     * @return single ServiceInfo object
     */
    public static ServiceInfo createServiceFrom(ServiceDocument doc) {
        SingleServiceType singleService = doc.getService();
        checkSchemaVersion(singleService.getSchemaVersion());

        return createServiceFromElement(singleService);
    }

    private static void checkSchemaVersion(String schemaVersion) {
        if (!schemaVersion.equals(ServicesConfigDocument.SCHEMA_VERSION)) {
            // FIXME: add proper runtime exception
            throw new RuntimeException(
                "Unsupported schema version: " + schemaVersion);
        }
    }

    private static ServiceInfo createServiceFromElement(ServiceType serviceType) {
        ServiceInfo service = new ServiceInfo();

        int id = serviceType.getId();
        if (id >= 0) {
            service.setId(id);
        }

        String serviceVersion = serviceType.getServiceVersion();
        if (!StringUtils.isBlank(serviceVersion)) {
            service.setServiceVersion(serviceVersion);
        }

        String contentId = serviceType.getContentId();
        if (!StringUtils.isBlank(contentId)) {
            service.setContentId(contentId);
        }

        String displayName = serviceType.getDisplayName();
        if (!StringUtils.isBlank(displayName)) {
            service.setDisplayName(displayName);
        }

        String userConfigVersion = serviceType.getUserConfigVersion();
        if (!StringUtils.isBlank(userConfigVersion)) {
            service.setUserConfigVersion(userConfigVersion);
        }

        String description = serviceType.getDescription();
        if (!StringUtils.isBlank(description)) {
            service.setDescription(description);
        }

        SystemConfigType systemConfigType = serviceType.getSystemConfig();
        if (null != systemConfigType) {
            service.setSystemConfigs(createSystemConfigs(systemConfigType));
        }

        UserConfigType userConfigType = serviceType.getUserConfig();
        if (null != userConfigType) {
            service.setUserConfigs(createUserConfigs(userConfigType));
        }

        DeploymentOptionsType deploymentOptionsType = serviceType.getDeploymentOptions();
        if (null != deploymentOptionsType) {
            int max = deploymentOptionsType.getMax();
            if (max >= -1) {
                service.setMaxDeploymentsAllowed(max);
            }

            service.setDeploymentOptions(createDeploymentOptions(
                deploymentOptionsType));
        }

        DeploymentsType deploymentsType = serviceType.getDeployments();
        if (null != deploymentsType) {
            service.setDeployments(createDeployments(deploymentsType));
        }

        return service;
    }

    private static List<SystemConfig> createSystemConfigs(SystemConfigType systemConfigType) {
        List<SystemConfig> systemConfigs = new ArrayList<SystemConfig>();

        SystemPropertyType[] systemPropertyTypes = systemConfigType.getPropertyArray();
        if (null != systemPropertyTypes && systemPropertyTypes.length > 0) {
            for (SystemPropertyType systemPropertyType : systemPropertyTypes) {
                int idNotUsed = systemPropertyType.getId();
                String name = systemPropertyType.getName();
                String value = systemPropertyType.getValue();
                String defaultValue = systemPropertyType.getDefaultValue();

                systemConfigs.add(new SystemConfig(name, value, defaultValue));
            }
        }

        return systemConfigs;
    }

    private static List<UserConfig> createUserConfigs(UserConfigType userConfigType) {
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();

        UserPropertyType[] userPropertyTypes = userConfigType.getPropertyArray();
        if (null != userPropertyTypes && userPropertyTypes.length > 0) {
            for (UserPropertyType userPropertyType : userPropertyTypes) {
                userConfigs.add(createUserConfig(userPropertyType));
            }
        }

        return userConfigs;
    }

    private static UserConfig createUserConfig(UserPropertyType userPropertyType) {
        UserConfig userConfig = null;

        String name = userPropertyType.getName();
        String displayName = userPropertyType.getDisplayName();

        OptionInputType.Enum inputType = userPropertyType.getInput();
        if (null == inputType) {
            // FIXME: add proper runtime exception
            throw new RuntimeException("InputType can not be null");
        }

        if (inputType.equals(OptionInputType.TEXT)) {
            String value = userPropertyType.getValue();
            userConfig = new TextUserConfig(name, displayName, value);
        } else if (inputType.equals(OptionInputType.SINGLESELECT)) {
            List<Option> options = createOptions(userPropertyType);
            userConfig = new SingleSelectUserConfig(name, displayName, options);
        } else if (inputType.equals(OptionInputType.MULTISELECT)) {
            List<Option> options = createOptions(userPropertyType);
            userConfig = new MultiSelectUserConfig(name, displayName, options);
        } else {
            // FIXME: add proper runtime exception
            throw new RuntimeException("Unexpected inputType: " + inputType);
        }

        return userConfig;
    }

    private static List<Option> createOptions(UserPropertyType userPropertyType) {
        List<Option> options = new ArrayList<Option>();

        UserPropertyType.Option[] optionTypes = userPropertyType.getOptionArray();
        if (null != optionTypes && optionTypes.length > 0) {
            for (UserPropertyType.Option optionType : optionTypes) {
                String displayName = optionType.getDisplayName();
                String value = optionType.getValue();
                boolean selected = optionType.getSelected();

                options.add(new Option(displayName, value, selected));
            }
        }

        return options;
    }

    private static List<DeploymentOption> createDeploymentOptions(
        DeploymentOptionsType deploymentOptionsType) {
        List<DeploymentOption> options = new ArrayList<DeploymentOption>();

        DeploymentOptionType[] deploymentOptionTypes = deploymentOptionsType.getOptionArray();
        if (null != deploymentOptionTypes && deploymentOptionTypes.length > 0) {
            for (DeploymentOptionType deploymentOptionType : deploymentOptionTypes) {
                DeploymentOption option = new DeploymentOption();

                String displayName = deploymentOptionType.getDisplayName();
                option.setDisplayName(displayName);

                String hostname = deploymentOptionType.getHostname();
                option.setHostname(hostname);

                DeploymentOptionLocationType.Enum locationType = deploymentOptionType
                    .getLocation();
                option.setLocation(DeploymentOption.Location.valueOf(
                    locationType.toString()));

                DeploymentOptionStateType.Enum stateType = deploymentOptionType.getState();
                if (null != stateType) {
                    option.setState(DeploymentOption.State.valueOf(stateType.toString()));
                }

                options.add(option);
            }
        }

        return options;
    }

    private static List<Deployment> createDeployments(DeploymentsType deploymentsType) {
        List<Deployment> deployments = new ArrayList<Deployment>();

        DeploymentType[] deploymentTypes = deploymentsType.getDeploymentArray();
        if (null != deploymentTypes && deploymentTypes.length > 0) {
            for (DeploymentType deploymentType : deploymentTypes) {
                Deployment deployment = new Deployment();

                int id = deploymentType.getId();
                deployment.setId(id);

                String hostname = deploymentType.getHostname();
                deployment.setHostname(hostname);

                DeploymentStatusType.Enum statusType = deploymentType.getStatus();
                deployment.setStatus(Deployment.Status.valueOf(statusType.toString()));

                SystemConfigType systemConfigType = deploymentType.getSystemConfig();
                if (null != systemConfigType) {
                    deployment.setSystemConfigs(createSystemConfigs(
                        systemConfigType));
                }

                UserConfigType userConfigType = deploymentType.getUserConfig();
                if (null != userConfigType) {
                    deployment.setUserConfigs(createUserConfigs(userConfigType));
                }

                deployments.add(deployment);
            }
        }

        return deployments;
    }

}
