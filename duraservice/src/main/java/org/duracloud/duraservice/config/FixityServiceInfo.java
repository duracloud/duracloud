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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class FixityServiceInfo extends AbstractServiceInfo {

    private final String userConfigVersion = "1.0";
    private final String displayName = "Bit Integrity Checker";

    protected enum ModeType {
        ALL_IN_ONE_LIST("all-in-one-for-list",
                        "Verify integrity of an item list"),
        ALL_IN_ONE_SPACE("all-in-one-for-space",
                         "Verify integrity of a Space");

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

    @Override
    public ServiceInfo getServiceXml(int index, String version) {
        ServiceInfo fsService = new ServiceInfo();
        fsService.setId(index);
        fsService.setContentId("fixityservice-" + version + ".zip");
        fsService.setDescription(getDescription());
        fsService.setDisplayName(displayName);
        fsService.setUserConfigVersion(userConfigVersion);
        fsService.setServiceVersion(version);
        fsService.setMaxDeploymentsAllowed(1); // single deployment until otherwise supported

        fsService.setSystemConfigs(getSystemConfigs());
        fsService.setUserConfigModeSets(getModeSets());
        fsService.setDeploymentOptions(getSimpleDeploymentOptions());

        return fsService;
    }

    private String getDescription() {
        return "The Bit Integrity Checker provides the ability to verify " +
            "that the content held within DuraCloud has maintained its bit " +
            "integrity. There are two modes of operation: " +
            "(1) All-in-one generation of system MD5s for items in provided " +
            "listing and verification " +
            "(2) All-in-one generation of system MD5s over entire space(s) " +
            "and verification to provided listing.";
    }

    private List<SystemConfig> getSystemConfigs() {
        List<SystemConfig> systemConfig = getBaseSystemConfigs();

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

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(context);
        systemConfig.add(username);
        systemConfig.add(password);
        return systemConfig;
    }

    private List<UserConfigModeSet> getModeSets() {
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();

        modes.add(getMode(ModeType.ALL_IN_ONE_SPACE));
        modes.add(getMode(ModeType.ALL_IN_ONE_LIST));

        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setModes(modes);
        modeSet.setDisplayName("Service Mode");
        modeSet.setName("mode");

        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(modeSet);
        return modeSets;
    }

    private UserConfigMode getMode(ModeType modeType) {
        List<UserConfigModeSet> modeSets = new ArrayList<UserConfigModeSet>();
        modeSets.add(getStorageProviderSelection(modeType));

        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        switch (modeType) {
            case ALL_IN_ONE_LIST:
                userConfigs.add(getContentIdOfProvidedListingConfig());
                break;
            case ALL_IN_ONE_SPACE:
                break;
            default:
                throw new RuntimeException("Unexpected ModeType: " + modeType);
        }

        UserConfigMode mode = new UserConfigMode();
        mode.setDisplayName(modeType.getDesc());
        mode.setName(modeType.getKey());
        mode.setUserConfigs(userConfigs);
        mode.setUserConfigModeSets(modeSets);
        return mode;
    }

    private UserConfigModeSet getStorageProviderSelection(ModeType modeType) {
        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setName("storeId");
        modeSet.setDisplayName("Stores");
        modeSet.setValue(ServiceConfigUtil.ALL_STORE_SPACES_VAR);

        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        switch (modeType) {
            case ALL_IN_ONE_LIST:
                userConfigs.add(getSpaceOfProvidedListingSelection());
                break;
            case ALL_IN_ONE_SPACE:
                userConfigs.add(getSpaceOfGeneratedHashListingSelection());
                break;
            default:
                throw new RuntimeException("Unexpected ModeType: " + modeType);
        }

        UserConfigMode mode = new UserConfigMode();
        mode.setUserConfigs(userConfigs);

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(mode);

        modeSet.setModes(modes);
        return modeSet;
    }

    private SingleSelectUserConfig getSpaceOfProvidedListingSelection() {
        return new SingleSelectUserConfig("providedListingSpaceIdA",
                                          "Space with input listing",
                                          getSpaceOptions());
    }

    private SingleSelectUserConfig getSpaceOfGeneratedHashListingSelection() {
        return new SingleSelectUserConfig("targetSpaceId",
                                          "Space containing content items",
                                          getSpaceOptions());
    }

    private TextUserConfig getContentIdOfProvidedListingConfig() {
        return new TextUserConfig("providedListingContentIdA",
                                  "Input listing name",
                                  "item-listing.csv");
    }

    private List<Option> getSpaceOptions() {
        List<Option> spaceOptions = new ArrayList<Option>();
        spaceOptions.add(new Option("Spaces",
                                    ServiceConfigUtil.SPACES_VAR,
                                    false));
        return spaceOptions;
    }

}