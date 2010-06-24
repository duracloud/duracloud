/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.rest;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.duraservice.config.DuraServiceConfig;
import org.duracloud.serviceconfig.Deployment;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServicesConfigDocument;
import org.duracloud.serviceconfig.user.TextUserConfig;
import org.duracloud.serviceconfig.user.UserConfig;
import org.duracloud.services.beans.ComputeServiceBean;
import org.duracloud.services.util.ServiceSerializer;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.duracloud.servicesadminclient.ServicesAdminClient;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runtime test of service REST API. The duraservice and durastore web
 * applications must be deployed and available in order for these tests
 * to pass. The durastore web application must also be initialized
 *
 * @author Bill Branan
 */
public class TestServiceRest {

    private static String configFileName = "test-duraservice.properties";
    private static ServicesAdminClient servicesAdmin;   

    static {
        DuraServiceConfig config = new DuraServiceConfig();
        config.setConfigFileName(configFileName);
        String servicesAdminBaseURL;
            servicesAdminBaseURL = config.getServicesAdminUrl();

        servicesAdmin = new ServicesAdminClient();
        servicesAdmin.setBaseURL(servicesAdminBaseURL);
        servicesAdmin.setRester(RestTestHelper.getAuthorizedRestHelper());
    }

    private static RestHttpHelper restHelper = RestTestHelper.getAuthorizedRestHelper();

    private static String baseUrl;
    
    private ServiceSerializer serializer;

    private int deploymentId = 0;

    private static final String testServiceId = "1"; // replication service

    @Before
    public void setUp() throws Exception {
        baseUrl = RestTestHelper.getBaseServiceUrl();

        // Initialize DuraService
        HttpResponse response = RestTestHelper.initialize();
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @After
    public void tearDown() throws Exception {
        try {
            undeployService(deploymentId);
        } catch(Exception e) {
            // Ignore, the service has likely already been undeployed
        }
    }

    @Test
    public void testDeployService() throws Exception {
        List<String> deployedServicesStart = getDeployedServicesViaAdmin();
        assertNotNull(deployedServicesStart);
        deploymentId = deployService();
        List<String> deployedServicesEnd = getDeployedServicesViaAdmin();
        assertNotNull(deployedServicesEnd);

        // TODO: Re-enable after verifying that listing works properly
        // assertEquals((deployedServicesStart.size() + 1), deployedServicesEnd.size());
    }

    @Test
    public void testGetService() throws Exception {
        String url = baseUrl + "/" + testServiceId;
        HttpResponse response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        ServiceInfo service =
            ServicesConfigDocument.getService(response.getResponseStream());
        assertNotNull(service);
        assertNotNull(service.getUserConfigVersion());
    }

    @Test
    public void testGetDeployedService() throws Exception {
        deploymentId = deployService();
        String url = baseUrl + "/" + testServiceId + "/" + deploymentId;
        HttpResponse response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        ServiceInfo service =
            ServicesConfigDocument.getService(response.getResponseStream());
        assertNotNull(service);

        List<Deployment> deployments = service.getDeployments();
        assertNotNull(deployments);
        assertFalse(deployments.isEmpty());

        assertNotNull(findDeployment(deployments, deploymentId));
    }

    @Test
    public void testGetDeployedServiceProps() throws Exception {
        deploymentId = deployService();
        String url = baseUrl + "/" + testServiceId + "/" +
            deploymentId + "/properties";

        HttpResponse response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        String responseBody = response.getResponseBody();
        Map<String, String> serviceProps =
            SerializationUtil.deserializeMap(responseBody);
        assertNotNull(serviceProps);
    }

    @Test
    public void testConfigureService() throws Exception {
        deploymentId = deployService();

        String url = baseUrl + "/" + testServiceId + "/" + deploymentId;
        HttpResponse response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        // Get Serivce
        ServiceInfo service =
            ServicesConfigDocument.getService(response.getResponseStream());
        assertNotNull(service);

        // Create User Config
        List<UserConfig> userConfigs = new ArrayList<UserConfig>();
        assertNotNull(userConfigs);

        String userConfigName = "test-name";
        String userConfigValue = "test-value";
        TextUserConfig newUserConfig =
            new TextUserConfig(userConfigName, "Test Config");
        newUserConfig.setValue(userConfigValue);
        userConfigs.add(newUserConfig);
        service.setUserConfigs(userConfigs);
        String serviceXml = ServicesConfigDocument.getServiceAsXML(service);

        // Update config
        response = restHelper.post(url, serviceXml, null);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        // Check config
        response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());

        service =
            ServicesConfigDocument.getService(response.getResponseStream());
        assertNotNull(service);

        List<Deployment> deployments = service.getDeployments();
        assertNotNull(deployments);
        assertFalse(deployments.isEmpty());

        Deployment dep = findDeployment(deployments, deploymentId);
        userConfigs = dep.getUserConfigs();
        assertNotNull(userConfigs);
        assertFalse(userConfigs.isEmpty());
        for(UserConfig config : userConfigs) {
            assertTrue(config instanceof TextUserConfig);
            assertEquals(userConfigName, ((TextUserConfig)config).getName());
            assertEquals(userConfigValue, ((TextUserConfig)config).getValue());
        }
    }

