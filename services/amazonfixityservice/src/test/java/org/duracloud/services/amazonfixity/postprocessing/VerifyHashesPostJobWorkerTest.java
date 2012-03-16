/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonfixity.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.fixity.FixityService;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.results.ServiceResultProcessor;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Jan 13, 2011
 */
public class VerifyHashesPostJobWorkerTest {

    private VerifyHashesPostJobWorker worker;

    private AmazonMapReduceJobWorker predecessor;
    private ContentStore contentStore;
    private FixityService fixityService;

    @Before
    public void setUp() throws Exception {
        predecessor = createMockJobWorkerPredecessor();
        contentStore = createMockContentStore();
        fixityService = createMockFixityService();

        String arg = "arg";
        worker = new VerifyHashesPostJobWorker(predecessor,
                                               contentStore,
                                               fixityService,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg,
                                               arg);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(predecessor);
        EasyMock.verify(contentStore);
        EasyMock.verify(fixityService);
    }

    private AmazonMapReduceJobWorker createMockJobWorkerPredecessor() {
        AmazonMapReduceJobWorker predecessor = EasyMock.createMock(
            "JobWorkerPredecessor",
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(predecessor.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.COMPLETE);

        EasyMock.replay(predecessor);
        return predecessor;
    }

    private ContentStore createMockContentStore() throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private FixityService createMockFixityService() throws Exception {
        FixityService fixityService = EasyMock.createNiceMock("FixityService",
                                                              FixityService.class);

        fixityService.start();
        EasyMock.expectLastCall();

        Map<String, String> props = new HashMap<String, String>();
        ServiceResultListener.StatusMsg status = new ServiceResultListener.StatusMsg(
            1,
            0,
            1,
            ServiceResultListener.State.COMPLETE,
            FixityService.PHASE_COMPARE,
            null);
        props.put(ServiceResultProcessor.STATUS_KEY, status.toString());
        EasyMock.expect(fixityService.getServiceProps()).andReturn(props);

        EasyMock.replay(fixityService);
        return fixityService;
    }

    @Test
    public void testRun() throws Exception {
        worker.run();
        // test is verified if mocks verify in tearDown().
    }
}
