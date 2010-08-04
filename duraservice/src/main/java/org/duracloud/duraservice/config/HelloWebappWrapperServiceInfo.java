package org.duracloud.duraservice.config;

import org.duracloud.serviceconfig.ServiceInfo;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class HelloWebappWrapperServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {
        ServiceInfo hellowebapp = new ServiceInfo();
        hellowebapp.setId(index);
        hellowebapp.setContentId("hellowebappwrapper-" + version + ".zip");
        String desc = "The HelloWebApp wrapper deploys a simple web " +
            "application which prints a pleasant greeting.";
        hellowebapp.setDescription(desc);
        hellowebapp.setDisplayName("Hello WebApp Wrapper");
        hellowebapp.setUserConfigVersion("1.0");
        hellowebapp.setServiceVersion(version);
        hellowebapp.setMaxDeploymentsAllowed(1);

        hellowebapp.setDeploymentOptions(getSimpleDeploymentOptions());

        return hellowebapp;
    }
}