    @Test
    public void testUnDeployService() throws Exception {
        deploymentId = deployService();
        List<String> deployedServicesStart = getDeployedServicesViaAdmin();
        assertNotNull(deployedServicesStart);
        HttpResponse response = undeployService(deploymentId);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        List<String> deployedServicesEnd = getDeployedServicesViaAdmin();
        assertNotNull(deployedServicesEnd);

        // TODO: Re-enable after verifying that listing works properly
        // assertEquals((deployedServicesStart.size() - 1), deployedServicesEnd.size());
    }

    @Test
    public void testGetAvailableServices() throws Exception {
        String url = baseUrl + "/services?show=available";
        HttpResponse response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        List<ServiceInfo> services =
            ServicesConfigDocument.getServiceList(response.getResponseStream());
        assertNotNull(services);
        assertFalse(services.isEmpty());
    }

    @Test
    public void testGetDeployedServices() throws Exception {
        String url = baseUrl + "/services?show=deployed";
        HttpResponse response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        List<ServiceInfo> servicesStart =
            ServicesConfigDocument.getServiceList(response.getResponseStream());
        assertNotNull(servicesStart);
        assertNull(findService(servicesStart, testServiceId));

        deploymentId = deployService();

        response = restHelper.get(url);
        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        List<ServiceInfo> servicesEnd =
            ServicesConfigDocument.getServiceList(response.getResponseStream());
        assertNotNull(servicesEnd);
        assertNotNull(findService(servicesEnd, testServiceId));
    }

    private ServiceInfo findService(List<ServiceInfo> services,
                                    String serviceId) {
        return findService(services, Integer.parseInt(serviceId));
    }

    private ServiceInfo findService(List<ServiceInfo> services, int serviceId) {
        for(ServiceInfo service : services) {
            if(service.getId() == serviceId) {
                return service;
            }
        }
        return null;
    }

    private Deployment findDeployment(List<Deployment> deployments, int deploymentId) {
        for(Deployment deployment : deployments) {
            if(deployment.getId() == deploymentId) {
                return deployment;
            }
        }
        return null;
    }

    private int deployService() throws Exception {
        String url = baseUrl + "/" + testServiceId;
        HttpResponse response = restHelper.get(url);
        InputStream serviceXmlStream = response.getResponseStream();
        ServiceInfo service =
            ServicesConfigDocument.getService(serviceXmlStream);
        String serviceStr = ServicesConfigDocument.getServiceAsXML(service);

        response = restHelper.put(url, serviceStr, null);
        assertEquals(HttpStatus.SC_CREATED, response.getStatusCode());
        String deploymentIdUri =
            response.getResponseHeader("Location").getValue();
        int deploymentId = extractDeploymentId(deploymentIdUri);

        assertNotNull(deploymentId);
        return deploymentId;
    }

    private int extractDeploymentId(String deploymentUrl) {
        String[] deploymentUrlSplit = deploymentUrl.split("/");
        String depId = deploymentUrlSplit[deploymentUrlSplit.length-1];
        return Integer.valueOf(depId);
    }

    private HttpResponse undeployService(int deploymentId) throws Exception {
        String url = baseUrl + "/" + testServiceId + "/" + deploymentId;
        return restHelper.delete(url);
    }

    private List<String> getDeployedServicesViaAdmin() throws Exception {
        String deployedServicesXml =
            servicesAdmin.getServiceListing().getResponseBody();

        List<ComputeServiceBean> deployedServices;
        if(deployedServicesXml == null || deployedServicesXml.equals("")) {
            deployedServices = new ArrayList<ComputeServiceBean>();
        } else {
            deployedServices =
                getSerializer().deserializeList(deployedServicesXml);
        }

        List<String> deployedServiceNames = new ArrayList<String>();
        for(ComputeServiceBean deployedService : deployedServices) {
            deployedServiceNames.add(deployedService.getServiceName());
        }
        return deployedServiceNames;
    }

    private ServiceSerializer getSerializer() {
        if (serializer == null) {
            serializer = new XMLServiceSerializerImpl();
        }
        return serializer;
    }

}