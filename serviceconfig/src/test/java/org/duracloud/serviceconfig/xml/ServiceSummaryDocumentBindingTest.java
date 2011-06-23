/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig.xml;

import org.duracloud.ServiceSummaryDocument;
import org.duracloud.serviceconfig.ServiceSummary;
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

    private static final int MAP_SIZE = 5;

    private int id = 5;
    private int deploymentId = 7;
    private String name = "name";
    private String version = "1.2.3";
    private Map<String, String> properties = new HashMap<String, String>();
    private Map<String, String> configs = new HashMap<String, String>();

    @Before
    public void setUp() {
        for (int i = 0; i < MAP_SIZE; ++i) {
            String x = Integer.toString(i);
            properties.put("prop-" + x, x);
            configs.put("config-" + x, x);
        }
    }

    @Test
    public void test0() {
        verifySummary(createServiceSummary());
    }

    @Test
    public void test1() {
        id = 0;
        deploymentId = 0;
        name = null;
        version = null;
        properties = null;
        configs = null;
        
        verifySummary(createServiceSummary());
    }

    @Test
    public void test2() {
        id = 0;
        verifySummary(createServiceSummary());
    }

    @Test
    public void test3() {
        deploymentId = 0;
        verifySummary(createServiceSummary());
    }

    @Test
    public void test4() {
        name = null;
        verifySummary(createServiceSummary());
    }

    @Test
    public void test5() {
        version = null;
        verifySummary(createServiceSummary());
    }

    @Test
    public void test6() {
        properties = null;
        verifySummary(createServiceSummary());
    }

    @Test
    public void test7() {
        configs = null;
        verifySummary(createServiceSummary());
    }

    private ServiceSummary createServiceSummary() {
        ServiceSummary summary = new ServiceSummary();
        summary.setId(id);
        summary.setDeploymentId(deploymentId);
        summary.setName(name);
        summary.setVersion(version);
        summary.setConfigs(configs);
        summary.setProperties(properties);

        return summary;
    }

    private void verifySummary(ServiceSummary summary) {
        ServiceSummary result = serializeAndDeserialize(summary);
        Assert.assertEquals(summary.getId(), result.getId());
        Assert.assertEquals(summary.getDeploymentId(),
                            result.getDeploymentId());
        Assert.assertEquals(summary.getName(), result.getName());
        Assert.assertEquals(summary.getVersion(), result.getVersion());

        verifyMap(summary.getConfigs(), result.getConfigs());
        verifyMap(summary.getProperties(), result.getProperties());
    }

    private ServiceSummary serializeAndDeserialize(ServiceSummary summary) {
        ServiceSummaryDocument doc = ServiceSummaryDocumentBinding.createDocumentFrom(
            summary);
        Assert.assertNotNull(doc);

        ServiceSummary smry = ServiceSummaryDocumentBinding.createServiceSummaryFrom(
            doc.newInputStream());
        Assert.assertNotNull(smry);
        return smry;
    }

    private void verifyMap(Map<String, String> map,
                           Map<String, String> result) {
        if (null == map) {
            Assert.assertEquals(0, result.size());
        }
        Assert.assertNotNull(result);

        if (null == map) {
            return;
        }

        Assert.assertEquals(map.size(), result.size());
        for (String key : map.keySet()) {
            Assert.assertEquals(key, map.get(key), result.get(key));
        }
    }
}
