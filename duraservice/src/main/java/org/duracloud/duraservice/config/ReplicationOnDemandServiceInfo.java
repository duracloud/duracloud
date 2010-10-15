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
 * @author Bill Branan
 *         Date: Sept 29, 2010
 */
public class ReplicationOnDemandServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo repService = new ServiceInfo();
        repService.setId(index);
        repService.setContentId("replication-on-demand-service-" + version + ".zip");
        String desc = "The Replication On Demand service provides a simple " +
            "way to replicate content from one space to anther. This service " +
            "is primarily focused on allowing the replication of content " +
            "from the primary storage provider to a secondary provider.  " +
            "To begin, a source space is chosen, along with a store and " +
            "space to which content will be replicated. The service then " +
            "performs a copy of all content and metadata in the source space " +
            "to the replicated space, creating the space if necessary. " +
            "When the service has completed its work, a results file will be " +
            "stored in the chosen space and a set of files (primarily logs) " +
            "created as part of the process will be stored in the work space.";
        repService.setDescription(desc);
        repService.setDisplayName("Replication On Demand Service");
        repService.setUserConfigVersion("1.0");
        repService.setServiceVersion(version);
        repService.setMaxDeploymentsAllowed(1);

        // User Configs
        List<UserConfig> repServiceUserConfig = new ArrayList<UserConfig>();

        // Space Options
        List<Option> spaceOptions = new ArrayList<Option>();
        Option spaces = new Option("Spaces",
                                   ServiceConfigUtil.SPACES_VAR,
                                   false);
        spaceOptions.add(spaces);

        // Store Options
        List<Option> storeOptions = new ArrayList<Option>();
        Option stores =
            new Option("Stores", ServiceConfigUtil.STORES_VAR, false);
        storeOptions.add(stores);

        SingleSelectUserConfig sourceSpace = 
            new SingleSelectUserConfig("sourceSpaceId",
                                       "Source Space",
                                       spaceOptions);

        SingleSelectUserConfig repStore =
            new SingleSelectUserConfig("repStoreId",
                                       "Replicate to this store",
                                       storeOptions);

        TextUserConfig repSpace =
            new TextUserConfig("repSpaceId", "Replicate to this space");

        SingleSelectUserConfig destSpace =
            new SingleSelectUserConfig("destSpaceId",
                                       "Store results file in this space",
                                       spaceOptions);

        SingleSelectUserConfig workSpace =
            new SingleSelectUserConfig("workSpaceId",
                                       "Working Space",
                                       spaceOptions);

        // Number of instances
        List<Option> numInstancesOptions = new ArrayList<Option>();
        for(int i = 1; i<20; i++) {
            Option op = new Option(String.valueOf(i), String.valueOf(i), false);
            numInstancesOptions.add(op);
        }
        SingleSelectUserConfig numInstances =
            new SingleSelectUserConfig("numInstances",
                                       "Number of Server Instances",
                                       numInstancesOptions);

        // Instance type
        List<Option> instanceTypeOptions = new ArrayList<Option>();
        instanceTypeOptions.add(new Option("Small Instance", "m1.small", true));
        instanceTypeOptions.add(new Option("Large Instance", "m1.large", false));
        instanceTypeOptions.add(
            new Option("Extra Large Instance", "m1.xlarge", false));

        SingleSelectUserConfig instanceType =
            new SingleSelectUserConfig("instanceType",
                                       "Type of Server Instance",
                                       instanceTypeOptions);

        // Include all user configs
        repServiceUserConfig.add(sourceSpace);
        repServiceUserConfig.add(repStore);
        repServiceUserConfig.add(repSpace);
        repServiceUserConfig.add(destSpace);
        repServiceUserConfig.add(workSpace);
        repServiceUserConfig.add(numInstances);
        repServiceUserConfig.add(instanceType);

        repService.setUserConfigs(repServiceUserConfig);

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

        repService.setSystemConfigs(systemConfig);

        repService.setDeploymentOptions(getSimpleDeploymentOptions());

        return repService;
    }
}
