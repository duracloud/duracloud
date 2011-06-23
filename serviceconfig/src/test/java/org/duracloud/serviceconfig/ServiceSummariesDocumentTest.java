/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.serviceconfig;

import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jun 22, 2011
 */
public class ServiceSummariesDocumentTest {

    private static final int MAP_SIZE = 5;

    private String name = "name-";
    private String version = "version-";

    private String propertyName = "prop-";
    private String propertyValue = "propvalue-";
    private String configName = "config-";
    private String configValue = "configvalue-";


    @Test
    public void testServiceSummary() {
        int tag = 5;
        ServiceSummary summary = createServiceSummary(tag);

        // serialize / deserialize
        String xml = ServiceSummariesDocument.getServiceSummaryAsXML(summary);
        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        ServiceSummary result = ServiceSummariesDocument.getServiceSummary(
            inputStream);

        verifySummariesEqual(summary, result);

        String newXml = ServiceSummariesDocument.getServiceSummaryAsXML(result);
        Assert.assertEquals(xml, newXml);
        System.out.println(xml);
    }

    private ServiceSummary createServiceSummary(int tag) {
        ServiceSummary summary = new ServiceSummary();
        summary.setId(tag);
        summary.setDeploymentId(tag);
        summary.setName(getName(tag));
        summary.setVersion(getVersion(tag));
        summary.setConfigs(getConfigs(tag));
        summary.setProperties(getProperties(tag));

        return summary;
    }

    private void verifySummariesEqual(ServiceSummary summary,
                                      ServiceSummary result) {
        Assert.assertNotNull(summary);
        Assert.assertNotNull(result);

        Assert.assertEquals(summary.getId(), result.getId());
        Assert.assertEquals(summary.getDeploymentId(),
                            result.getDeploymentId());
        Assert.assertEquals(summary.getName(), result.getName());
        Assert.assertEquals(summary.getVersion(), result.getVersion());

        verifyMapsEqual(summary.getConfigs(), result.getConfigs());
        verifyMapsEqual(summary.getProperties(), result.getProperties());
    }

    private void verifyMapsEqual(Map<String, String> map,
                                 Map<String, String> results) {
        Assert.assertNotNull(map);
        Assert.assertNotNull(results);
        Assert.assertEquals(map.size(), results.size());

        for (String key : map.keySet()) {
            Assert.assertEquals(map.get(key), results.get(key));
        }
    }

    @Test
    public void testServiceSummaryList() {
        List<ServiceSummary> summaries = new ArrayList<ServiceSummary>();
        List<Integer> tags = new ArrayList<Integer>();
        for (int tag = 0; tag < 10; ++tag) {
            summaries.add(createServiceSummary(tag));
            tags.add(tag);
        }
        String xml = ServiceSummariesDocument.getServiceSummaryListAsXML(
            summaries);

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        List<ServiceSummary> results = ServiceSummariesDocument.getServiceSummaryList(
            inputStream);
        verifySummariesListsEqual(summaries, results);

        String newXml = ServiceSummariesDocument.getServiceSummaryListAsXML(
            summaries);
        Assert.assertEquals(xml, newXml);

        System.out.println(xml);
    }

    private void verifySummariesListsEqual(List<ServiceSummary> summaries,
                                           List<ServiceSummary> results) {
        Assert.assertNotNull(summaries);
        Assert.assertNotNull(results);
        Assert.assertEquals(summaries.size(), results.size());

        for (ServiceSummary summary : summaries) {
            ServiceSummary result = getSummary(summary.getId(), results);
            verifySummariesEqual(summary, result);
        }
    }

    private ServiceSummary getSummary(int id, List<ServiceSummary> results) {
        for (ServiceSummary result : results) {
            if (result.getId() == id) {
                return result;
            }
        }
        Assert.fail("No result found for id: " + id);
        return null;
    }

    private String getName(int i) {
        return name + i;
    }

    private String getVersion(int i) {
        return version + i;
    }

    private String getPropertyName(int i, int j) {
        return propertyName + i + "-" + j;
    }

    private String getPropertyValue(int i, int j) {
        return propertyValue + i + "-" + j;
    }

    private String getConfigName(int i, int j) {
        return configName + i + "-" + j;
    }

    private String getConfigValue(int i, int j) {
        return configValue + i + "-" + j;
    }

    private Map<String, String> getProperties(int i) {
        Map<String, String> properties = new HashMap<String, String>();
        for (int x = 0; x < MAP_SIZE; ++x) {
            properties.put(getPropertyName(i, x), getPropertyValue(i, x));
        }
        return properties;
    }

    private Map<String, String> getConfigs(int i) {
        Map<String, String> configs = new HashMap<String, String>();
        for (int x = 0; x < MAP_SIZE; ++x) {
            configs.put(getConfigName(i, x), getConfigValue(i, x));
        }
        return configs;
    }
}
