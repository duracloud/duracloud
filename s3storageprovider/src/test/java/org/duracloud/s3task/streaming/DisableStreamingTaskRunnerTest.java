/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskParameters;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class DisableStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    protected DisableStreamingTaskRunner createRunner(StorageProvider s3Provider,
                                                      S3StorageProvider unwrappedS3Provider,
                                                      AmazonS3Client s3Client,
                                                      AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
        return new DisableStreamingTaskRunner(s3Provider, unwrappedS3Provider,
                                              s3Client, cfClient);
    }

    @Test
    public void testGetName() throws Exception {
        DisableStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV1());

        String name = runner.getName();
        assertEquals("disable-streaming", name);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket. An exception should be thrown.
     */
    @Test
    public void testPerformTask1() throws Exception {
        DisableStreamingTaskRunner runner =
            createRunner(createMockStorageProviderV2(true),
                         createMockUnwrappedS3StorageProviderV2(),
                         createMockS3ClientV1(),
                         createMockCFClientV3());

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        DisableStreamingTaskParameters taskParams = new DisableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        testCapturedProps();
    }

    private void testCapturedProps() {
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        String propName = DisableStreamingTaskRunner.STREAMING_HOST_PROP;
        assertFalse(spaceProps.containsKey(propName));
    }

    /*
     * Testing the case where a streaming distribution exists for the given
     * bucket and will be disabled.
     */
    @Test
    public void testPerformTask2() throws Exception {
        DisableStreamingTaskRunner runner =
            createRunner(createMockStorageProviderV2(true),
                         createMockUnwrappedS3StorageProviderV2(),
                         createMockS3ClientV3(),
                         createMockCFClientV4());

        DisableStreamingTaskParameters taskParams = new DisableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        testCapturedProps();
    }

    /*
     * For testing the case where a distribution exists and will be disabled
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - return dist with matching bucket name, enabled
     */
    private AmazonCloudFrontClient createMockCFClientV4() throws Exception {
        cfClient = EasyMock.createMock(AmazonCloudFrontClient.class);

        cfClientExpectValidDistribution(cfClient);

        EasyMock.replay(cfClient);
        return cfClient;
    }

}
