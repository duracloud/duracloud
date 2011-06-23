/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durareport.service;

import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceconfig.ServiceInfo;
import org.duracloud.serviceconfig.ServiceSummary;
import org.duracloud.servicemonitor.ServiceSummarizer;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 6/22/11
 */
public class ServiceReportBuilderTest {

    private ServicesManager servicesMgr;
    private ServiceSummarizer serviceSummarizer;
    private ServiceReportBuilder builder;

    @Before
    public void setup() {
        servicesMgr = EasyMock.createMock(ServicesManager.class);
        serviceSummarizer = EasyMock.createMock(ServiceSummarizer.class);
        builder = new ServiceReportBuilder(servicesMgr, serviceSummarizer);
    }

    private void replayMocks() {
        EasyMock.replay(servicesMgr, serviceSummarizer);
    }

    @After
    public void teardown() {
        EasyMock.verify(servicesMgr, serviceSummarizer);
    }

    @Test
    public void testCollectRunningServices() throws Exception {
        EasyMock.expect(servicesMgr.getDeployedServices())
            .andReturn(null)
            .times(1);
        List<ServiceSummary> summaries = new ArrayList<ServiceSummary>();
        EasyMock.expect(serviceSummarizer.summarizeServices(
            EasyMock.<List<ServiceInfo>>isNull()))
            .andReturn(summaries)
            .times(1);

        replayMocks();

        List<ServiceSummary> runningSummaries =
            builder.collectRunningServices();
        assertNotNull(runningSummaries);
    }


}
