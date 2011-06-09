/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.util.ContentStreamUtil;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author Andrew Woods
 *         Date: 6/8/11
 */
public class PassFailPostJobWorkerTest {

    private PassFailPostJobWorker worker;

    private AmazonMapReduceJobWorker predecessor;
    private ContentStore contentStore;

    private ContentStreamUtil streamUtil = new ContentStreamUtil();
    private String serviceWorkDir = System.getProperty("java.io.tmpdir");
    private String spaceId = "space-id";
    private String contentId = "content-id";
    private long sleepMillis = 100;

    private InputStream stream;
    private static final int NUM_LINES = 5;

    @Before
    public void setUp() throws Exception {
        predecessor = EasyMock.createMock("AmazonMapReduceJobWorker",
                                          AmazonMapReduceJobWorker.class);
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(predecessor, contentStore);

        if (null != stream) {
            stream.close();
        }
    }

    private void replayMocks() {
        EasyMock.replay(predecessor, contentStore);
    }

    @Test
    public void testWithErrors() throws ContentStoreException {
        doTest(true);
    }

    @Test
    public void testWithoutErrors() throws ContentStoreException {
        doTest(false);
    }

    private void doTest(boolean hasError) throws ContentStoreException {
        contentStore = setContentStoreExpectations();
        replayMocks();

        worker = createWorker(hasError);

        worker.doWork();

        String errorMsg = worker.getError();
        if (hasError) {
            Assert.assertNotNull(errorMsg);
            // skip first line (header)
            Assert.assertEquals(NUM_LINES - 1 + " errors", errorMsg);
        } else {
            Assert.assertNull(errorMsg);
        }
    }

    private ContentStore setContentStoreExpectations()
        throws ContentStoreException {

        stream = createStreamWithLines(NUM_LINES);

        Content content = new Content();
        content.setStream(stream);
        EasyMock.expect(contentStore.getContent(spaceId, contentId)).andReturn(
            content);

        return contentStore;
    }

    private InputStream createStreamWithLines(int numLines) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numLines; ++i) {
            sb.append(i + ":hello");
            sb.append(System.getProperty("line.separator"));
        }

        if (numLines > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private PassFailPostJobWorker createWorker(boolean error) {
        return new PassFailPostJobWorkerImpl(predecessor,
                                             contentStore,
                                             serviceWorkDir,
                                             spaceId,
                                             contentId,
                                             sleepMillis,
                                             error);
    }

    /**
     * This private class is a simple test implementation of the PassFailPostJobWorker.
     */
    private class PassFailPostJobWorkerImpl extends PassFailPostJobWorker {
        private boolean error;

        public PassFailPostJobWorkerImpl(AmazonMapReduceJobWorker predecessor,
                                         ContentStore contentStore,
                                         String serviceWorkDir,
                                         String spaceId,
                                         String contentId,
                                         long sleepMillis,
                                         boolean error) {
            super(predecessor,
                  contentStore,
                  streamUtil,
                  serviceWorkDir,
                  spaceId,
                  contentId,
                  sleepMillis);
            this.error = error;
        }

        @Override
        protected boolean isError(String line) {
            return error;
        }
    }
}
