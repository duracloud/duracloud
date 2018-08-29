/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import static org.duracloud.s3task.streaminghls.BaseHlsTaskRunner.HLS_STREAMING_HOST_PROP;
import static org.duracloud.s3task.streaminghls.BaseHlsTaskRunner.HLS_STREAMING_TYPE_PROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.cloudfront.model.DeleteDistributionRequest;
import com.amazonaws.services.cloudfront.model.DeleteDistributionResult;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.GetDistributionRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionResult;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import com.amazonaws.services.cloudfront.model.UpdateDistributionRequest;
import org.duracloud.s3storageprovider.dto.DeleteStreamingTaskParameters;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 8/17/2018
 */
public class DeleteHlsTaskRunnerTest extends HlsTaskRunnerTestBase {

    @Test
    public void testGetName() {
        DeleteHlsTaskRunner runner =
            new DeleteHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client, cfClient);

        replayMocks();

        String name = runner.getName();
        assertEquals("delete-hls", name);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket. An exception should be thrown.
     */
    @Test
    public void testPerformTaskNoDistribution() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        Map<String, String> props = new HashMap<>();
        props.put(HLS_STREAMING_HOST_PROP, domainName);
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(props).once();

        unwrappedS3Provider.setNewSpaceProperties(EasyMock.eq(spaceId), EasyMock.isA(Map.class));
        EasyMock.expectLastCall().once();

        s3Client.deleteBucketPolicy(EasyMock.eq(bucketName));
        EasyMock.expectLastCall().once();

        s3Client.deleteBucketCrossOriginConfiguration(EasyMock.eq(bucketName));
        EasyMock.expectLastCall().once();

        ListDistributionsResult listDistResult = new ListDistributionsResult().withDistributionList(
            new DistributionList().withItems(new ArrayList()).withIsTruncated(false));
        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(listDistResult)
                .once();

        DeleteHlsTaskRunner runner =
            new DeleteHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client, cfClient);

        // Replay mocks
        replayMocks();

        // Verify failure on null parameters
        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }

        // Verify failure when the space does not have an associated distribution
        DeleteStreamingTaskParameters taskParams = new DeleteStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }

    /*
     * Testing the case where a streaming distribution exists for the given
     * bucket and is deleted.
     */
    @Test
    public void testPerformTaskSuccess() throws Exception {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        Map<String, String> props = new HashMap<>();
        props.put(HLS_STREAMING_HOST_PROP, domainName);
        props.put(HLS_STREAMING_TYPE_PROP,
                  BaseHlsTaskRunner.STREAMING_TYPE.SECURE.name());
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(props).once();

        Capture<Map<String, String>> spacePropsCapture = Capture.newInstance();
        unwrappedS3Provider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                                  EasyMock.capture(spacePropsCapture));
        EasyMock.expectLastCall().once();

        s3Client.deleteBucketPolicy(EasyMock.eq(bucketName));
        EasyMock.expectLastCall().once();

        s3Client.deleteBucketCrossOriginConfiguration(EasyMock.eq(bucketName));
        EasyMock.expectLastCall().once();

        cfClientExpectValidDistribution(cfClient);

        DistributionConfig distConfig = new DistributionConfig().withEnabled(true);
        GetDistributionConfigResult distConfigResult =
            new GetDistributionConfigResult().withDistributionConfig(distConfig);

        EasyMock.expect(cfClient.getDistributionConfig(EasyMock.isA(GetDistributionConfigRequest.class)))
                .andReturn(distConfigResult)
                .times(2);

        EasyMock.expect(cfClient.updateDistribution(EasyMock.isA(UpdateDistributionRequest.class)))
                .andReturn(null).once();

        Distribution dist = new Distribution().withStatus("Deployed");
        GetDistributionResult distResult = new GetDistributionResult().withDistribution(dist);
        EasyMock.expect(cfClient.getDistribution(EasyMock.isA(GetDistributionRequest.class)))
                .andReturn(distResult).once();

        EasyMock.expect(cfClient.deleteDistribution(EasyMock.isA(DeleteDistributionRequest.class)))
                .andReturn(new DeleteDistributionResult()).once();

        DeleteHlsTaskRunner runner =
            new DeleteHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client, cfClient);

        // Replay mocks
        replayMocks();

        // Verify success
        DeleteStreamingTaskParameters taskParams = new DeleteStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        assertTrue(results.contains("completed"));

        // Verify captured properties
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        assertFalse(spaceProps.containsKey(HLS_STREAMING_HOST_PROP));
        assertFalse(spaceProps.containsKey(HLS_STREAMING_TYPE_PROP));

        Thread.sleep(2000); // Give time for delete thread to execute before mock verify
    }

}
