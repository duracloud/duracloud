package org.duracloud.duraservice.config;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class CloudSyncServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo cloudSyncService = new ServiceInfo();
        cloudSyncService.setId(index);
        cloudSyncService.setContentId("cloudsyncservice-" + version + ".zip");
        String desc =
            "The CloudSync Service deploys an instance of the Fedora " +
                "CloudSync application which provides for synchronizing and " +
                "restoring your Fedora installation to/from DuraCloud.";
        cloudSyncService.setDescription(desc);
        cloudSyncService.setDisplayName("CloudSync");
        cloudSyncService.setUserConfigVersion("1.0");
        cloudSyncService.setServiceVersion(version);
        cloudSyncService.setMaxDeploymentsAllowed(1);

        // System Configs
        List<SystemConfig> systemConfig = getBaseSystemConfigs();

        SystemConfig host = new SystemConfig("host",
                                             ServiceConfigUtil.STORE_HOST_VAR,
                                             "localhost");
        SystemConfig port = new SystemConfig("port",
                                             ServiceConfigUtil.STORE_PORT_VAR,
                                             "8080");
        SystemConfig username = new SystemConfig("username",
                                                 ServiceConfigUtil.STORE_USER_VAR,
                                                 "no-username");
        SystemConfig password = new SystemConfig("password",
                                                 ServiceConfigUtil.STORE_PWORD_VAR,
                                                 "no-password");

        systemConfig.add(host);
        systemConfig.add(port);
        systemConfig.add(username);
        systemConfig.add(password);

        cloudSyncService.setSystemConfigs(systemConfig);

        cloudSyncService.setDeploymentOptions(getSimpleDeploymentOptions());

        // service dependencies
        Map<String, String> dependencies = new HashMap<String, String>();
        dependencies.put("6", "webapputilservice-" + version + ".zip");
        cloudSyncService.setDependencies(dependencies);

        return cloudSyncService;
    }
}
