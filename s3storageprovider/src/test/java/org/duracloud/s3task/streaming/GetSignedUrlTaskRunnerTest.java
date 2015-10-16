/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import java.util.HashMap;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.StorageTaskConstants;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetSignedUrlTaskResult;
import org.duracloud.s3storageprovider.dto.GetUrlTaskParameters;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author: Bill Branan
 * Date: Apr 3, 2015
 */
public class GetSignedUrlTaskRunnerTest extends StreamingTaskRunnerTestBase {

    private final String contentId = "content-id";
    private final String cfKeyId = "cf-key-id";
    private String cfKeyPath;

    @Before
    public void setup() {
        cfKeyPath = this.getClass().getClassLoader()
                        .getResource("test-signing-key.der").getPath();
    }

    protected GetSignedUrlTaskRunner createRunner(StorageProvider s3Provider,
                                                  S3StorageProvider unwrappedS3Provider,
                                                  AmazonS3Client s3Client,
                                                  AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
        return new GetSignedUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient,
                                          cfKeyId, cfKeyPath);
    }

    @Test
    public void testGetName() throws Exception {
        GetSignedUrlTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV1());

        String name = runner.getName();
        assertEquals("get-signed-url", name);
    }

    /*
     * Testing the case where a url is generated and returned with
     * the minimum parameters
     */
    @Test
    public void testPerformTask1a() throws Exception {
        GetSignedUrlTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV4(true)); // Secure dist

        try { // Make sure null params throw exception
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        GetSignedUrlTaskParameters taskParams = new GetSignedUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        String results = runner.performTask(taskParams.serialize());
        String signedUrl = GetSignedUrlTaskResult.deserialize(results).getSignedUrl();
        assertTrue(signedUrl.startsWith("rtmp://" + domainName + "/cfx/st/" + contentId));
        assertTrue(signedUrl.contains("Policy="));
        assertTrue(signedUrl.contains("Signature="));
        assertTrue(signedUrl.contains("Key-Pair-Id="));
    }

    /*
     * Testing the case where a url is generated and returned with
     * the full set of parameters
     */
    @Test
    public void testPerformTask1b() throws Exception {
        GetSignedUrlTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV4(true)); // Secure dist

        String prefix = "prefix:";

        GetSignedUrlTaskParameters taskParams = new GetSignedUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);
        taskParams.setResourcePrefix(prefix);
        taskParams.setIpAddress("1.2.3.4/32");
        taskParams.setMinutesToExpire(10);

        String results = runner.performTask(taskParams.serialize());
        String signedUrl = GetSignedUrlTaskResult.deserialize(results).getSignedUrl();
        assertTrue(signedUrl.startsWith("rtmp://" + domainName + "/cfx/st/" +
                                        prefix + contentId));
        assertTrue(signedUrl.contains("Policy="));
        assertTrue(signedUrl.contains("Signature="));
        assertTrue(signedUrl.contains("Key-Pair-Id="));
    }

    /*
     * Testing the case where a distribution does not exist,
     * an exception is expected
     */
    @Test
    public void testPerformTask2() throws Exception {
        GetSignedUrlTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV3());

        GetSignedUrlTaskParameters taskParams = new GetSignedUrlTaskParameters();
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
        GetSignedUrlTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV4(false)); // Open dist

        GetSignedUrlTaskParameters taskParams = new GetSignedUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch(UnsupportedTaskException e) {
            assertTrue(e.getMessage()
                        .contains(StorageTaskConstants.GET_URL_TASK_NAME));
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
