/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.duraservice.error.NoSuchDeployedServiceException;
import org.duracloud.duraservice.error.NoSuchServiceComputeInstanceException;
import org.duracloud.duraservice.error.NoSuchServiceException;
import org.duracloud.duraservice.rest.RestTestHelper;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.user.MultiSelectUserConfig;
import org.duracloud.serviceconfig.user.Option;
import org.duracloud.serviceconfig.user.SingleSelectUserConfig;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests ServiceManager
 *
 * @author Bill Branan
 */
public class ServiceManagerTest extends ServiceManagerTestMockSupport {

    private ServiceManager serviceManager;

    @Before
    public void setUp() throws Exception {
        serviceManager = new ServiceManager(createMockContentStoreManagerUtil(),
                                            createMockServiceConfigUtil(),
                                            createMockServiceComputeInstanceUtil());

        String configXml = RestTestHelper.buildTestInitXml();
        ByteArrayInputStream configStream = new ByteArrayInputStream(configXml.getBytes(
            "UTF-8"));
        serviceManager.configure(configStream);
    }

    @After
    public void tearDown() throws Exception {
        serviceManager.undeployAllServices();
        List<ServiceInfo> deployedServices =
            serviceManager.getDeployedServices();
        assertTrue(deployedServices.size() == 0);
    }

    @Test
    public void testNotConfigured() throws Exception {
        ServiceManager unConfigedServiceManager = new ServiceManager(
            createMockContentStoreManagerUtil(),
            createMockServiceConfigUtil(),
            createMockServiceComputeInstanceUtil());
        String failMsg = "Should throw an exception if not initialized";

        try {
            unConfigedServiceManager.getAvailableServices();
            fail(failMsg);
        } catch (RuntimeException re) {}

        try{
            unConfigedServiceManager.getDeployedServices();
            fail(failMsg);
        } catch (RuntimeException re) {}        

        try{
            unConfigedServiceManager.getService(1);
            fail(failMsg);
        } catch (RuntimeException re) {}

        try{
            unConfigedServiceManager.getDeployedService(1, 0);
            fail(failMsg);
        } catch (RuntimeException re) {}

        try{
            unConfigedServiceManager.deployService(1, "test", "1.0", null);
            fail(failMsg);
        } catch (RuntimeException re) {}

        try{
            unConfigedServiceManager.updateServiceConfig(1, 0, "1.0", null);
            fail(failMsg);
        } catch (RuntimeException re) {}

        try{
            unConfigedServiceManager.undeployService(1, 0);
            fail(failMsg);
        } catch (RuntimeException expected) {}
    }

    @Test
    public void testGetAvailableServices() throws Exception {
        List<ServiceInfo> services = serviceManager.getAvailableServices();
        assertNotNull(services);
        assertTrue(services.size() == 2);
        for(ServiceInfo service : services) {
            int serviceId = service.getId();
            assertTrue("id: " + serviceId, serviceId == 1 || serviceId == 2);
        }
    }

    @Test
    public void testGetDeployedServices() throws Exception {
        List<ServiceInfo> services = serviceManager.getDeployedServices();
        assertTrue(services.size() == 0);
    }

    @Test
    public void testGetService() throws Exception {
        ServiceInfo service = serviceManager.getService(1);
        assertNotNull(service);
        assertNotNull(service.getContentId());
        assertNotNull(service.getDescription());
        assertNotNull(service.getDisplayName());
        assertNotNull(service.getUserConfigVersion());
        List deploymentOptions = service.getDeploymentOptions();
        assertNotNull(deploymentOptions);
        assertTrue(deploymentOptions.size() == 1);
        assertNull(service.getSystemConfigs());

        service = serviceManager.getService(2);
        assertNotNull(service);
        List<UserConfig> userConfigList = service.getUserConfigs();
        assertNotNull(userConfigList);
        boolean storesOptions = false;
        boolean spacesOptions = false;
        for(UserConfig config : userConfigList) {
            assertNotNull(config);
            // Stores Config Options
            if(config instanceof SingleSelectUserConfig) {
                assertEquals("config2", config.getName());
                List<Option> options =
                    ((SingleSelectUserConfig)config).getOptions();
                assertNotNull(options);
                assertTrue(options.size() == 1);
                assertEquals(USER_CONTENT_STORE, options.get(0).getValue());
                storesOptions = true;
            }
            // Spaces Config Options
            if(config instanceof MultiSelectUserConfig) {
                assertEquals("config3", config.getName());
                List<Option> options =
                    ((MultiSelectUserConfig)config).getOptions();
                assertNotNull(options);
                assertTrue("size: " + options.size(), options.size() == 2);
                for(Option option : options) {
                    assertTrue(option.getValue().equals(USER_SPACE_1) ||
                               option.getValue().equals(USER_SPACE_2));
                }
                spacesOptions = true;
            }
        }
        assertTrue(storesOptions);
        assertTrue(spacesOptions);
        assertNull(service.getSystemConfigs());

        service = serviceManager.getService(3);
        assertNotNull(service);
    }

