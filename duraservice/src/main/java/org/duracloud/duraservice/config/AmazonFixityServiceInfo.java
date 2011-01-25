package org.duracloud.duraservice.config;

import java.util.ArrayList;
import java.util.List;

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
import org.duracloud.storage.domain.HadoopTypes.INSTANCES;

/**
 * @author Andrew Woods
 *         Date: Sept 22, 2010
 */
public class AmazonFixityServiceInfo extends AbstractServiceInfo {

    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo info = new ServiceInfo();
        info.setId(index);
        info.setContentId("amazonfixityservice-" + version + ".zip");
        String desc = "The Bulk Bit Integrity Checker provides a simple way " +
            "to determine checksums (MD5s) for all content items in any " +
            "particular space by leveraging an Amazon Hadoop cluster. Note " +
            "that this service can only be run over content stored in Amazon.";
        info.setDescription(desc);
        info.setDisplayName("Bit Integrity Checker - Bulk");
        info.setUserConfigVersion("1.0");
        info.setServiceVersion(version);
        info.setMaxDeploymentsAllowed(1);

        info.setUserConfigModeSets(getModeSets());
        info.setSystemConfigs(getSystemConfigs());
        info.setDeploymentOptions(getSimpleDeploymentOptions());

        return info;
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

        UserConfigMode mode = new UserConfigMode();
        mode.setDisplayName(modeType.getDesc());
        mode.setName(modeType.getKey());
        mode.setUserConfigs(userConfigs);
        mode.setUserConfigModeSets(getListModeSets());
        return mode;
    }

    private List<UserConfig> getDefaultUserConfigs()
    {
        // User Configs
        List<UserConfig> userConfig = new ArrayList<UserConfig>();

        // Include all user configs
        userConfig.add(getTargetSpace());

        return userConfig;
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

    protected enum ModeType {
        OPTIMIZE_ADVANCED("advanced",
                          "Advanced"),
        OPTIMIZE_STANDARD("standard",
                          "Standard"),
        SERVICE_PROVIDES_BOTH_LISTS("all-in-one-for-list",
                                    "Verify integrity of a Space"),
        USER_PROVIDES_ONE_OF_LISTS("compare",
                                   "Verify integrity from an item list");

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

    private List<UserConfigModeSet> getListModeSets() {
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();

        modes.add(getListMode(ModeType.SERVICE_PROVIDES_BOTH_LISTS));
        modes.add(getListMode(ModeType.USER_PROVIDES_ONE_OF_LISTS));

        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setModes(modes);
        modeSet.setDisplayName("Service Mode");
        modeSet.setName("mode");

        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(modeSet);
        return modeSets;
    }

    private UserConfigMode getListMode(ModeType modeType) {
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();

        switch (modeType) {
            case SERVICE_PROVIDES_BOTH_LISTS:
                break;
            case USER_PROVIDES_ONE_OF_LISTS:
                userConfigs.add(getSpaceOfProvidedListingSelection());
                userConfigs.add(getContentIdOfProvidedListingConfig());
                break;
            default:
                throw new RuntimeException("Unexpected ModeType: " + modeType);
        }

        UserConfigMode mode = new UserConfigMode();
        mode.setDisplayName(modeType.getDesc());
        mode.setName(modeType.getKey());
        mode.setUserConfigs(userConfigs);
        mode.setUserConfigModeSets(new ArrayList<UserConfigModeSet>());
        return mode;
    }

    private TextUserConfig getContentIdOfProvidedListingConfig() {
        return new TextUserConfig("providedListingContentIdB",
                                  "Input listing name",
                                  "fingerprints.csv");
    }

    private SingleSelectUserConfig getSpaceOfProvidedListingSelection() {
        return new SingleSelectUserConfig("providedListingSpaceIdB",
                                          "Space with input listing",
                                          getSpaceOptions());
    }

    private SingleSelectUserConfig getTargetSpace() {
        return new SingleSelectUserConfig("sourceSpaceId",
                                          "Space to verify",
                                          getSpaceOptions());
    }

    private List<Option> getSpaceOptions() {
        List<Option> spaceOptions = new ArrayList<Option>();
        spaceOptions.add(new Option("Spaces",
                                    ServiceConfigUtil.SPACES_VAR,
                                    false));
        return spaceOptions;
    }

    private List<SystemConfig> getSystemConfigs() {
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
        return systemConfig;
    }
}
