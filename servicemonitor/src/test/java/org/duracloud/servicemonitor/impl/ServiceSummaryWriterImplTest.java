/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.servicemonitor.impl;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.serviceapi.ServicesManager;
import org.duracloud.serviceapi.error.NotFoundException;
import org.duracloud.serviceapi.error.ServicesException;
import org.duracloud.servicemonitor.ServiceSummaryWriter;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 6/20/11
 */
public class ServiceSummaryWriterImplTest {

    private ServiceSummaryWriter summaryWriter;

    private ServicesManager servicesManager;
    private ContentStore contentStore;
    private final String spaceId = "space-id";
    private final String contentId = "content-id";

    private final int serviceId = 5;
    private final int deploymentId = 7;

    @Before
    public void setUp() throws Exception {
        servicesManager = EasyMock.createMock("ServicesManager",
                                              ServicesManager.class);
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        summaryWriter = new ServiceSummaryWriterImpl(servicesManager,
                                                     contentStore,
                                                     spaceId,
                                                     contentId);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(servicesManager, contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(servicesManager, contentStore);
    }

    @Test
    public void testCollectAndWriteSummary() throws Exception {
        createMockExpectations();
        replayMocks();
        summaryWriter.collectAndWriteSummary(serviceId, deploymentId);
    }

    private void createMockExpectations()
        throws ServicesException, NotFoundException, ContentStoreException {
        // servicesManager
        EasyMock.expect(servicesManager.getDeployedService(serviceId,
                                                           deploymentId))
            .andReturn(null);

        EasyMock.expect(servicesManager.getDeployedServiceProps(serviceId,
                                                                deploymentId))
            .andReturn(null);

        // contentStore
        EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.eq(contentId),
                                                EasyMock.<InputStream>anyObject(),
                                                EasyMock.anyLong(),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<Map<String, String>>isNull()))
            .andReturn(null);

    }
}