    @Test
    public void testGetDeployedServiceProps() throws Exception {
        int serviceId = 1;
        ServiceInfo service1 = serviceManager.getService(serviceId);
        List<UserConfig> userConfigs = service1.getUserConfigs();
        int deploymentId =
            serviceManager.deployService(serviceId, null, "1.0", userConfigs);

        Map<String, String> serviceProps =
            serviceManager.getDeployedServiceProps(serviceId, deploymentId);
        assertNotNull(serviceProps);
    }

    @Test
    public void testDeployService() throws Exception {
        int serviceId = 1;
        ServiceInfo service1 = serviceManager.getService(serviceId);
        List<UserConfig> userConfigs = service1.getUserConfigs();
        serviceManager.deployService(serviceId, null, "1.0", userConfigs);
        List<ServiceInfo> services = serviceManager.getDeployedServices();
        assertTrue(services.size() == 1);
        ServiceInfo deployedService = services.get(0);
        assertEquals(serviceId, deployedService.getId());
        assertTrue(deployedService.getDeployments().size() == 1);

        serviceManager.deployService(serviceId, null, "1.0", userConfigs);
        services = serviceManager.getDeployedServices();
        assertTrue(services.size() == 1);
        deployedService = services.get(0);
        assertEquals(serviceId, deployedService.getId());
        assertTrue(deployedService.getDeployments().size() == 2);

        try {
            int invalidServiceId = 1000;
            serviceManager.deployService(invalidServiceId,
                                         null,
                                         "1.0",
                                         userConfigs);
            fail("Should throw an exception trying to deploy invalid service");
        } catch (NoSuchServiceException expected) {
            assertNotNull(expected);
        }

        try {
            String invalidHost = "invalidHost";
            serviceManager.deployService(1, invalidHost, "1.0", userConfigs);
            fail("Should throw an exception trying to deploy to invalid host");
        } catch (NoSuchServiceComputeInstanceException expected) {
            assertNotNull(expected);
        }

        serviceId = 2;
        try {
            serviceManager.deployService(serviceId, null, "1.0", userConfigs);
            fail("Should throw an exception trying to deploy to service 2 to " +
                "the primary host");
        } catch (RuntimeException expected) {
            assertNotNull(expected);
        }

        serviceManager.deployService(serviceId,
                                     ServiceManager.NEW_SERVICE_HOST,
                                     "1.0",
                                     userConfigs);
        services = serviceManager.getDeployedServices();
        assertTrue(services.size() == 2);
        assertEquals(serviceId, services.get(1).getId());
    }

    @Test
    public void testReconfigureService() throws Exception {
        // Deploy service 1 with standard user config set
        int serviceId = 1;
        ServiceInfo service1 = serviceManager.getService(serviceId);
        List<UserConfig> userConfigs = service1.getUserConfigs();
        int deploymentId =
            serviceManager.deployService(serviceId, null, "1.0", userConfigs);

        // Check config
        ServiceInfo deployedService =
            serviceManager.getDeployedService(serviceId, deploymentId);
        assertTrue(userConfigs.size() ==
                   deployedService.getUserConfigs().size());

        // Create new config set and update service
        List<UserConfig> newConfigs = new ArrayList<UserConfig>();
        newConfigs.add(new TextUserConfig("newConfig", "New Config", "new"));
        serviceManager.updateServiceConfig(1, deploymentId, "1.0", newConfigs);

        // Check config
        deployedService =
            serviceManager.getDeployedService(serviceId, deploymentId);
        List<Deployment> deployments = deployedService.getDeployments();
        List<UserConfig> deployedConfig = deployments.get(0).getUserConfigs();
        assertTrue(newConfigs.size() == deployedConfig.size());
    }

    @Test
    public void testUnDeployService() throws Exception {
        int serviceId = 1;
        ServiceInfo service1 = serviceManager.getService(serviceId);
        List<UserConfig> userConfigs = service1.getUserConfigs();
        int deploymentId =
            serviceManager.deployService(serviceId, null, "1.0", userConfigs);

        ServiceInfo deployedService =
            serviceManager.getDeployedService(serviceId, deploymentId);
        assertNotNull(deployedService);

        serviceManager.undeployService(serviceId, deploymentId);

        try {
            serviceManager.getDeployedService(serviceId, deploymentId);
            fail("Should not be able to retrieve undeployed service");
        } catch(NoSuchDeployedServiceException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testAddServicesInstance() throws Exception {
        String hostName =
            serviceManager.createServiceInstance("New Service Instance");
        assertNotNull(hostName);
    }

}