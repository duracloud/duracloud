package org.duracloud.duraservice.config;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;

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
            "for applying changes to secondary storage providers. When " +
            "running, Duplicate on Change will watch for changes in spaces " +
            "within a selected storage provider and apply those changes to " +
            "spaces in the specified provider(s).";
        repService.setDescription(desc);
        repService.setDisplayName("Duplicate on Change");
        repService.setUserConfigVersion("1.0");
        repService.setServiceVersion(version);
        repService.setMaxDeploymentsAllowed(1);

        repService.setUserConfigModeSets(getUserConfig());
        repService.setSystemConfigs(getSystemConfig());
        repService.setDeploymentOptions(getSimpleDeploymentOptions());

        return repService;
    }

    private List<UserConfigModeSet> getUserConfig() {
        List<UserConfigModeSet> modeSetList = new ArrayList<UserConfigModeSet>();

        UserConfigModeSet modeSet = new UserConfigModeSet();
        modeSet.setName("fromStoreId");
        modeSet.setDisplayName("Store to watch");
        modeSet.setValue(ServiceConfigUtil.ALL_STORE_SPACES_VAR);

        List<Option> storeOptions = new ArrayList<Option>();
        storeOptions.add(new Option("Not Duplicated", "none", true));
        storeOptions.add(
            new Option("Stores", ServiceConfigUtil.STORES_VAR, false));

        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        userConfigs.add(
            new MultiSelectUserConfig(ServiceConfigUtil.SPACES_CONFIG_VAR,
                                      "Space",
                                      storeOptions));

        UserConfigMode mode = new UserConfigMode();
        mode.setUserConfigs(userConfigs);

        List<UserConfigMode> modes = new ArrayList<UserConfigMode>();
        modes.add(mode);

        modeSet.setModes(modes);
        modeSetList.add(modeSet);
        return modeSetList;
    }

    private List<SystemConfig> getSystemConfig() {
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

        return systemConfig;
    }
}
