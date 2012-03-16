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
import org.duracloud.services.ComputeService;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.duracloud.services.amazonmapreduce.util.ContentStreamUtil;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

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
    private String errorReportContentId = "error-report-content-id";

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
        if (hasError) {
            EasyMock.expect(contentStore.getStoreId()).andReturn("0");
            EasyMock.expect(contentStore.addContent((String)EasyMock.anyObject(),
                                                    (String)EasyMock.anyObject(),
                                                    (InputStream)EasyMock.anyObject(),
                                                    EasyMock.anyLong(),
                                                    (String)EasyMock.anyObject(),
                                                    (String)EasyMock.anyObject(),
                                                    (Map<String, String>) EasyMock.anyObject()))
                    .andReturn(errorReportContentId);
        }
        
        replayMocks();

        worker = createWorker(hasError);

        worker.doWork();

        Assert.assertNull(worker.getError());

        Map<String,String> bp = worker.getBubbleableProperties();
        
        long itemCount = NUM_LINES - 1;

        long total =
            Long.valueOf(bp.get(ComputeService.ITEMS_PROCESS_COUNT)).longValue();
        
        long failureCount =
            Long.valueOf(bp.get(ComputeService.FAILURE_COUNT_KEY)).longValue();
        long passCount =
            Long.valueOf(bp.get(ComputeService.PASS_COUNT_KEY)).longValue();

        Assert.assertEquals(itemCount, total);

        if (hasError) {
            // skip first line (header)
            Assert.assertEquals(itemCount, failureCount);
            Assert.assertEquals(0, passCount);

        } else {
            Assert.assertEquals(0, failureCount);
            Assert.assertEquals(itemCount, passCount);
            
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
                                             errorReportContentId,
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
                                         String errorReportContentId,
                                         long sleepMillis,
                                         boolean error) {
            super(predecessor,
                  contentStore,
                  streamUtil,
                  serviceWorkDir,
                  spaceId,
                  contentId,
                  errorReportContentId,
                  sleepMillis);
            this.error = error;
        }

        @Override
        protected boolean isError(String line) {
            return error;
        }
    }
}
