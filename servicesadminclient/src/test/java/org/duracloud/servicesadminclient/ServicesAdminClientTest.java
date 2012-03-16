/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient;

import junit.framework.Assert;
import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 01, 2010
 */
public class ServicesAdminClientTest {

    private ServicesAdminClient client;

    private final String configId = "configId-test";

    @Before
    public void setUp() throws Exception {
        client = new ServicesAdminClient();
        client.setBaseURL("http://junk.com");
        client.setSerializer(new XMLServiceSerializerImpl());
    }

    @After
    public void tearDown() throws Exception {
    }

    public void testPostServiceBundle() {
        //        fail("Not yet implemented");
    }

    @Test
    public void testDeleteServiceBundle() throws Exception {
        String serviceId = "some-service-0.8.0.zip";

        client.setRester(mockRestHttpHelperDeleteBundle(serviceId));
        HttpResponse response = client.deleteServiceBundle(serviceId);
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        Assert.assertEquals(serviceId + " deleted", response.getResponseBody());
    }

    @Test
    public void testDeleteAllServiceBundles() throws Exception {
        client.setRester(mockRestHttpHelperDeleteBundle("all"));
        HttpResponse response = client.deleteAllServiceBundles();
        Assert.assertNotNull(response);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        Assert.assertEquals("all bundles deleted", response.getResponseBody());
    }

    private RestHttpHelper mockRestHttpHelperDeleteBundle(String serviceId)
        throws Exception {
        HttpResponse mockResponse = EasyMock.createMock("HttpResponse",
                                                        HttpResponse.class);

        if (serviceId.equals("all")) {
            EasyMock.expect(mockResponse.getResponseBody()).andReturn(
                "all bundles deleted").anyTimes();

        } else {
            EasyMock.expect(mockResponse.getResponseBody()).andReturn(
                serviceId + " deleted").anyTimes();
        }

        EasyMock.expect(mockResponse.getStatusCode()).andReturn(HttpStatus.SC_OK)
            .anyTimes();
        EasyMock.replay(mockResponse);

        RestHttpHelper helper = EasyMock.createMock("RestHttpHelper",
                                                    RestHttpHelper.class);
        EasyMock.expect(helper.delete(EasyMock.isA(String.class))).andReturn(
            mockResponse);

        EasyMock.replay(helper);
        return helper;
    }

    public void testGetServiceListing() {
        //        fail("Not yet implemented");
    }

    public void testIsServiceDeployed() {
        //        fail("Not yet implemented");
    }

    @Test
    public void testGetServiceConfig() throws Exception {
        // SetUp
        Map<String, String> testConfig = new HashMap<String, String>();
        testConfig.put("key0", "val0");
        testConfig.put("key1", "val1");
        testConfig.put("key2", "val2");
        String testConfigXml = SerializationUtil.serializeMap(testConfig);

        client.setRester(mockRestHttpHelperConfigGET(testConfigXml));

        // Test
        Map<String, String> config = client.getServiceConfig(configId);
        Assert.assertNotNull(config);
        Assert.assertEquals(testConfig.size(), config.size());

        for (String key : testConfig.keySet()) {
            Assert.assertEquals(testConfig.get(key), config.get(key));
        }
    }

    private RestHttpHelper mockRestHttpHelperConfigGET(String xml)
            throws Exception {
        HttpResponse mockResponse = EasyMock.createMock(HttpResponse.class);
        EasyMock.expect(mockResponse.getResponseBody()).andReturn(xml)
                .anyTimes();
        EasyMock.replay(mockResponse);

        RestHttpHelper helper = EasyMock.createMock(RestHttpHelper.class);
        EasyMock.expect(helper.get(EasyMock.isA(String.class)))
                .andReturn(mockResponse);
        EasyMock.replay(helper);

        return helper;
    }

}
