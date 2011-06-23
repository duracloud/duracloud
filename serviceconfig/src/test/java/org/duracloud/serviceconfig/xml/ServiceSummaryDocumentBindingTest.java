/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.duracloud.ServiceSummaryDocument;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServiceSummary;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jun 22, 2011
 */
public class ServiceSummaryDocumentBindingTest {

    private ServiceSummary inputSummary;

    @Before
    public void setUp() {
        inputSummary = new ServiceSummary();
    }

    @After
    public void tearDown() {
        inputSummary = null;
    }

    @Test
    public void test() {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setContentId("content-id");

        Map<String, String> properties = new HashMap<String, String>();
        properties.put("name", "value");

        inputSummary.setServiceInfo(serviceInfo);
        inputSummary.setServiceProperties(properties);
        verifySummary(inputSummary, serviceInfo, properties);

        inputSummary.setServiceInfo(null);
        inputSummary.setServiceProperties(null);
        verifySummary(inputSummary, null, null);
    }

    private void verifySummary(ServiceSummary inputSummary,
                               ServiceInfo serviceInfo,
                               Map<String, String> properties) {
        Assert.assertEquals(inputSummary.getServiceInfo(), serviceInfo);
        Assert.assertEquals(inputSummary.getServiceProperties(), properties);

        ServiceSummaryDocument doc = ServiceSummaryDocumentBinding.createDocumentFrom(
            inputSummary);
        Assert.assertNotNull(doc);

        ServiceSummary summary = ServiceSummaryDocumentBinding.createServiceSummaryFrom(
            doc.newInputStream());
        Assert.assertNotNull(summary);

        if (null != serviceInfo) {
            Assert.assertEquals(serviceInfo.getContentId(),
                                summary.getServiceInfo().getContentId());
        }

        if (null != properties) {
            Map<String, String> props = summary.getServiceProperties();
            Assert.assertEquals(props.size(), properties.size());

            for (String key : properties.keySet()) {
                Assert.assertEquals(properties.get(key), props.get(key));
            }
        }
    }
}
