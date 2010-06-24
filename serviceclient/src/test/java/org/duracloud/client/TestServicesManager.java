/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import junit.framework.TestCase;
import org.duracloud.client.error.NotFoundException;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SelectableUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Runtime test of DuraService java client. DuraService must
 * be available and initialized for these tests to pass.
 *
 * @author Bill Branan
 */
public class TestServicesManager
        extends TestCase {

    protected static final Logger log =
        LoggerFactory.getLogger(TestServicesManager.class);

    private static String host = "localhost";

    private static String port = null;

    private static final String defaultPort = "8080";

    private ServicesManager servicesManager;

    private static final int testServiceId = 1; // Replication Service
    private static int testDeploymentId = 0;
    private static final String textConfigValue = "Test";

    @Override
    @Before
    protected void setUp() throws Exception {
        servicesManager = new ServicesManagerImpl(host, getPort());
        servicesManager.login(getRootCredential());
        assertNotNull(servicesManager.getBaseURL());
    }

    private String getPort() throws Exception {
        ServiceClientConfig config = new ServiceClientConfig();
        String port = config.getPort();

        if(port == null || port.startsWith("$")) {
           port = defaultPort;
        }
        return port;
    }

    @Override
    @After
    protected void tearDown() throws Exception {
        // Make sure the test service is undeployed
        try {
            servicesManager.undeployService(testServiceId, testDeploymentId);
        } catch(NotFoundException e) {
            // Ignore, the service was likely already removed
        }
    }

    @Test
    public void testGetAvailableServices() throws Exception {
        List<ServiceInfo> availableServicesList =
            servicesManager.getAvailableServices();
        assertNotNull(availableServicesList);
        assertFalse(availableServicesList.isEmpty());
    }

    /*
     * Tests both deployService and getDeployedServices
     */
    @Test
    public void testDeployService() throws Exception {
        List<ServiceInfo> deployedServicesListStart =
            servicesManager.getDeployedServices();
        assertNotNull(deployedServicesListStart);
        assertNull(findService(testServiceId, deployedServicesListStart));

        deployService();

        List<ServiceInfo> deployedServicesListEnd =
            servicesManager.getDeployedServices();
        assertNotNull(deployedServicesListEnd);
        assertNotNull(findService(testServiceId, deployedServicesListEnd));
    }

    /*
     * Tests both updateServiceConfig and getService
     */
    @Test
    public void testUpdateServiceConfig() throws Exception {
        deployService();

        String updatedTextConfigValue = "Updated";

        ServiceInfo service = servicesManager.getService(testServiceId);
        assertNotNull(service);
        List<UserConfig> userConfigs =
            service.getDeployments().get(0).getUserConfigs();
        for(UserConfig config : userConfigs) {
            if(config instanceof TextUserConfig) {
                assertEquals(textConfigValue,
                             ((TextUserConfig)config).getValue());
                ((TextUserConfig)config).setValue(updatedTextConfigValue);
            }
        }

        servicesManager.updateServiceConfig(testServiceId,
                                            testDeploymentId,
                                            service.getUserConfigVersion(),
                                            userConfigs);

        service = servicesManager.getService(testServiceId);
        assertNotNull(service);
        userConfigs = service.getDeployments().get(0).getUserConfigs();
        for(UserConfig config : userConfigs) {
            if(config instanceof TextUserConfig) {
                assertEquals(updatedTextConfigValue,
                             ((TextUserConfig)config).getValue());
            }
        }
    }

    /*
     * Tests getting deployed service properties
     */
    @Test
    public void testGetDeployedServiceProps() throws Exception {
        deployService();

        Map<String, String> serviceProps =
            servicesManager.getDeployedServiceProps(testServiceId,
                                                    testDeploymentId);
        assertNotNull(serviceProps);
        assertTrue(serviceProps.size() >= 1);
    }


    /*
     * Tests both undeployService and getDeployedService
     */
    @Test
    public void testUnDeployService() throws Exception {
        deployService();

        ServiceInfo depService =
            servicesManager.getDeployedService(testServiceId, testDeploymentId);
        assertNotNull(depService);

        servicesManager.undeployService(testServiceId, testDeploymentId);

        try {
            servicesManager.getDeployedService(testServiceId, testDeploymentId);
            fail("Should not be able to retrieve an undeployed service");
        } catch(NotFoundException expected) {
            assertNotNull(expected);
        }
    }

    private void deployService() throws Exception {
        List<ServiceInfo> availableServicesList =
            servicesManager.getAvailableServices();
        assertNotNull(availableServicesList);
        ServiceInfo serviceToDeploy =
            findService(testServiceId, availableServicesList);

        String userConfigVer = serviceToDeploy.getUserConfigVersion();

        List<UserConfig> userConfigs = serviceToDeploy.getUserConfigs();
        for(UserConfig config : userConfigs) {
            if(config instanceof TextUserConfig) {
                ((TextUserConfig)config).setValue(textConfigValue);
            } else if(config instanceof SelectableUserConfig) {
                List<Option> options =
                    ((SelectableUserConfig)config).getOptions();
                for(Option option : options) {
                    option.setSelected(false);
                }
                options.get(0).setSelected(true);
            }
        }

        testDeploymentId =
             servicesManager.deployService(testServiceId,
                                             userConfigVer,
                                             userConfigs,
                                             null);
    }

    private ServiceInfo findService(int serviceId, List<ServiceInfo> services) {
        for(ServiceInfo service : services) {
            if(service.getId() == serviceId) {
                return service;
            }
        }
        return null;
    }

    private static Credential getRootCredential() {
        UnitTestDatabaseUtil dbUtil = null;
        try {
            dbUtil = new UnitTestDatabaseUtil();
        } catch (Exception e) {
            System.err.println("ERROR from unitTestDB: " + e.getMessage());
        }

        Credential rootCredential = null;
        try {
            ResourceType rootUser = ResourceType.fromDuraCloudUserType(
                DuraCloudUserType.ROOT);
            rootCredential = dbUtil.findCredentialForResource(rootUser);
        } catch (Exception e) {
            System.err.print("ERROR getting credential: " + e.getMessage());

        }
        return rootCredential;
    }

}