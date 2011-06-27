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
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

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
        EasyMock.expect(summaryDirectory.getCurrentServiceSummaries())
            .andReturn(createServiceSummaryList(5))
            .times(1);

        List<String> summaryIds = new LinkedList<String>();
        summaryIds.add("one");
        summaryIds.add("two");
        EasyMock.expect(summaryDirectory.getServiceSummaryIds())
            .andReturn(summaryIds)
            .times(1);

        EasyMock.expect(summaryDirectory.getServiceSummariesById("one"))
            .andReturn(createServiceSummaryList(3))
            .times(1);

        EasyMock.expect(summaryDirectory.getServiceSummariesById("two"))
            .andReturn(createServiceSummaryList(4))
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

}
