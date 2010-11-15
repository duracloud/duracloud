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
                        "Verify the integrity of a list of items"),
        ALL_IN_ONE_SPACE("all-in-one-for-space",
                         "Verify the integrity of an entire Space"),
        GENERATE_LIST("generate-for-list",
                      "Generate integrity information for a list of items"),
        GENERATE_SPACE("generate-for-space",
                       "Generate integrity information for an entire Space"),
        COMPARE("compare", "Compare two different integrity reports");

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
        fsService.setUserConfigs(getUserConfigs());
        fsService.setModeSets(getModeSets());
        fsService.setDeploymentOptions(getSimpleDeploymentOptions());

        return fsService;
    }

    private String getDescription() {
        return "The Bit Integrity Checker provides the ability to verify " +
            "that the content held within DuraCloud has maintained its bit " +
            "integrity. There are five modes of operation: " +
            "(1) All-in-one generation of system MD5s for items in provided " +
            "listing and verification " +
            "(2) All-in-one generation of system MD5s over entire space(s) " +
            "and verification to provided listing " +
            "(3) Generation of system MD5 listing for items in provided listing" +
            "(4) Generation of system MD5 listing over entire space, and " +
            "(5) Comparison of two provided MD5 listings.";
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

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(context);
        systemConfig.add(username);
        systemConfig.add(password);
        return systemConfig;
    }

    private List<UserConfig> getUserConfigs() {
        return new ArrayList<UserConfig>();
    }

    private List<UserConfigModeSet> getModeSets() {
        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();

        modes.add(getMode(ModeType.ALL_IN_ONE_LIST));
        modes.add(getMode(ModeType.ALL_IN_ONE_SPACE));
        modes.add(getMode(ModeType.GENERATE_LIST));
        modes.add(getMode(ModeType.GENERATE_SPACE));
        modes.add(getMode(ModeType.COMPARE));

        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setModes(modes);
        modeSet.setName("mode");
        modeSet.setDisplayName("Service Mode");

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
                userConfigs.add(getHashMethodSelection());
                userConfigs.add(getContentIdOfProvidedListingConfig());
                userConfigs.add(getContentIdOfGeneratedListingConfig());
                userConfigs.add(getContentIdOfReportConfig());
                break;
            case ALL_IN_ONE_SPACE:
                userConfigs.add(getHashMethodSelection());
                userConfigs.add(getContentIdOfProvidedListingConfig());
                userConfigs.add(getContentIdOfGeneratedListingConfig());
                userConfigs.add(getContentIdOfReportConfig());
                break;
            case GENERATE_LIST:
                userConfigs.add(getHashMethodSelection());
                userConfigs.add(getContentIdOfProvidedListingConfig());
                userConfigs.add(getContentIdOfGeneratedListingConfig());
                break;
            case GENERATE_SPACE:
                userConfigs.add(getHashMethodSelection());
                userConfigs.add(getContentIdOfGeneratedListingConfig());
                break;
            case COMPARE:
                userConfigs.add(getContentIdOfProvidedListingConfig());
                userConfigs.add(getContentIdOfProvidedListingBConfig());
                userConfigs.add(getContentIdOfReportConfig());
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
        modeSet.setName("stores");
        modeSet.setDisplayName("Stores");
        modeSet.setValue(ServiceConfigUtil.ALL_STORE_SPACES_VAR);

        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        switch (modeType) {
            case ALL_IN_ONE_LIST:
                userConfigs.add(getSpaceOfProvidedListingSelection());
                break;
            case ALL_IN_ONE_SPACE:
                userConfigs.add(getSpaceOfProvidedListingSelection());
                userConfigs.add(getSpaceOfGeneratedHashListingSelection());
                break;
            case GENERATE_LIST:
                userConfigs.add(getSpaceOfProvidedListingSelection());
                break;
            case GENERATE_SPACE:
                userConfigs.add(getSpaceOfGeneratedHashListingSelection());
                break;
            case COMPARE:
                userConfigs.add(getSpaceOfProvidedListingSelection());
                userConfigs.add(getSpaceOfProvidedListingBSelection());
                break;
            default:
                throw new RuntimeException("Unexpected ModeType: " + modeType);
        }

        userConfigs.add(getSpaceOfOutputConfig());

        UserConfigMode mode = new UserConfigMode();
        mode.setUserConfigs(userConfigs);

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(mode);

        modeSet.setModes(modes);
        return modeSet;
    }

    private SingleSelectUserConfig getHashMethodSelection() {
        List<Option> trustLevelOptions = new ArrayList<Option>();
        trustLevelOptions.add(new Option("The storage provider",
                                         "stored",
                                         false));
        trustLevelOptions.add(new Option("The files themselves",
                                         "generated",
                                         false));
        trustLevelOptions.add(new Option("The files themselves, with salt",
                                         "salted",
                                         false));

        return new SingleSelectUserConfig("hashApproach",
                                          "Get integrity information from...",
                                          trustLevelOptions);
    }

    private UserConfig getSaltConfig() {
        String emptyValue = "";
        return new TextUserConfig("salt", "Salt", emptyValue);
    }

    private SingleSelectUserConfig getFailFastBoolean() {
        return new SingleSelectUserConfig("failFast",
                                          "Exit Immediately on Failure or Error",
                                          getBooleanOptions());
    }

    private List<Option> getBooleanOptions() {
        List<Option> booleanOptions = new ArrayList<Option>();
        booleanOptions.add(new Option("true", "true", false));
        booleanOptions.add(new Option("false", "false", false));
        return booleanOptions;
    }

    private SingleSelectUserConfig getStorageProviderSelection() {
        List<Option> storeOptions = new ArrayList<Option>();
        Option stores = new Option("Stores",
                                   ServiceConfigUtil.STORES_VAR,
                                   false);
        storeOptions.add(stores);
        return new SingleSelectUserConfig("storeId", "Store", storeOptions);
    }

    private SingleSelectUserConfig getSpaceOfProvidedListingSelection() {
        return new SingleSelectUserConfig("providedListingSpaceIdA",
                                          "Space with input listing",
                                          getSpaceOptions());
    }

    private SingleSelectUserConfig getSpaceOfProvidedListingBSelection() {
        return new SingleSelectUserConfig("providedListingSpaceIdB",
                                          "Space with second input listing",
                                          getSpaceOptions(),
                                          ModeType.COMPARE.getKey());
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

    private TextUserConfig getContentIdOfProvidedListingBConfig() {
        return new TextUserConfig("providedListingContentIdB",
                                  "Second input listing name",
                                  "fingerprints.csv");
    }

    private SingleSelectUserConfig getSpaceOfOutputConfig() {
        return new SingleSelectUserConfig("outputSpaceId",
                                          "Output space",
                                          getSpaceOptions());
    }

    private List<Option> getSpaceOptions() {
        List<Option> spaceOptions = new ArrayList<Option>();
        spaceOptions.add(new Option("Spaces",
                                    ServiceConfigUtil.SPACES_VAR,
                                    false));
        return spaceOptions;
    }

    private TextUserConfig getContentIdOfGeneratedListingConfig() {
        return new TextUserConfig("outputContentId",
                                  "Output listing name",
                                  "fingerprints.csv");
    }

    private UserConfig getContentIdOfReportConfig() {
        return new TextUserConfig("reportContentId",
                                  "Output report name",
                                  "integrity-report.csv");
    }

}