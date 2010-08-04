package org.duracloud.duraservice.config;

import org.duracloud.serviceconfig.ServiceInfo;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class J2kServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {

        ServiceInfo j2kService = new ServiceInfo();
        j2kService.setId(index);
        j2kService.setContentId("j2kservice-" + version + ".zip");
        String desc =
            "The J2K service deploys an instance of the Adore Djatoka web " +
            "application which provides for serving and viewing JPEG2000 " +
            "images. Note that in order to view images using the J2K " +
            "service, the images must be in an OPEN space.";
        j2kService.setDescription(desc);
        j2kService.setDisplayName("JPEG 2000 Image Viewer Service");
        j2kService.setUserConfigVersion("1.0");
        j2kService.setServiceVersion(version);
        j2kService.setMaxDeploymentsAllowed(1);

        j2kService.setDeploymentOptions(getSimpleDeploymentOptions());

        return j2kService;
    }
}
