package org.duracloud.duraservice.config;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class MediaStreamingServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {
        ServiceInfo msService = new ServiceInfo();
        msService.setId(index);
        msService.setContentId("mediastreamingservice-" + version + ".zip");
        String desc = "The Media Streamer provides streaming for video and " +
            "audio files. This service takes advantage of the Amazon " +
            "Cloudfront streaming capabilities, so files to be streamed must " +
            "be within a space on an Amazon provider. Media to be streamed " +
            "by this service can be in multiple spaces. More information " +
            "about which file formats can be streamed can be found in the " +
            "Cloudfront documentation. After the service has started, the " +
            "space chosen as the viewer space will include a playlist for " +
            "each media space (which will include all items in a space) as " +
            "well as example html files which can be used to display a viewer.";
        msService.setDescription(desc);
        msService.setDisplayName("Media Streamer");
        msService.setUserConfigVersion("1.0");
        msService.setServiceVersion(version);
        msService.setMaxDeploymentsAllowed(1);

        // User Configs
        List<UserConfig> msServiceUserConfig = new ArrayList<UserConfig>();

        // Source space
        List<Option> spaceOptions = new ArrayList<Option>();
        Option spaces = new Option("Spaces",
                                   ServiceConfigUtil.SPACES_VAR,
                                   false);
        spaceOptions.add(spaces);

        MultiSelectUserConfig mediaSourceSpace = new MultiSelectUserConfig(
            "mediaSourceSpaceId",
            "Source Media Space (select multiple spaces by holding ctrl or cmd)",
            spaceOptions);

        SingleSelectUserConfig mediaViewerSpace = new SingleSelectUserConfig(
            "mediaViewerSpaceId",
            "Viewer Space",
            spaceOptions);


        msServiceUserConfig.add(mediaSourceSpace);
        msServiceUserConfig.add(mediaViewerSpace);

        msService.setUserConfigModeSets(createDefaultModeSet(msServiceUserConfig));

        // System Configs
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
        SystemConfig brokerURL = new SystemConfig("brokerURL",
                                                  ServiceConfigUtil.STORE_MSG_BROKER_VAR,
                                                  "tcp://localhost:61617");

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(context);
        systemConfig.add(username);
        systemConfig.add(password);
        systemConfig.add(brokerURL);

        msService.setSystemConfigs(systemConfig);

        msService.setDeploymentOptions(getSimpleDeploymentOptions());

        return msService;

    }
}
