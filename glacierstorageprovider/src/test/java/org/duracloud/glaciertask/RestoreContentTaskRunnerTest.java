/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glaciertask;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.storage.error.StorageStateException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author: Bill Branan
 * Date: 2/1/13
 */
public class RestoreContentTaskRunnerTest {

    private AmazonS3Client s3Client;
    private StorageProvider glacierProvider;
    private GlacierStorageProvider unwrappedGlacierProvider;
    private RestoreContentTaskRunner taskRunner;
    private String accessKey = "access-key";
    private String contentId = "content-id";
    private String spaceId = "space-id";

    @Before
    public void setup() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        glacierProvider = EasyMock.createMock("StorageProvider",
                                              StorageProvider.class);
        unwrappedGlacierProvider =
            EasyMock.createMock("GlacierStorageProvider",
                                GlacierStorageProvider.class);
        taskRunner = new RestoreContentTaskRunner(glacierProvider,
                                                  unwrappedGlacierProvider,
                                                  s3Client);
    }

    private void replayMocks() {
        EasyMock.replay(s3Client, unwrappedGlacierProvider, glacierProvider);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(s3Client, unwrappedGlacierProvider, glacierProvider);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("restore-content", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        EasyMock.expect(unwrappedGlacierProvider.getBucketName("one"))
                .andReturn("123.one");

        s3Client.restoreObject("123.one", "two/three.txt", 14);
        EasyMock.expectLastCall();

        replayMocks();

        taskRunner.performTask("one/two/three.txt");
    }

    @Test
    public void testPerformTaskRetrivalInProgress() {
        EasyMock.expect(unwrappedGlacierProvider.getBucketName("one"))
                .andReturn("123.one");

        AmazonS3Exception glacierEx = new AmazonS3Exception("err msg");
        glacierEx.setErrorCode(RestoreContentTaskRunner.RESTORE_IN_PROGRESS);
        s3Client.restoreObject("123.one", "two/three.txt", 14);
        EasyMock.expectLastCall().andThrow(glacierEx);

        replayMocks();

        try {
            taskRunner.performTask("one/two/three.txt");
            fail("Exception expected due to state of retrieval");
        } catch(StorageStateException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testPerformTaskInvalidParams() {
        try {
            taskRunner.performTask(null);
            fail("Exception expected passing a null parameter");
        } catch(RuntimeException e) {
        }

        try {
            taskRunner.performTask("");
            fail("Exception expected passing a empty parameter");
        } catch(RuntimeException e) {
        }

        try {
            taskRunner.performTask("one");
            fail("Exception expected passing an invalid parameter");
        } catch(RuntimeException e) {
        }

        replayMocks();
    }

}
