/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import static org.duracloud.s3task.streaminghls.BaseHlsTaskRunner.HLS_STREAMING_HOST_PROP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import org.duracloud.s3storageprovider.dto.GetHlsUrlTaskParameters;
import org.duracloud.s3storageprovider.dto.GetUrlTaskResult;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 22, 2018
 */
public class GetUrlHlsTaskRunnerTest extends HlsTaskRunnerTestBase {

    @Test
    public void testGetName() {
        GetUrlHlsTaskRunner runner =
            new GetUrlHlsTaskRunner(s3Provider, unwrappedS3Provider, cfClient);

        replayMocks();

        String name = runner.getName();
        assertEquals("get-url-hls", name);
    }

    /*
     * Testing the case where a distribution domain is not listed in space properties
     * (mostly likely meaning streaming is not enabled), an exception is expected
     */
    @Test
    public void testPerformTaskNoDistributionDomain() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(new HashMap<>());

        GetUrlHlsTaskRunner runner =
            new GetUrlHlsTaskRunner(s3Provider, unwrappedS3Provider, cfClient);

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
        GetHlsUrlTaskParameters taskParams = new GetHlsUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }

    /*
     * Testing the case where a distribution does not exist for the given bucket,
     * an exception is expected
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

        // Empty distribution list
        ListDistributionsResult distSummaryResult =
            new ListDistributionsResult().withDistributionList(new DistributionList());
        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(distSummaryResult);

        GetUrlHlsTaskRunner runner =
            new GetUrlHlsTaskRunner(s3Provider, unwrappedS3Provider, cfClient);

        // Replay mocks
        replayMocks();

        // Verify failure when the space does not have an associated distribution
        GetHlsUrlTaskParameters taskParams = new GetHlsUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch (Exception expected) {
            assertNotNull(expected);
        }
    }

    /*
     * Testing the case where a streaming distribution exists for the given
     * bucket and a URL is successfully generated.
     */
    @Test
    public void testPerformTaskSuccess() {
        // Setup mocks
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        Map<String, String> props = new HashMap<>();
        props.put(HLS_STREAMING_HOST_PROP, domainName);
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(props);

        cfClientExpectValidDistribution(cfClient);

        GetUrlHlsTaskRunner runner =
            new GetUrlHlsTaskRunner(s3Provider, unwrappedS3Provider, cfClient);

        // Replay mocks
        replayMocks();

        // Verify success
        GetHlsUrlTaskParameters taskParams = new GetHlsUrlTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setContentId(contentId);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        GetUrlTaskResult taskResult = GetUrlTaskResult.deserialize(results);
        assertNotNull(taskResult);

        String streamUrl = taskResult.getStreamUrl();
        assertNotNull(streamUrl);
        assertEquals("https://" + domainName + "/" + contentId, streamUrl);
    }

}
