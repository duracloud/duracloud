package org.duracloud.duraservice.config;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
import org.duracloud.storage.domain.HadoopTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Bill Branan
 *         Date: Sept 29, 2010
 */
public class ReplicationOnDemandServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo repService = new ServiceInfo();
        repService.setId(index);
        repService.setContentId("replication-on-demand-service-" + version + ".zip");
        String desc = "Duplicate on Demand provides a simple " +
            "way to copy content from one space to anther. This service " +
            "is primarily focused on copying content from a space " +
            "in the Amazon storage provider to a space in another provider. " +
            "To begin, a source space is chosen, along with a store and " +
            "space to which content will be copied. The service then " +
            "performs a copy of all content and properties in the source space " +
            "to the destination space, creating the space if necessary. " +
            "When the service has completed its work, a results file will be " +
            "stored and a set of files (primarily logs) " +
            "created as part of the process will be stored in the work space.";
        repService.setDescription(desc);
        repService.setDisplayName("Duplicate on Demand");
        repService.setUserConfigVersion("1.0");
        repService.setServiceVersion(version);
        repService.setMaxDeploymentsAllowed(1);
        repService.setUserConfigModeSets(getModeSets());

        // System Configs
        List<SystemConfig> systemConfig = new ArrayList<SystemConfig>();

        SystemConfig host = new SystemConfig("duraStoreHost",
                                             ServiceConfigUtil.STORE_HOST_VAR,
                                             "localhost");
        SystemConfig port = new SystemConfig("duraStorePort",
                                             ServiceConfigUtil.STORE_PORT_VAR,
                                             "8080");
        SystemConfig context = new SystemConfig("duraStoreContext",
                                                ServiceConfigUtil.STORE_CONTEXT_VAR,
                                                "durastore");
        SystemConfig username = new SystemConfig("username",
                                                 ServiceConfigUtil.STORE_USER_VAR,
                                                 "no-username");
        SystemConfig password = new SystemConfig("password",
                                                 ServiceConfigUtil.STORE_PWORD_VAR,
                                                 "no-password");
        SystemConfig mappersPerInstance = new SystemConfig("mappersPerInstance",
                                                           "1",
                                                           "1");

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(context);
        systemConfig.add(username);
        systemConfig.add(password);
        systemConfig.add(mappersPerInstance);

        repService.setSystemConfigs(systemConfig);
        repService.setDeploymentOptions(getSimpleDeploymentOptions());

        return repService;
    }

    private List<UserConfigModeSet> getModeSets() {
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();

        modes.add(getMode(ModeType.OPTIMIZE_STANDARD));
        modes.add(getMode(ModeType.OPTIMIZE_ADVANCED));

        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setModes(modes);
        modeSet.setDisplayName("Configuration");
        modeSet.setName("optimizeMode");

        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(modeSet);
        return modeSets;
    }

    private UserConfigMode getMode(ModeType modeType) {
        List<UserConfig> userConfigs = getDefaultUserConfigs();
        switch (modeType) {
            case OPTIMIZE_ADVANCED:
                userConfigs.add(getNumberOfInstancesSelection());
                userConfigs.add(getTypeOfInstanceListingSelection());
                break;
            case OPTIMIZE_STANDARD:
                // Removed optimize selection for now
                //userConfigs.add(getOptimizationSelection());
                break;
            default:
                throw new RuntimeException("Unexpected ModeType: " + modeType);
        }

        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(getStorageProviderSelection());

        UserConfigMode mode = new UserConfigMode();
        mode.setDisplayName(modeType.getDesc());
        mode.setName(modeType.getKey());
        mode.setUserConfigs(userConfigs);
        mode.setUserConfigModeSets(modeSets);
        return mode;
    }

    private List<UserConfig> getDefaultUserConfigs()
    {
        // User Configs
        List<UserConfig> repServiceUserConfig = new ArrayList<UserConfig>();

        // Space Options
        List<Option> spaceOptions = new ArrayList<Option>();
        Option spaces = new Option("Spaces",
                                   ServiceConfigUtil.SPACES_VAR,
                                   false);
        spaceOptions.add(spaces);

        // Store Options
        List<Option> storeOptions = new ArrayList<Option>();
        Option stores =
            new Option("Stores", ServiceConfigUtil.STORES_VAR, false);
        storeOptions.add(stores);

        SingleSelectUserConfig sourceSpace =
            new SingleSelectUserConfig("sourceSpaceId",
                                       "Source Space",
                                       spaceOptions);

        // Include all user configs
        repServiceUserConfig.add(sourceSpace);

        return repServiceUserConfig;
    }

    private SingleSelectUserConfig getNumberOfInstancesSelection() {
        // Number of instances
        List<Option> numInstancesOptions = new ArrayList<Option>();
        for (int i = 1; i < 20; i++) {
            Option op = new Option(String.valueOf(i), String.valueOf(i), false);
            numInstancesOptions.add(op);
        }

        return new SingleSelectUserConfig(
            "numInstances",
            "Number of Server Instances",
            numInstancesOptions);
    }

    private SingleSelectUserConfig getOptimizationSelection() {
        List<Option> options = new ArrayList<Option>();
        options.add(new Option("Optimize for cost",
                               "optimize_for_cost",
                               true));
        options.add(new Option("Optimize for speed",
                               "optimize_for_speed",
                               false));

        return new SingleSelectUserConfig(
            "optimizeType",
            "Optimize",
            options);
    }

    private SingleSelectUserConfig getTypeOfInstanceListingSelection() {
        // Instance type
        List<Option> instanceTypeOptions = new ArrayList<Option>();
        instanceTypeOptions.add(new Option(HadoopTypes.INSTANCES
                                               .SMALL.getDescription(),
                                           HadoopTypes.INSTANCES.SMALL.getId(),
                                           true));
        instanceTypeOptions.add(new Option(HadoopTypes.INSTANCES
                                               .LARGE.getDescription(),
                                           HadoopTypes.INSTANCES.LARGE.getId(),
                                           false));
        instanceTypeOptions.add(new Option(HadoopTypes.INSTANCES
                                               .XLARGE.getDescription(),
                                           HadoopTypes.INSTANCES.XLARGE.getId(),
                                           false));

        return new SingleSelectUserConfig(
            "instanceType",
            "Type of Server Instance",
            instanceTypeOptions);
    }

    private UserConfigModeSet getStorageProviderSelection() {
        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setName("repStoreId");
        modeSet.setDisplayName("Copy to this store");
        modeSet.setValue(ServiceConfigUtil.ALL_STORE_SPACES_VAR);

        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        userConfigs.add(new SingleSelectUserConfig("repSpaceId",
                                                   "Copy to this space",
                                                   getSpaceOptions()));

        UserConfigMode mode = new UserConfigMode();
        mode.setUserConfigs(userConfigs);

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(mode);

        modeSet.setModes(modes);
        return modeSet;
    }

    private List<Option> getSpaceOptions() {
        List<Option> spaceOptions = new ArrayList<Option>();
        spaceOptions.add(new Option("Spaces",
                                    ServiceConfigUtil.SPACES_VAR,
                                    false));
        return spaceOptions;
    }

    protected enum ModeType {
        OPTIMIZE_ADVANCED("advanced",
                          "Advanced"),
        OPTIMIZE_STANDARD("standard",
                          "Standard");

        private String key;
        private String desc;

        private ModeType(String key, String desc) {
            this.key = key;
            this.desc = desc;
        }

        public String toString() {
            return getKey();
        }

        protected String getKey() {
            return key;
        }

        protected String getDesc() {
            return desc;
        }
    }
}
