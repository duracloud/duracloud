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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jun 22, 2011
 */
public class ServiceSummariesDocumentTest {
    private int id = 100;
    private String contentId = "contentId-";
    private String displayName = "displayName-";
    private String serviceVersion = "serviceVersion-";
    private String userConfigVersion = "userConfigVersion-";
    private String description = "description-";

    private String propName = "propName-";
    private String propValue = "propValue-";

    private ServiceSummary createSummary(int tag) {
        ServiceInfo serviceInfo = createService(tag);
        verifyService(serviceInfo, tag);

        Map<String, String> properties = createProps(tag);
        verifyProps(properties, tag);

        ServiceSummary serviceSummary = new ServiceSummary();
        serviceSummary.setServiceInfo(serviceInfo);
        serviceSummary.setServiceProperties(properties);

        return serviceSummary;
    }

    private ServiceInfo createService(int tag) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setId(id + tag);
        serviceInfo.setContentId(contentId + tag);
        serviceInfo.setDisplayName(displayName + tag);
        serviceInfo.setServiceVersion(serviceVersion + tag);
        serviceInfo.setUserConfigVersion(userConfigVersion + tag);
        serviceInfo.setDescription(description + tag);

        return serviceInfo;
    }

    @Test
    public void testServiceSummary() {
        int tag = 5;
        ServiceSummary expected = createSummary(tag);

        String xml = ServiceSummariesDocument.getServiceSummaryAsXML(expected);
        verifySummaryXML(xml, tag);

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        ServiceSummary summary = ServiceSummariesDocument.getServiceSummary(
            inputStream);

        ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(summary.getServiceInfo());
        verifier.verify(summary.getServiceInfo());

        verifyProps(summary.getServiceProperties(), tag);

        String newXml = ServiceSummariesDocument.getServiceSummaryAsXML(summary);
        Assert.assertEquals(xml, newXml);
        System.out.println(xml);
    }

    private void verifyProps(Map<String, String> props, int tag) {
        Assert.assertEquals(tag, props.size());
        if (tag > 0) {
            for (int i = 0; i < tag; ++i) {
                String name = props.get(propName + i);
                Assert.assertEquals(name, propValue + i);
            }
        }
    }

    private Map<String, String> createProps(int tag) {
        Map<String, String> props = new HashMap<String, String>();
        for (int i = 0; i < tag; ++i) {
            props.put(propName + i, propValue + i);
        }

        return props;
    }

    @Test
    public void testServiceSummaryList() {
        List<ServiceSummary> expectedSummaries = new ArrayList<ServiceSummary>();
        List<Integer> tags = new ArrayList<Integer>();
        for (int tag = 10; tag < 1000; tag *= 10) {
            expectedSummaries.add(createSummary(tag));
            tags.add(tag);
        }

        verifySummaryList(expectedSummaries, tags);

        String xml = ServiceSummariesDocument.getServiceSummaryListAsXML(
            expectedSummaries);

        verifySummariesListXML(xml, tags);

        InputStream inputStream = new ByteArrayInputStream(xml.getBytes());
        List<ServiceSummary> summaries = ServiceSummariesDocument.getServiceSummaryList(
            inputStream);
        verifySummariesListsEqual(expectedSummaries, summaries);

        String newXml = ServiceSummariesDocument.getServiceSummaryListAsXML(
            summaries);
        Assert.assertEquals(xml, newXml);

        System.out.println(xml);
    }

    private void verifySummaryList(List<ServiceSummary> expectedSummaries,
                                   List<Integer> tags) {
        Assert.assertNotNull(expectedSummaries);
        Assert.assertNotNull(tags);
        Assert.assertTrue(expectedSummaries.size() > 0);
        Assert.assertEquals(expectedSummaries.size(), tags.size());

        Iterator<ServiceSummary> summariesItr = expectedSummaries.iterator();
        for (int tag : tags) {
            ServiceSummary summary = summariesItr.next();

            verifyService(summary.getServiceInfo(), tag);
            verifyProps(summary.getServiceProperties(), tag);
        }
    }

    private void verifyService(ServiceInfo serviceInfo, int tag) {
        ServiceInfo expected = createService(tag);

        ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(expected);
        verifier.verify(serviceInfo);
    }

    private void verifySummariesListsEqual(List<ServiceSummary> expectedSummaries,
                                           List<ServiceSummary> summaries) {
        Assert.assertNotNull(expectedSummaries);
        Assert.assertNotNull(summaries);
        Assert.assertEquals(expectedSummaries.size(), summaries.size());

        Iterator<ServiceSummary> expectedSummariesItr = expectedSummaries.iterator();
        Iterator<ServiceSummary> summariesItr = summaries.iterator();
        while (summariesItr.hasNext() && expectedSummariesItr.hasNext()) {
            ServiceSummary expectedSummary = expectedSummariesItr.next();
            ServiceSummary summary = summariesItr.next();
            ServiceInfoVerifyHelper verifier = new ServiceInfoVerifyHelper(
                expectedSummary.getServiceInfo());
            verifier.verify(summary.getServiceInfo());
        }
    }

    private void verifySummariesListXML(String xml, List<Integer> tags) {
        for (int tag : tags) {
            verifySummaryXML(xml, tag);
        }
    }

    private void verifySummaryXML(String xml, int tag) {
        Assert.assertNotNull(xml);
        Assert.assertTrue(xml, xml.contains(contentId + tag));
        Assert.assertTrue(xml, xml.contains(displayName + tag));
        Assert.assertTrue(xml, xml.contains(serviceVersion + tag));
        Assert.assertTrue(xml, xml.contains(userConfigVersion + tag));
        Assert.assertTrue(xml, xml.contains(description + tag));

        Assert.assertTrue(xml, xml.contains(propName + (tag - 1)));
        Assert.assertTrue(xml, xml.contains(propValue + (tag- 1)));
    }

}
