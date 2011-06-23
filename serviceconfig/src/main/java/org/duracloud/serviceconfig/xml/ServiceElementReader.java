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
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    protected static ServiceInfo createServiceFromElement(ServiceType serviceType) {
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

        Boolean isSystemService = serviceType.getIsSystemService();
        service.setSystemService(isSystemService);

        SystemConfigType systemConfigType = serviceType.getSystemConfig();
        if (null != systemConfigType) {
            service.setSystemConfigs(createSystemConfigs(systemConfigType));
        }

        UserConfigType userConfigType = serviceType.getUserConfig();
        if (null != userConfigType) {
            ModeSetType[] modeSetTypes = userConfigType.getModeSetArray();
            if (null != modeSetTypes && modeSetTypes.length > 0) {
                service.setUserConfigModeSets(createUserConfigModeSets(modeSetTypes));
            }
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

        DependenciesType dependenciesType = serviceType.getDependencies();
        if (null != dependenciesType) {
            DependencyType[] dependencyTypes = dependenciesType.getDependencyArray();
            if (null != dependencyTypes && dependencyTypes.length > 0) {
                service.setDependencies(createDependencies(dependencyTypes));
            }
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

    private static List<UserConfig> createUserConfigs(UserPropertyType[] userPropertyTypes) {
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        for (UserPropertyType userPropertyType : userPropertyTypes) {
            userConfigs.add(createUserConfig(userPropertyType));
        }

        return userConfigs;
    }

    private static UserConfig createUserConfig(UserPropertyType userPropertyType) {
        UserConfig userConfig = null;

        String name = userPropertyType.getName();
        String displayName = userPropertyType.getDisplayName();
        String exclusion = userPropertyType.getExclusion();

        OptionInputType.Enum inputType = userPropertyType.getInput();
        if (null == inputType) {
            // FIXME: add proper runtime exception
            throw new RuntimeException("InputType can not be null");
        }

        if (inputType.equals(OptionInputType.TEXT)) {
            String value = userPropertyType.getValue();
            userConfig = new TextUserConfig(name,
                                            displayName,
                                            value,
                                            exclusion);

        } else if (inputType.equals(OptionInputType.SINGLESELECT)) {
            List<Option> options = createOptions(userPropertyType);
            userConfig = new SingleSelectUserConfig(name,
                                                    displayName,
                                                    options,
                                                    exclusion);

        } else if (inputType.equals(OptionInputType.MULTISELECT)) {
            List<Option> options = createOptions(userPropertyType);
            userConfig = new MultiSelectUserConfig(name,
                                                   displayName,
                                                   options,
                                                   exclusion);

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

    private static List<UserConfigModeSet> createUserConfigModeSets(ModeSetType[] modeSetTypes) {
        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        for (ModeSetType modeSetType : modeSetTypes) {
            modeSets.add(createUserConfigModeSet(modeSetType));
        }

        return modeSets;
    }

    private static UserConfigModeSet createUserConfigModeSet(ModeSetType modeSetType) {
        UserConfigModeSet modeSet = new UserConfigModeSet();

        modeSet.setId(modeSetType.getId());
        modeSet.setName(modeSetType.getName());
        modeSet.setDisplayName(modeSetType.getDisplayName());
        modeSet.setValue(modeSetType.getValue());

        ModeType[] modeTypes = modeSetType.getModeArray();
        if (null != modeTypes && modeTypes.length > 0) {
            List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
            for (ModeType modeType : modeTypes) {
                modes.add(createUserConfigMode(modeType));
            }

            modeSet.setModes(modes);
        }

        return modeSet;
    }

    private static UserConfigMode createUserConfigMode(ModeType modeType) {
        UserConfigMode mode = new UserConfigMode();

        mode.setName(modeType.getName());
        mode.setDisplayName(modeType.getDisplayName());
        mode.setSelected(modeType.getSelected());

        UserPropertyType[] userPropertyTypes = modeType.getPropertyArray();
        if (null != userPropertyTypes && userPropertyTypes.length > 0) {
            mode.setUserConfigs(createUserConfigs(userPropertyTypes));
        }

        ModeSetType[] modeSetTypes = modeType.getModeSetArray();
        if (null != modeSetTypes && modeSetTypes.length > 0) {
            mode.setUserConfigModeSets(createUserConfigModeSets(modeSetTypes));
        }

        return mode;
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
                	ModeSetType[] modeSetTypes = userConfigType.getModeSetArray();
                    if (null != modeSetTypes &&
                    		modeSetTypes.length > 0) {
                        deployment.setUserConfigModeSets(createUserConfigModeSets(modeSetTypes));
                    }
                }

                deployments.add(deployment);
            }
        }

        return deployments;
    }

    private static Map<String, String> createDependencies(DependencyType[] dependencyTypes) {
        Map<String, String> dependencies = new HashMap<String, String>();
        for (DependencyType dependencyType : dependencyTypes) {
            dependencies.put(dependencyType.getServiceId(),
                             dependencyType.getContentId());
        }
        return dependencies;
    }

}
