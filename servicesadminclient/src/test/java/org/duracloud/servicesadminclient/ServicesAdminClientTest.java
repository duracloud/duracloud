/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadminclient;

import junit.framework.Assert;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.easymock.EasyMock;
import static org.easymock.classextension.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.replay;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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

    public void testDeleteServiceBundle() {
        //        fail("Not yet implemented");
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
        HttpResponse mockResponse = createMock(HttpResponse.class);
        EasyMock.expect(mockResponse.getResponseBody()).andReturn(xml)
                .anyTimes();
        replay(mockResponse);

        RestHttpHelper helper = createMock(RestHttpHelper.class);
        EasyMock.expect(helper.get(EasyMock.isA(String.class)))
                .andReturn(mockResponse);
        replay(helper);

        return helper;
    }

}
