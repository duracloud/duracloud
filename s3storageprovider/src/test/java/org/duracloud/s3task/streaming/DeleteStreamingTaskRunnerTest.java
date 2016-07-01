/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.DeleteStreamingDistributionRequest;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionRequest;
import com.amazonaws.services.cloudfront.model.GetStreamingDistributionResult;
import com.amazonaws.services.cloudfront.model.StreamingDistribution;
import com.amazonaws.services.cloudfront.model.StreamingDistributionConfig;
import com.amazonaws.services.cloudfront.model.UpdateStreamingDistributionRequest;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskParameters;
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
public class DeleteStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    protected DeleteStreamingTaskRunner createRunner(StorageProvider s3Provider,
                                                     S3StorageProvider unwrappedS3Provider,
                                                     AmazonS3Client s3Client,
                                                     AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
        return new DeleteStreamingTaskRunner(s3Provider, unwrappedS3Provider,
                                             s3Client, cfClient);
    }

    @Test
    public void testGetName() throws Exception {
        DeleteStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV1());

        String name = runner.getName();
        assertEquals("delete-streaming", name);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket. An exception should be thrown.
     */
    @Test
    public void testPerformTask1() throws Exception {
        DeleteStreamingTaskRunner runner =
            createRunner(createMockStorageProviderV2(true),
                         createMockUnwrappedS3StorageProviderV2(),
                         createMockS3ClientV3(),
                         createMockCFClientV3());

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        DeleteStreamingTaskParameters taskParams = new DeleteStreamingTaskParameters();
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
     * bucket and will be deleted.
     */
    @Test
    public void testPerformTask2() throws Exception {
        DeleteStreamingTaskRunner runner =
            createRunner(createMockStorageProviderV2(true),
                         createMockUnwrappedS3StorageProviderV2(),
                         createMockS3ClientV3(),
                         createMockCFClientV4());

        DeleteStreamingTaskParameters taskParams = new DeleteStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        testCapturedProps();

        Thread.sleep(2000); // Give time for delete thread to execute before mock verify
    }

    /*
     * For testing the case where a distribution exists and will be deleted
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - return dist with matching bucket name, enabled
     * getStreamingDistributionConfig (1) - distribution config, enabled
     * updateStreamingDistribution (1) - return null
     * getStreamingDistribution (1) - return valid dist, deployed
     * deleteStreamingDistribution (1) - void return
     */
    private AmazonCloudFrontClient createMockCFClientV4() throws Exception {
        cfClient = EasyMock.createMock(AmazonCloudFrontClient.class);

        cfClientExpectValidDistribution(cfClient);

        StreamingDistributionConfig distConfig =
            new StreamingDistributionConfig().withEnabled(true);
        GetStreamingDistributionConfigResult distConfigResult =
            new GetStreamingDistributionConfigResult()
                .withStreamingDistributionConfig(distConfig);

        EasyMock
            .expect(cfClient.getStreamingDistributionConfig(
                EasyMock.isA(GetStreamingDistributionConfigRequest.class)))
            .andReturn(distConfigResult)
            .times(2);

        EasyMock
            .expect(cfClient.updateStreamingDistribution(
                EasyMock.isA(UpdateStreamingDistributionRequest.class)))
            .andReturn(null)
            .times(1);

        StreamingDistribution dist =
            new StreamingDistribution().withStatus("Deployed");
        GetStreamingDistributionResult distResult =
            new GetStreamingDistributionResult().withStreamingDistribution(dist);
        EasyMock
            .expect(cfClient.getStreamingDistribution(
                EasyMock.isA(GetStreamingDistributionRequest.class)))
            .andReturn(distResult)
            .times(1);

        cfClient.deleteStreamingDistribution(
            EasyMock.isA(DeleteStreamingDistributionRequest.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(cfClient);
        return cfClient;
    }

}
