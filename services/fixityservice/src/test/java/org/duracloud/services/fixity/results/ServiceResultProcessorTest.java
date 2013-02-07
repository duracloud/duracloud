/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.status.StatusListener;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.duracloud.services.fixity.results.ServiceResult.DELIM;

/**
 * @author Andrew Woods
 *         Date: Aug 9, 2010
 */
public class ServiceResultProcessorTest {

    private ServiceResultProcessor processor;
    private StatusListener statusListener;

    private String header = "header-value";

    private String outputSpaceId = "output-space-id";
    private String outputContentId = "output-content-id";
    private String errorContentId = "error-content-id";

    private String mime = "text/tab-separated-values";

    private final String SPACE_PREFIX = "test-space-id-";
    private final String CONTENT_PREFIX = "test-content-id-";
    private final String HASH_PREFIX = "test-hash-";

    private File workDir = new File("target/test-result-processor");
    BufferedReader reader;

    @Before
    public void setUp() throws Exception {
        if (!workDir.exists()) {
            Assert.assertTrue(workDir.mkdir());
        }


    }

    @After
    public void tearDown() throws Exception {
        processor = null;
        if (reader != null) {
            reader.close();
        }
        
        if (null != statusListener) {
            EasyMock.verify(statusListener);
        }
    }

    @Test
    public void testSetProcessingComplete() throws Exception {
        statusListener = null;
        processor = createProcessor(statusListener);

        String status = processor.getProcessingStatus().toString();
        Assert.assertNotNull(status);

        ServiceResultListener.StatusMsg msg = new ServiceResultListener.StatusMsg(
            status);
        Assert.assertEquals(ServiceResultListener.State.IN_PROGRESS,
                            msg.getState());

        processor.setProcessingState(ServiceResultListener.State.COMPLETE);
        status = processor.getProcessingStatus().toString();
        Assert.assertNotNull(status);

        msg = new ServiceResultListener.StatusMsg(status);
        Assert.assertEquals(ServiceResultListener.State.COMPLETE,
                            msg.getState());
    }

    @Test
    public void testProcessServiceResult() throws Exception {
        statusListener = createMockListener(0);
        processor = createProcessor(statusListener);

        ServiceResultListener.State inProgress = ServiceResultListener.State.IN_PROGRESS;

        boolean success = true;
        int index = 0;
        processResult(success, index++);
        verifyStatus(inProgress.name(), "1", "0", "?");

        success = true;
        processResult(success, index++);
        verifyStatus(inProgress.name(), "2", "0", "?");

        success = false;
        processResult(success, index++);
        verifyStatus(inProgress.name(), "3", "1", "?");

        processor.setTotalWorkItems(5);
        verifyStatus(inProgress.name(), "3", "1", "5");

        success = true;
        processResult(success, index++);
        verifyStatus(inProgress.name(), "4", "1", "5");

        success = false;
        processResult(success, index++);
        verifyStatus(inProgress.name(), "5", "2", "5");

        verifyLocalOutputFile();
        verifyLocalErrorFile(2);
    }

    @Test
    public void testSetTotalWorkItems() throws Exception {
        long numItems = 0;
        int numErrors = 1;
        doTestSetTotalWorkItems(numItems, numErrors);

        numItems = 1;
        numErrors = 0;
        doTestSetTotalWorkItems(numItems, numErrors);

        numItems = 10;
        numErrors = 0;
        doTestSetTotalWorkItems(numItems, numErrors);
    }

    private void doTestSetTotalWorkItems(long numItems, int numErrors)
        throws Exception {
        statusListener = createMockListener(numErrors);
        processor = createProcessor(statusListener);

        processor.setTotalWorkItems(numItems);
    }

    private StatusListener createMockListener(int numErrors) {
        StatusListener listener = EasyMock.createMock("StatusListener",
                                                      StatusListener.class);
        if (numErrors > 0) {
            listener.setError(EasyMock.<String>anyObject());
            EasyMock.expectLastCall().times(numErrors);
        }

        EasyMock.replay(listener);
        return listener;
    }

