package org.duracloud.duraservice.config;

import org.duracloud.serviceconfig.DeploymentOption;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.serviceconfig.user.UserConfigMode;
import org.duracloud.serviceconfig.user.UserConfigModeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is the base of service-specific configuration classes.
 * 
 * @author Andrew Woods
 *         Date: Aug 3, 2010
 */
public abstract class AbstractServiceInfo {

        public abstract ServiceInfo getServiceXml(int index, String version);

        protected List<DeploymentOption> getSimpleDeploymentOptions() {
        // Deployment Options
        DeploymentOption depPrimary = new DeploymentOption();
        depPrimary.setLocation(DeploymentOption.Location.PRIMARY);
        depPrimary.setState(DeploymentOption.State.AVAILABLE);

        DeploymentOption depNew = new DeploymentOption();
        depNew.setLocation(DeploymentOption.Location.NEW);
        depNew.setState(DeploymentOption.State.UNAVAILABLE);

        DeploymentOption depExisting = new DeploymentOption();
        depExisting.setLocation(DeploymentOption.Location.EXISTING);
        depExisting.setState(DeploymentOption.State.UNAVAILABLE);

        List<DeploymentOption> depOptions = new ArrayList<DeploymentOption>();
        depOptions.add(depPrimary);
        depOptions.add(depNew);
        depOptions.add(depExisting);

        return depOptions;
    }

    protected List<UserConfigModeSet> createDefaultModeSet(List<UserConfig> userConfigs) {
        return Arrays.asList(new UserConfigModeSet(userConfigs));
    }

}
