/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class DisableStreamingWorkerTest {

    private ContentStore contentStore;

    @Before
    public void setUp() throws Exception {
        contentStore = createMockContentStore();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        EasyMock
            .expect(contentStore.performTask(EasyMock.eq("disable-streaming"),
                                             EasyMock.isA(String.class)))
            .andReturn("success")
            .times(1);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
        contentStore = null;
    }

    @Test
    public void testDisableStreamingWorker() throws Exception {

        DisableStreamingWorker worker =
            new DisableStreamingWorker(contentStore,
                                      "sourceSpaceId");
        worker.run();
    }

}
