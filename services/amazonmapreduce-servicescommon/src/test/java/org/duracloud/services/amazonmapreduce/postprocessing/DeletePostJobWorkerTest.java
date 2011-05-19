/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DeletePostJobWorkerTest {
    private DeletePostJobWorker worker;

    private AmazonMapReduceJobWorker predecessor;
    private ContentStore contentStore;
    private String spaceId = "space-id";
    private List<String> contentIds;

    @Before
    public void setUp() throws Exception {
        predecessor = createMockJobWorker();
        contentStore = createMockContentStore();

        contentIds = new ArrayList<String>();
        contentIds.add("content-id");

        worker = new DeletePostJobWorker(predecessor,
                                         contentStore,
                                         spaceId,
                                         contentIds);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(predecessor);
        EasyMock.verify(contentStore);
    }

    private AmazonMapReduceJobWorker createMockJobWorker() {
        AmazonMapReduceJobWorker worker = EasyMock.createMock(
            "PredecessorWorker",
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(worker.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.COMPLETE);

        EasyMock.replay(worker);
        return worker;
    }

    private ContentStore createMockContentStore() throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);

        contentStore.deleteContent(EasyMock.isA(String.class),
                                   EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        EasyMock.replay(contentStore);
        return contentStore;
    }

    @Test
    public void testRun() throws Exception {
        worker.run();
    }
}
