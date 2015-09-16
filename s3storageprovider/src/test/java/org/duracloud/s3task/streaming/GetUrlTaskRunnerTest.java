/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.GetUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.Test;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * @author: Bill Branan
 * Date: Apr 3, 2015
 */
public class GetUrlTaskRunnerTest extends StreamingTaskRunnerTestBase {

    private final String contentId = "content-id";

    protected BaseStreamingTaskRunner createRunner(StorageProvider s3Provider,
                                            S3StorageProvider unwrappedS3Provider,
                                            AmazonS3Client s3Client,
                                            AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
        return new GetUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient);
    }

    @Test
    public void testGetName() throws Exception {
        BaseStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV1());

        String name = runner.getName();
        assertEquals("get-url", name);
    }

    /*
     * Testing the case where a url is generated and returned
     */
    @Test
    public void testPerformTask1() throws Exception {
        BaseStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV4(false)); // Open dist

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        GetUrlTaskParameters taskParams = new GetUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        String results = runner.performTask(taskParams.serialize());
        String streamUrl = GetUrlTaskResult.deserialize(results).getStreamUrl();
        assertEquals(streamUrl, "rtmp://" + domainName + "/cfx/st/" + contentId);
    }

    /*
     * Testing the case where a distribution does not exist,
     * an exception is expected
     */
    @Test
    public void testPerformTask2() throws Exception {
        BaseStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV3());

        GetUrlTaskParameters taskParams = new GetUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch(UnsupportedTaskException e) {
            assertTrue(e.getMessage()
                        .contains(StorageTaskConstants.ENABLE_STREAMING_TASK_NAME));
        }
    }

    

    /*
     * Testing the case where a distribution is secure rather than open,
     * an exception is expected
     */
    @Test
    public void testPerformTask3() throws Exception {
        BaseStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV4(true)); // Secure dist

        GetUrlTaskParameters taskParams = new GetUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch(UnsupportedTaskException e) {
            assertTrue(e.getMessage()
                        .contains(StorageTaskConstants.GET_SIGNED_URL_TASK_NAME));
        }
    }
    
    /*
     * Testing the case where streaming is not enabled, an exception is expected
     */
    @Test
    public void testPerformTask4() throws Exception {
        BaseStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(new HashMap<String,String>(), true),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV1());

        GetUrlTaskParameters taskParams = new GetUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch(UnsupportedTaskException e) {
            assertTrue(e.getMessage()
                        .contains(StorageTaskConstants.ENABLE_STREAMING_TASK_NAME));
        }
    }

    /*
     * For testing the case where a URL is generated from an existing
     * distribution
     */
    private AmazonCloudFrontClient createMockCFClientV4(boolean secure) throws Exception {
        cfClient = EasyMock.createMock(AmazonCloudFrontClient.class);

        cfClientExpectValidDistribution(cfClient, secure);

        EasyMock.replay(cfClient);
        return cfClient;
    }

}
