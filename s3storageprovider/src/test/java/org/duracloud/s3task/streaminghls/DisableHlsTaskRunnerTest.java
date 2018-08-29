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

import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import org.duracloud.s3storageprovider.dto.DisableStreamingTaskParameters;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 17, 2018
 */
public class DisableHlsTaskRunnerTest extends HlsTaskRunnerTestBase {

    @Test
    public void testGetName() {
        DisableHlsTaskRunner runner =
            new DisableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client, cfClient);

        replayMocks();

        String name = runner.getName();
        assertEquals("disable-hls", name);
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
                .andReturn(props);

        unwrappedS3Provider.setNewSpaceProperties(EasyMock.eq(spaceId), EasyMock.isA(Map.class));
        EasyMock.expectLastCall().once();

        ListDistributionsResult listDistResult = new ListDistributionsResult().withDistributionList(
            new DistributionList().withItems(new ArrayList()).withIsTruncated(false));
        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(listDistResult)
                .once();

        DisableHlsTaskRunner runner =
            new DisableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client, cfClient);

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
        DisableStreamingTaskParameters taskParams = new DisableStreamingTaskParameters();
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
     * bucket and is disabled.
     */
    @Test
    public void testPerformTaskSuccess() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        Map<String, String> props = new HashMap<>();
        props.put(HLS_STREAMING_HOST_PROP, domainName);
        props.put(HLS_STREAMING_TYPE_PROP,
                  BaseHlsTaskRunner.STREAMING_TYPE.SECURE.name());
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(props);

        Capture<Map<String, String>> spacePropsCapture = Capture.newInstance();
        unwrappedS3Provider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                                  EasyMock.capture(spacePropsCapture));
        EasyMock.expectLastCall().once();

        cfClientExpectValidDistribution(cfClient);

        s3Client.deleteBucketPolicy(EasyMock.eq(bucketName));
        EasyMock.expectLastCall().once();

        DisableHlsTaskRunner runner =
            new DisableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client, cfClient);

        // Replay mocks
        replayMocks();

        // Verify success
        DisableStreamingTaskParameters taskParams = new DisableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        assertTrue(results.contains("completed"));

        // Verify captured properties
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        assertFalse(spaceProps.containsKey(HLS_STREAMING_HOST_PROP));
        assertFalse(spaceProps.containsKey(HLS_STREAMING_TYPE_PROP));
    }

}
