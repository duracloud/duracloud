package org.duracloud.duraservice.config;

import org.duracloud.serviceconfig.ServiceInfo;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class WebappUtilServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo webappUtilService = new ServiceInfo();
        webappUtilService.setId(index);
        webappUtilService.setContentId(
            "webapputilservice-" + version + ".zip");
        String desc = "The System WebApp Utility coordinates the " +
            "(de)installation and startup/shutdown of Apache Tomcat instances" +
            " that are created to run web application services that deployed " +
            "externally to the hosting OSGi container.";
        webappUtilService.setDescription(desc);
        webappUtilService.setDisplayName("System Utility - WebApp");
        webappUtilService.setUserConfigVersion("1.0");
        webappUtilService.setServiceVersion(version);
        webappUtilService.setMaxDeploymentsAllowed(1);

        webappUtilService.setDeploymentOptions(getSimpleDeploymentOptions());

        return webappUtilService;
    }
}
