package org.duracloud.duraservice.config;

import org.duracloud.serviceconfig.ServiceInfo;

/**
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public class ImageMagickServiceInfo extends AbstractServiceInfo {
    @Override
    public ServiceInfo getServiceXml(int index, String version) {
        ServiceInfo imService = new ServiceInfo();
        imService.setId(index);
        imService.setContentId("imagemagickservice-" + version + ".zip");
        String desc = "The ImageMagick service deploys the ImageMagick " +
            "application which allows other services to take advantage of " +
            "its features.";
        imService.setDescription(desc);
        imService.setDisplayName("ImageMagick Service");
        imService.setUserConfigVersion("1.0");
        imService.setServiceVersion(version);
        imService.setMaxDeploymentsAllowed(1);

        imService.setDeploymentOptions(getSimpleDeploymentOptions());

        return imService;
    }
}
