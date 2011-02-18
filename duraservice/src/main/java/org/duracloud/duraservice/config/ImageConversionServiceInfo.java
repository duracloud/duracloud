package org.duracloud.duraservice.config;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class ImageConversionServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo icService = new ServiceInfo();
        icService.setId(index);
        icService.setContentId("imageconversionservice-" + version + ".zip");
        String desc = "The Image Transformer provides a simple way to " +
            "transform image files from one format to another. A space is " +
            "selected from which image files will be read and converted to " +
            "the chosen format. The transformed image files will be stored in " +
            "the destination space along with a file which details the " +
            "results of the transformation process. Note that the System " +
            "Transformer Utility must be deployed prior to using the Image " +
            "Transformer.";
        icService.setDescription(desc);
        icService.setDisplayName("Image Transformer");
        icService.setUserConfigVersion("1.0");
        icService.setServiceVersion(version);
        icService.setMaxDeploymentsAllowed(1);

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
                               "beginning with this value will be transformed.",
                               "");

        // Name Suffix
        TextUserConfig nameSuffix =
            new TextUserConfig("nameSuffix",
                               "Source file name suffix, only files ending " +
                               "with this value will be transformed.",
                               "");

        icServiceUserConfig.add(sourceSpace);
        icServiceUserConfig.add(namePrefix);
        icServiceUserConfig.add(nameSuffix);
        icServiceUserConfig.add(destSpace);
        icServiceUserConfig.add(toFormat);
        icServiceUserConfig.add(colorSpace);

        icService.setUserConfigModeSets(createDefaultModeSet(icServiceUserConfig));

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
        SystemConfig threads = new SystemConfig("threads", "1", "1");

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(context);
        systemConfig.add(username);
        systemConfig.add(password);
        systemConfig.add(threads);

        icService.setSystemConfigs(systemConfig);

        icService.setDeploymentOptions(getSimpleDeploymentOptions());

        return icService;
    }
}
