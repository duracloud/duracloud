package org.duracloud.duraservice.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.duracloud.duraservice.mgmt.ServiceConfigUtil;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.SystemConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;
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
        String desc = "The Bulk Bit Integrity Checker provides a simple way to " +
            "determine checksums (MD5s) for all content items in any " +
            "particular space by leveraging an Amazon Hadoop cluster. Note " +
            "that this service can only be run over content stored in Amazon.";
        info.setDescription(desc);
        info.setDisplayName("Bulk Bit Integrity Checker");
        info.setUserConfigVersion("1.0");
        info.setServiceVersion(version);
        info.setMaxDeploymentsAllowed(1);

        // User Configs
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();

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

        SingleSelectUserConfig workSpace = new SingleSelectUserConfig(
            "workSpaceId",
            "Working Space",
            spaceOptions);

        // Number of instances
        List<Option> numInstancesOptions = new ArrayList<Option>();
        for (int i = 1; i < 20; i++) {
            Option op = new Option(String.valueOf(i), String.valueOf(i), false);
            numInstancesOptions.add(op);
        }
        SingleSelectUserConfig numInstances = new SingleSelectUserConfig(
            "numInstances",
            "Number of Server Instances",
            numInstancesOptions);

        // Instance type
        List<Option> instanceTypeOptions = new ArrayList<Option>();
        instanceTypeOptions.add(new Option(INSTANCES.SMALL.getDescription(),
                                           INSTANCES.SMALL.getId(),
                                           true));
        instanceTypeOptions.add(new Option(INSTANCES.LARGE.getDescription(),
                                           INSTANCES.LARGE.getId(),
                                           false));
        instanceTypeOptions.add(new Option(INSTANCES.XLARGE.getDescription(),
                                           INSTANCES.XLARGE.getId(),
                                           false));

        SingleSelectUserConfig instanceType = new SingleSelectUserConfig(
            "instanceType",
            "Type of Server Instance",
            instanceTypeOptions);

        // Include all user configs
        userConfigs.add(sourceSpace);
        userConfigs.add(destSpace);
        userConfigs.add(workSpace);
        userConfigs.add(numInstances);
        userConfigs.add(instanceType);
        info.setUserConfigModeSets(createDefaultModeSet(userConfigs));

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

        info.setSystemConfigs(systemConfig);

        info.setDeploymentOptions(getSimpleDeploymentOptions());

        return info;
    }
}