    private ServiceResultProcessor createProcessor(StatusListener listener)
        throws Exception {
        ContentStore contentStore = createContentStore();
        ServiceResultProcessor processor =
            new ServiceResultProcessor(contentStore,
                                       header,
                                       listener,
                                       outputSpaceId,
                                       outputContentId,
                                       errorContentId,
                                       "test-hashing",
                                       "previous status blah",
                                       workDir);
        File file = new File(workDir, outputContentId);
        Assert.assertTrue(file.exists());
        Assert.assertTrue(FileUtils.readFileToString(file).startsWith(header));

        return processor;
    }

    private void processResult(boolean success, int index) {
        ServiceResult result = new HashFinderResult(success,
                                                    SPACE_PREFIX + index,
                                                    CONTENT_PREFIX + index,
                                                    HASH_PREFIX + index);

        processor.processServiceResult(result);
    }

    private void verifyStatus(String status,
                              String numProcessed,
                              String numFailure,
                              String totalWorkItems) {
        String text = processor.getProcessingStatus().toString();
        Assert.assertNotNull(text);
        System.out.println("s: '" + text + "'");
        ServiceResultListener.StatusMsg msg = new ServiceResultListener.StatusMsg(
            text);

        Assert.assertEquals(status, msg.getState().name());
        Assert.assertEquals(Long.parseLong(numFailure), msg.getFailed());
        Assert.assertEquals(Long.parseLong(numProcessed),
                            msg.getPassed() + msg.getFailed());

        long totalExpected = -1;
        if (!totalWorkItems.equals("?")) {
            totalExpected = Long.parseLong(totalWorkItems);
        }
        Assert.assertEquals(totalExpected, msg.getTotal());


    }

    private void verifyLocalOutputFile() throws IOException {
        HashFinderResult result = new HashFinderResult(true, "", "", "");
        File file = new File(workDir, outputContentId);
        Assert.assertTrue(file.exists());

        reader = new BufferedReader(new FileReader(file));
        int i = -1;
        String line;
        while ((line = reader.readLine()) != null) {
            if (i == -1) {
                Assert.assertEquals(header, line);
            } else {
                StringBuilder expected = new StringBuilder(SPACE_PREFIX);
                expected.append(i);
                expected.append(DELIM);
                expected.append(CONTENT_PREFIX);
                expected.append(i);
                expected.append(DELIM);
                expected.append(HASH_PREFIX);
                expected.append(i);
                Assert.assertEquals(expected.toString(), line);
            }
            System.out.println(line);
            i++;
        }
        Assert.assertEquals(5, i);
    }
    
    private void verifyLocalErrorFile(int errorCount) throws IOException {
        File file = new File(workDir, errorContentId);
        Assert.assertTrue(file.exists());
        BufferedReader r = new BufferedReader(new FileReader(file));

        long count = 0;
        String line;
        while ((line = r.readLine()) != null) {
            count++;
        }

        Assert.assertEquals(errorCount+1, count);
        
        
    }
    
    private ContentStore createContentStore() throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);

        EasyMock.expect(store.addContent(EasyMock.eq(outputSpaceId),
                                         EasyMock.eq(outputContentId),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.anyLong(),
                                         EasyMock.eq(mime),
                                         EasyMock.<String>anyObject(),
                                         EasyMock.<Map<String, String>>anyObject()))
            .andReturn("checksum")
            .anyTimes();

        EasyMock.expect(store.addContent(EasyMock.eq(outputSpaceId),
                                         EasyMock.eq(errorContentId),
                                         EasyMock.<InputStream>anyObject(),
                                         EasyMock.anyLong(),
                                         EasyMock.eq(mime),
                                         EasyMock.<String>anyObject(),
                                         EasyMock.<Map<String, String>>anyObject()))
            .andReturn("checksum")
            .anyTimes();

        EasyMock.replay(store);

        return store;
    }

}
