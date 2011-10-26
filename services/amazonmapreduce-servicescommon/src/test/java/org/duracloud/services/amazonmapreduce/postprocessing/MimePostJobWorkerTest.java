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
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Oct 3, 2010
 */
public class MimePostJobWorkerTest {

    private MimePostJobWorker worker;

    private AmazonMapReduceJobWorker predecessor;
    private ContentStore contentStore;
    private String spaceId = "space-id";

    private List<String> contents;
    private String file0 = "file.pdf";
    private String file1 = "file.pdf.tsv";
    private String file2 = "file";

    private String mime0 = "application/pdf";
    private String mime1 = "text/tab-separated-values";
    private String mime2 = "application/octet-stream";


    @Before
    public void setUp() throws Exception {
        contents = new ArrayList<String>();
        contents.add(file0);
        contents.add(file1);
        contents.add(file2);

        predecessor = createMockJobWorker();
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


    private ContentStore createMockContentStore()
        throws ContentStoreException, FileNotFoundException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);

        EasyMock.expect(contentStore.getSpaceContents(spaceId)).andReturn(
            contents.iterator());

        EasyMock.expect(contentStore.getContentProperties(EasyMock.eq(spaceId),
                                                          EasyMock.isA(String.class)))
            .andReturn(new HashMap<String, String>())
            .times(contents.size());

        contentStore.setContentProperties(EasyMock.eq(spaceId), EasyMock.isA(
            String.class), EasyMock.isA(Map.class));
        EasyMock.expectLastCall().andStubAnswer(verifyProperties());

        EasyMock.replay(contentStore);
        return contentStore;
    }

    /**
     * This method verifies that the mimeType passed into the call matches the
     * contentId argument.
     * The return value is ignore, hence the null.
     *
     * @return not used
     */
    private IAnswer<? extends Object> verifyProperties() {
        return new IAnswer<Object>() {
            public Object answer() throws Throwable {
                Object[] args = EasyMock.getCurrentArguments();
                Assert.assertNotNull(args);
                Assert.assertEquals(3, args.length);

                String contentId = (String) args[1];
                Map<String, String> properties = (Map<String, String>) args[2];
                Assert.assertEquals(1, properties.size());

                String mime = properties.get(ContentStore.CONTENT_MIMETYPE);
                Assert.assertNotNull(mime);
                if (file0.equals(contentId)) {
                    Assert.assertEquals(mime0, mime);

                } else if (file1.equals(contentId)) {
                    Assert.assertEquals(mime1, mime);

                } else if (file2.equals(contentId)) {
                    Assert.assertEquals(mime2, mime);

                } else {
                    Assert.fail("Unexpected content item: " + contentId);
                }

                return null;
            }
        };
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    @Test
    public void testRun() throws Exception {
        contentStore = createMockContentStore();
        worker = new MimePostJobWorker(predecessor, contentStore, spaceId);

        // The actual testing/verification happens in 'verifyProperties()'
        worker.run();
    }

    @Test
    public void testShutdown() throws Exception {
        contentStore = createMockContentStoreShutdown();
        worker = new MimePostJobWorker(predecessor, contentStore, spaceId);

        // call 'shutdown' in 200 millis
        new Thread(new Stopper(worker, 200)).start();

        // this call delays on content iteration for 500 millis
        worker.run();
    }

    private ContentStore createMockContentStoreShutdown()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);

        IAnswer<Iterator<String>> delayedAnswer = new DelayedAnswer();
        EasyMock.expect(contentStore.getSpaceContents(spaceId)).andStubAnswer(
            delayedAnswer);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private class DelayedAnswer implements IAnswer<Iterator<String>> {
        @Override
        public Iterator<String> answer() throws Throwable {
            sleep(500);
            return contents.iterator();
        }
    }

    /**
     * This inner class calls 'shutdown' on the provided worker after the
     * provided millis.
     */
    private class Stopper implements Runnable {

        private MimePostJobWorker worker;
        private long millis;

        public Stopper(MimePostJobWorker worker, int millis) {
            this.worker = worker;
            this.millis = millis;
        }

        @Override
        public void run() {
            sleep(millis);
            worker.shutdown();
        }
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
