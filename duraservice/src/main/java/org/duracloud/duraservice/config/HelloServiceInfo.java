package org.duracloud.duraservice.config;

import org.duracloud.serviceconfig.ServiceInfo;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class HelloServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {
        ServiceInfo helloService = new ServiceInfo();
        helloService.setId(index);
        helloService.setContentId("helloservice-" + version + ".jar");
        String desc = "The Hello service acts as a simple test case " +
            "for service deployment.";
        helloService.setDescription(desc);
        helloService.setDisplayName("Hello Service");
        helloService.setUserConfigVersion("1.0");
        helloService.setServiceVersion(version);
        helloService.setMaxDeploymentsAllowed(1);

        helloService.setSystemConfigs(getBaseSystemConfigs());

        helloService.setDeploymentOptions(getSimpleDeploymentOptions());

        return helloService;
    }
}
