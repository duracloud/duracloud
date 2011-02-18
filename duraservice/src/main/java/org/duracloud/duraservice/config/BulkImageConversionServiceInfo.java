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
 *         Date: Aug 19, 2010
 */
public class BulkImageConversionServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo icService = new ServiceInfo();
        icService.setId(index);
        icService.setContentId("bulkimageconversionservice-" + version + ".zip");
        String desc = "The Bulk Image Transformer provides a simple " +
            "way to transform image files from one format to another in bulk. " +
            "A space is selected from which image files will be read and " +
            "converted to the chosen format. The transformed image files will " +
            "be stored in the destination space along with a file which " +
            "details the results of the transformation process. The working " +
            "space will be used to store files used for processing and logs. " +
            "Note that this service can only be used for content stored in " +
            "Amazon.";
        icService.setDescription(desc);
        icService.setDisplayName("Image Transformer - Bulk");
        icService.setUserConfigVersion("1.0");
        icService.setServiceVersion(version);
        icService.setMaxDeploymentsAllowed(1);


        icService.setUserConfigModeSets(getModeSets());

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

        icService.setSystemConfigs(systemConfig);

        icService.setDeploymentOptions(getSimpleDeploymentOptions());

        return icService;
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
        mode.setUserConfigModeSets(null);
        return mode;
    }

    private List<UserConfig> getDefaultUserConfigs()
    {
        // User Configs
        List<UserConfig> icServiceUserConfig = new ArrayList<UserConfig>();

        // Source space
        List<Option> spaceOptions = new ArrayList<Option>();
        Option spaces = new Option("Spaces",
                                   ServiceConfigUtil.SPACES_VAR,
                                   false);
        spaceOptions.add(spaces);

        SingleSelectUserConfig sourceSpace = new SingleSelectUserConfig(
            "sourceSpaceId",
            "Source Space",
            spaceOptions);

        SingleSelectUserConfig destSpace = new SingleSelectUserConfig(
            "destSpaceId",
            "Destination Space",
            spaceOptions);

        // To Format
        List<Option> toFormatOptions = new ArrayList<Option>();
        Option gif = new Option("GIF", "gif", false);
        Option jpg = new Option("JPG", "jpg", false);
        Option png = new Option("PNG", "png", false);
        Option tiff = new Option("TIFF", "tiff", false);
        Option jp2 = new Option("JP2", "jp2", false);
        Option bmp = new Option("BMP", "bmp", false);
        Option pdf = new Option("PDF", "pdf", false);
        Option psd = new Option("PSD", "psd", false);
        toFormatOptions.add(gif);
        toFormatOptions.add(jpg);
        toFormatOptions.add(png);
        toFormatOptions.add(tiff);
        toFormatOptions.add(jp2);
        toFormatOptions.add(bmp);
        toFormatOptions.add(pdf);
        toFormatOptions.add(psd);

        SingleSelectUserConfig toFormat =
            new SingleSelectUserConfig("toFormat",
                                       "Format",
                                       toFormatOptions);

        List<Option> colorSpaceOptions = new ArrayList<Option>();
        colorSpaceOptions.add(new Option("Source Image Color Space",
                                         "source",
                                         true));
        colorSpaceOptions.add(new Option("sRGB", "sRGB", false));

        SingleSelectUserConfig colorSpace = new SingleSelectUserConfig(
            "colorSpace",
            "Color Space",
            colorSpaceOptions);

        // Name Prefix
        TextUserConfig namePrefix =
            new TextUserConfig("namePrefix",
                               "Source file name prefix, only files " +
                               "beginning with this value will be converted.",
                               "");

        // Name Suffix
        TextUserConfig nameSuffix =
            new TextUserConfig("nameSuffix",
                               "Source file name suffix, only files ending " +
                               "with this value will be converted.",
                               "");

        // Include all user configs
        icServiceUserConfig.add(sourceSpace);
        icServiceUserConfig.add(namePrefix);
        icServiceUserConfig.add(nameSuffix);
        icServiceUserConfig.add(destSpace);
        icServiceUserConfig.add(toFormat);
        icServiceUserConfig.add(colorSpace);

        return icServiceUserConfig;
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
