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
public class DuplicationServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo repService = new ServiceInfo();
        repService.setId(index);
        repService.setContentId("duplicationservice-" + version + ".zip");
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

        repServiceUserConfig.add(fromStoreId);
        repServiceUserConfig.add(toStoreId);

        repService.setUserConfigModeSets(createDefaultModeSet(repServiceUserConfig));

        // System Configs
        List<SystemConfig> systemConfig = getBaseSystemConfigs();

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
