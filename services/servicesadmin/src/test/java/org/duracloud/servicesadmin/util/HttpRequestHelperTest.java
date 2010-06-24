/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicesadmin.util;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.services.beans.ComputeServiceBean;
import org.duracloud.services.util.XMLServiceSerializerImpl;
import org.easymock.EasyMock;

import junit.framework.Assert;
import junit.framework.TestCase;

public class HttpRequestHelperTest
        extends TestCase {

    private HttpRequestHelper helper;

    private ComputeServiceBean bean;

    private final String serviceId = "serviceId-test";

    private final String configId = "configId-test";

    private Map<String, String> config;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        helper = new HttpRequestHelper();
        helper.setSerializer(new XMLServiceSerializerImpl());

        bean = new ComputeServiceBean(serviceId);

        config = new HashMap<String, String>();
        config.put("key0", "val0");
        config.put("key1", "val1");
        config.put("key2", "val2");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        helper = null;
        bean = null;
        config = null;
    }

    public void testGetServiceIdParameter() throws Exception {
        String id = helper.getServiceIdParameter(mockGetServiceRequest());
        Assert.assertNotNull(id);
        Assert.assertEquals(serviceId, id);
    }

    public void testGetConfigIdFromRestURL() throws Exception {
        String id = helper.getConfigIdFromRestURL(mockGetConfigIdRequest());
        assertNotNull(id);
        assertEquals(configId, id);
    }

    public void testGetConfigProps() throws Exception {
        Map<String, String> props =
                helper.getConfigProps(mockGetConfigPropsRequest());
        Assert.assertNotNull(props);

        assertEquals(config.size(), props.size());
        for (String key : config.keySet()) {
            assertEquals(config.get(key), props.get(key));
        }
    }

    private HttpServletRequest mockGetServiceRequest() throws Exception {
        String beanXml = helper.getSerializer().serialize(bean);
        Assert.assertNotNull(beanXml);

        return mockPostRequest(beanXml);
    }

    private HttpServletRequest mockGetConfigPropsRequest() throws Exception {
        String configXml = SerializationUtil.serializeMap(config);
        Assert.assertNotNull(configXml);

        return mockPostRequest(configXml);
    }

    private HttpServletRequest mockPostRequest(String xml) throws IOException {
        HttpServletRequest request =
                EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getContentLength()).andReturn(xml.length())
                .anyTimes();
        EasyMock.expect(request.getInputStream())
                .andReturn(mockServletInputStream(xml)).anyTimes();

        EasyMock.replay(request);
        return request;
    }

    private ServletInputStream mockServletInputStream(final String xml) {
        return new ServletInputStream() {

            @Override
            public int readLine(byte[] b, int off, int len) {
                byte[] beanBytes = xml.getBytes();
                Assert.assertEquals(beanBytes.length, b.length);

                for (int i = off; i < len; ++i) {
                    b[i] = beanBytes[i];
                }
                return -1;
            }

            @Override
            public int read() throws IOException {
                return xml.length();
            }
        };
    }

    private HttpServletRequest mockGetConfigIdRequest() throws Exception {
        HttpServletRequest request =
                EasyMock.createMock(HttpServletRequest.class);
        EasyMock.expect(request.getPathInfo()).andReturn("/configure/"
                + configId);

        EasyMock.replay(request);
        return request;
    }

}
