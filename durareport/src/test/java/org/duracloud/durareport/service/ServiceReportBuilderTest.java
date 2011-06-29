/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.service;

import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.duracloud.servicemonitor.ServiceSummaryDirectory;
import org.duracloud.servicemonitor.error.ServiceSummaryNotFoundException;
import org.duracloud.services.ComputeService;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 6/22/11
 */
public class ServiceReportBuilderTest {

    private ServicesManager servicesMgr;
    private ServiceSummarizer serviceSummarizer;
    private ServiceSummaryDirectory summaryDirectory;

    private ServiceReportBuilder builder;

    @Before
    public void setup() {
        servicesMgr = EasyMock.createMock(ServicesManager.class);
        serviceSummarizer = EasyMock.createMock(ServiceSummarizer.class);
        summaryDirectory = EasyMock.createMock(ServiceSummaryDirectory.class);
        builder = new ServiceReportBuilder(serviceSummarizer, summaryDirectory);
    }

    private void replayMocks() {
        EasyMock.replay(servicesMgr, serviceSummarizer, summaryDirectory);
    }

    @After
    public void teardown() {
        EasyMock.verify(servicesMgr, serviceSummarizer, summaryDirectory);
    }

    @Test
    public void testCollectCompletedServices()
        throws ServiceSummaryNotFoundException {
        List<String> summaryIds = new LinkedList<String>();
        summaryIds.add("one");
        summaryIds.add("two");
        EasyMock.expect(summaryDirectory.getServiceSummaryIds())
            .andReturn(summaryIds)
            .times(1);

        EasyMock.expect(summaryDirectory.getServiceSummariesById("one"))
            .andReturn(createServiceSummaryList(7))
            .times(1);

        EasyMock.expect(summaryDirectory.getServiceSummariesById("two"))
            .andReturn(createServiceSummaryList(5))
            .times(1);

        replayMocks();

        List<ServiceSummary> summaries = builder.collectCompletedServices(10);
        assertNotNull(summaries);
        assertEquals(10, summaries.size());
    }

    private List<ServiceSummary> createServiceSummaryList(int itemCount) {
        List<ServiceSummary> summaries = new LinkedList<ServiceSummary>();
        for(int i=1; i <= itemCount; i++) {
            ServiceSummary summary = new ServiceSummary();
            summary.setId(i*itemCount);
            summary.setDeploymentId(i*itemCount);
            summaries.add(summary);
        }
        return summaries;
    }

    @Test
    public void testGetSummaryIds() {
        String summaryId1 = "report/service-summaries-2011-01";
        String summaryId2 = "report/service-summaries-2011-02";
        String summaryId3 = "report/service-summaries-2011-03";

        List<String> summaryIds = new LinkedList<String>();
        // Add to list intentially out of date order
        summaryIds.add(summaryId2);
        summaryIds.add(summaryId1);
        summaryIds.add(summaryId3);
        EasyMock.expect(summaryDirectory.getServiceSummaryIds())
            .andReturn(summaryIds)
            .times(1);

        replayMocks();

        List<String> summaryIdList = builder.getSummaryIds();
        assertNotNull(summaryIdList);
        assertEquals(3, summaryIdList.size());
        assertEquals(summaryId3, summaryIdList.get(0));
        assertEquals(summaryId2, summaryIdList.get(1));
        assertEquals(summaryId1, summaryIdList.get(2));
    }

    @Test
    public void testAddSummaries() {
        List<ServiceSummary> sourceList = new LinkedList<ServiceSummary>();
        String date1 = "2011-01-01T10:00:00";
        String date2 = "2011-03-03T10:00:00";
        String date3 = "2011-05-05T10:00:00";

        // Add to list, intentionally out of date order
        sourceList.add(createServiceSummary(date3));
        sourceList.add(createServiceSummary(date1));
        sourceList.add(createServiceSummary(date2));

        List<ServiceSummary> compilationList = new LinkedList<ServiceSummary>();

        builder.addSummaries(sourceList, compilationList, 10);

        assertNotNull(compilationList);
        assertEquals(3, compilationList.size());
        assertEquals(date3, compilationList.get(0).getProperties()
                                .get(ComputeService.STOPTIME_KEY));
        assertEquals(date2, compilationList.get(1).getProperties()
                                .get(ComputeService.STOPTIME_KEY));
        assertEquals(date1, compilationList.get(2).getProperties()
                                .get(ComputeService.STOPTIME_KEY));

        replayMocks();
    }

    private ServiceSummary createServiceSummary(String stopDate) {
        ServiceSummary summary = new ServiceSummary();
        Map<String, String> props = new HashMap<String, String>();
        props.put(ComputeService.STOPTIME_KEY, stopDate);
        summary.setProperties(props);
        return summary;
    }

}
