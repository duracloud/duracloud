package org.duracloud.duraservice.config;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class ReplicationServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo repService = new ServiceInfo();
        repService.setId(index);
        repService.setContentId("replicationservice-" + version + ".zip");
        String desc = "Duplicate on Change provides a simple mechanism " +
            "for applying changes to a secondary storage provider. When " +
            "running, Duplicate on Change will watch for changes in " +
            "your primary storage provider and apply those changes to a " +
            "specified secondary provider.";
        repService.setDescription(desc);
        repService.setDisplayName("Duplicate on Change");
        repService.setUserConfigVersion("1.0");
        repService.setServiceVersion(version);
        repService.setMaxDeploymentsAllowed(1);

        // User Configs
        List<UserConfig> repServiceUserConfig = new ArrayList<UserConfig>();

        // Store Options (from/to)
        List<Option> storeOptions = new ArrayList<Option>();
        Option stores =
            new Option("Stores", ServiceConfigUtil.STORES_VAR, false);
        storeOptions.add(stores);

        SingleSelectUserConfig fromStoreId =
            new SingleSelectUserConfig("fromStoreId",
                                       "Watch this store for changes",
                                       storeOptions);

        SingleSelectUserConfig toStoreId =
            new SingleSelectUserConfig("toStoreId",
                                       "Apply to this store",
                                       storeOptions);

        /* These features have not been implemented as part of the service yet.
        // Replication Type
        List<Option> repTypeOptions = new ArrayList<Option>();
        Option repType1 =
            new Option("Sync Current Content", "1", false);
        Option repType2 =
            new Option("Replicate on Update", "2", false);
        Option repType3 =
            new Option("Sync Current Content then Replicate On Update",
                       "3",
                       false);
        repTypeOptions.add(repType1);
        repTypeOptions.add(repType2);
        repTypeOptions.add(repType3);

        SingleSelectUserConfig repType =
            new SingleSelectUserConfig("replicationType",
                                       "Replicataion Style",
                                       repTypeOptions);

        // Replicate spaces filter
        List<Option> spaceOptions = new ArrayList<Option>();
        Option spaces =
            new Option("Spaces", ServiceConfigUtil.SPACES_VAR, false);
        spaceOptions.add(spaces);

        MultiSelectUserConfig repSpaces =
            new MultiSelectUserConfig("replicateSpaces",
                                      "Only replicate content in these spaces",
                                      spaceOptions);

        // Mime type filter
        TextUserConfig repMimeTypes =
            new TextUserConfig("replicateMimetypes",
                               "Only replicate content with these MIME " +
                                   "types (separate with commas)", "");
        */

        repServiceUserConfig.add(fromStoreId);
        repServiceUserConfig.add(toStoreId);
        /*
        repServiceUserConfig.add(repType);
        repServiceUserConfig.add(repSpaces);
        repServiceUserConfig.add(repMimeTypes);
        */

        repService.setUserConfigModeSets(createDefaultModeSet(repServiceUserConfig));

        // System Configs
        List<SystemConfig> systemConfig = new ArrayList<SystemConfig>();

        SystemConfig host = new SystemConfig("host",
                                             ServiceConfigUtil.STORE_HOST_VAR,
                                             "localhost");
        SystemConfig port = new SystemConfig("port",
                                             ServiceConfigUtil.STORE_PORT_VAR,
                                             "8080");
        SystemConfig context = new SystemConfig("context",
                                                ServiceConfigUtil.STORE_CONTEXT_VAR,
                                                "durastore");
        SystemConfig brokerURL = new SystemConfig("brokerURL",
                                                  ServiceConfigUtil.STORE_MSG_BROKER_VAR,
                                                  "tcp://localhost:61617");
        SystemConfig username = new SystemConfig("username",
                                                 ServiceConfigUtil.STORE_USER_VAR,
                                                 "no-username");
        SystemConfig password = new SystemConfig("password",
                                                 ServiceConfigUtil.STORE_PWORD_VAR,
                                                 "no-password");

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(context);
        systemConfig.add(brokerURL);
        systemConfig.add(username);
        systemConfig.add(password);

        repService.setSystemConfigs(systemConfig);

        repService.setDeploymentOptions(getSimpleDeploymentOptions());

        return repService;
    }
}
