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
import static org.duracloud.s3task.streaminghls.BaseHlsTaskRunner.S3_ORIGIN_SUFFIX;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.cloudfront.model.CacheBehavior;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentity;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentityList;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentitySummary;
import com.amazonaws.services.cloudfront.model.CreateCloudFrontOriginAccessIdentityRequest;
import com.amazonaws.services.cloudfront.model.CreateCloudFrontOriginAccessIdentityResult;
import com.amazonaws.services.cloudfront.model.CreateDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateDistributionResult;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.Distribution;
import com.amazonaws.services.cloudfront.model.DistributionConfig;
import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.GetCloudFrontOriginAccessIdentityRequest;
import com.amazonaws.services.cloudfront.model.GetCloudFrontOriginAccessIdentityResult;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigRequest;
import com.amazonaws.services.cloudfront.model.GetDistributionConfigResult;
import com.amazonaws.services.cloudfront.model.ListCloudFrontOriginAccessIdentitiesRequest;
import com.amazonaws.services.cloudfront.model.ListCloudFrontOriginAccessIdentitiesResult;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.cloudfront.model.UpdateDistributionRequest;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.storage.error.UnsupportedTaskException;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: Aug 23, 2018
 */
public class EnableHlsTaskRunnerTest extends HlsTaskRunnerTestBase {

    private String oaIdentity = "origin-access-identity";

    @Test
    public void testGetName() {
        EnableHlsTaskRunner runner =
            new EnableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client,
                                    cfClient, cfAccountId, dcHost);

        replayMocks();

        String name = runner.getName();
        assertEquals("enable-hls", name);
    }

    /*
     * Testing the case where a SECURE streaming distribution exists and a call
     * is made to enable the space for OPEN streaming. An exception is expected
     */
    @Test
    public void testPerformTaskSwapDistType() {
        // Expect calls to get bucket name
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        // Origin Access Id already exists and is reused
        expectExistingOriginAccessId();

        // Distribution exists and is set to SECURE streaming
        expectExistingSecureDistribution(true);

        EnableHlsTaskRunner runner =
            new EnableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client,
                                    cfClient, cfAccountId, dcHost);
        // Replay mocks
        replayMocks();

        // Attempt create call
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(false); // OPEN streaming

        try {
            runner.performTask(taskParams.serialize());
            fail("Exception expected");
        } catch (UnsupportedTaskException e) {
            assertNotNull(e);
        }
    }

    /*
     * Testing the case where an OPEN streaming distribution exists but is
     * disabled. Enabling the distribution is successful.
     */
    @Test
    public void testPerformTaskOpenEnable() {
        // Expect calls to get bucket name
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        // Origin Access Id already exists and is reused
        expectExistingOriginAccessId();

        // Distribution exists, is set to OPEN streaming, and is disabled
        expectExistingOpenDistribution(false);

        // Distribution should be set to enabled
        Capture<UpdateDistributionRequest> updateDistRequestCapture = expectSetDistributionState();

        // Bucket policy should be set
        expectSetBucketPolicy();

        // Bucket CORS policy should be set
        Capture<BucketCrossOriginConfiguration> corsConfigCapture = expectSetCorsPolicy();

        // Bucket tags should be set
        Capture<Map<String, String>> spacePropsCapture = expectSetBucketTags();

        EnableHlsTaskRunner runner =
            new EnableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client,
                                    cfClient, cfAccountId, dcHost);
        // Replay mocks
        replayMocks();

        // Verify success
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(false); // OPEN streaming

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        EnableStreamingTaskResult taskResult = EnableStreamingTaskResult.deserialize(results);
        assertNotNull(taskResult);
        assertEquals(domainName, taskResult.getStreamingHost());

        // Verify update distribution request
        UpdateDistributionRequest updateDistRequest = updateDistRequestCapture.getValue();
        assertTrue(updateDistRequest.getDistributionConfig().getEnabled());

        // Verify CORS policy
        BucketCrossOriginConfiguration corsConfig = corsConfigCapture.getValue();
        assertEquals(1, corsConfig.getRules().size());
        CORSRule corsRule = corsConfig.getRules().get(0);
        assertEquals(1, corsRule.getAllowedOrigins().size());
        assertEquals("https://*", corsRule.getAllowedOrigins().get(0));
        assertEquals(2, corsRule.getAllowedMethods().size());
        assertEquals(1, corsRule.getAllowedHeaders().size());
        assertEquals("*", corsRule.getAllowedHeaders().get(0));

        // Verify bucket tags
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        assertEquals(domainName, spaceProps.get(HLS_STREAMING_HOST_PROP));
        assertEquals("OPEN", spaceProps.get(HLS_STREAMING_TYPE_PROP));
    }

    /*
     * Testing the case where an OPEN streaming distribution exists and is already
     * enabled. No changes should be made to the distribution.
     */
    @Test
    public void testPerformTaskNoChangesNeeded() {
        // Expect calls to get bucket name
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        // Origin Access Id already exists and is reused
        expectExistingOriginAccessId();

        // Distribution exists, is set to OPEN streaming, and is enabled
        expectExistingOpenDistribution(true);

        // Bucket policy should be set
        expectSetBucketPolicy();

        // Bucket CORS policy should be set
        Capture<BucketCrossOriginConfiguration> corsConfigCapture = expectSetCorsPolicy();

        // Bucket tags should be set
        Capture<Map<String, String>> spacePropsCapture = expectSetBucketTags();

        EnableHlsTaskRunner runner =
            new EnableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client,
                                    cfClient, cfAccountId, dcHost);
        // Replay mocks
        replayMocks();

        // Verify success
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(false); // OPEN streaming

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        EnableStreamingTaskResult taskResult = EnableStreamingTaskResult.deserialize(results);
        assertNotNull(taskResult);
        assertEquals(domainName, taskResult.getStreamingHost());

        // Verify CORS policy
        BucketCrossOriginConfiguration corsConfig = corsConfigCapture.getValue();
        assertEquals(1, corsConfig.getRules().size());
        CORSRule corsRule = corsConfig.getRules().get(0);
        assertEquals(1, corsRule.getAllowedOrigins().size());
        assertEquals("https://*", corsRule.getAllowedOrigins().get(0));
        assertEquals(2, corsRule.getAllowedMethods().size());
        assertEquals(1, corsRule.getAllowedHeaders().size());
        assertEquals("*", corsRule.getAllowedHeaders().get(0));

        // Verify bucket tags
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        assertEquals(domainName, spaceProps.get(HLS_STREAMING_HOST_PROP));
        assertEquals("OPEN", spaceProps.get(HLS_STREAMING_TYPE_PROP));
    }

    /*
     * Testing the case where an OPEN streaming distribution does not exist and
     * is successfully created
     */
    @Test
    public void testPerformTaskOpenSuccess() {
        // Expect calls to get bucket name
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        // Origin Access Id does not exist and should be created
        expectNewOriginAccessId();

        // Distribution does not exist and should be created
        Capture<CreateDistributionRequest> createDistRequestCapture = expectNewDistribution();

        // Bucket policy should be set
        expectSetBucketPolicy();

        // Bucket CORS policy should be set
        Capture<BucketCrossOriginConfiguration> corsConfigCapture = expectSetCorsPolicy();

        // Bucket tags should be set
        Capture<Map<String, String>> spacePropsCapture = expectSetBucketTags();

        EnableHlsTaskRunner runner =
            new EnableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client,
                                    cfClient, cfAccountId, dcHost);
        // Replay mocks
        replayMocks();

        // Verify success
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(false); // OPEN streaming

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        EnableStreamingTaskResult taskResult = EnableStreamingTaskResult.deserialize(results);
        assertNotNull(taskResult);

        assertEquals(taskResult.getStreamingHost(), domainName);

        // Verify distribution request
        DistributionConfig distConfig = createDistRequestCapture.getValue().getDistributionConfig();
        assertTrue(distConfig.getEnabled());
        // No additional cache behaviors for OPEN streaming
        assertNull(distConfig.getCacheBehaviors());
        // Default cache behavior
        DefaultCacheBehavior cacheBehavior = distConfig.getDefaultCacheBehavior();
        assertFalse(cacheBehavior.getTrustedSigners().getEnabled()); // No trusted signers for OPEN streaming
        assertEquals(new Integer(0), cacheBehavior.getTrustedSigners().getQuantity());
        assertEquals(0, cacheBehavior.getTrustedSigners().getItems().size());
        assertEquals(new Integer(3), cacheBehavior.getAllowedMethods().getQuantity());
        assertEquals(3, cacheBehavior.getAllowedMethods().getItems().size());
        assertEquals(new Integer(3), cacheBehavior.getForwardedValues().getHeaders().getQuantity());
        assertEquals(3, cacheBehavior.getForwardedValues().getHeaders().getItems().size());
        assertFalse(cacheBehavior.getForwardedValues().getQueryString());
        assertEquals("none", cacheBehavior.getForwardedValues().getCookies().getForward());
        // One origin for OPEN streaming
        assertEquals(new Integer(1), distConfig.getOrigins().getQuantity());
        assertEquals(1, distConfig.getOrigins().getItems().size());
        assertTrue(distConfig.getOrigins().getItems().get(0).getDomainName().startsWith(bucketName));

        // Verify CORS policy
        BucketCrossOriginConfiguration corsConfig = corsConfigCapture.getValue();
        assertEquals(1, corsConfig.getRules().size());
        CORSRule corsRule = corsConfig.getRules().get(0);
        assertEquals(1, corsRule.getAllowedOrigins().size());
        assertEquals("https://*", corsRule.getAllowedOrigins().get(0));
        assertEquals(2, corsRule.getAllowedMethods().size());
        assertEquals(1, corsRule.getAllowedHeaders().size());
        assertEquals("*", corsRule.getAllowedHeaders().get(0));

        // Verify bucket tags
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        assertEquals(domainName, spaceProps.get(HLS_STREAMING_HOST_PROP));
        assertEquals("OPEN", spaceProps.get(HLS_STREAMING_TYPE_PROP));
    }

    /*
     * Testing the case where a SECURE streaming distribution does not exist and
     * is successfully created
     */
    @Test
    public void testPerformTaskSecureSuccess() {
        // Expect calls to get bucket name
        EasyMock.expect(unwrappedS3Provider.getBucketName(EasyMock.isA(String.class)))
                .andReturn(bucketName);

        // Origin Access Id already exists and is reused
        expectExistingOriginAccessId();

        // Distribution does not exist and should be created
        Capture<CreateDistributionRequest> createDistRequestCapture = expectNewDistribution();

        // Bucket policy should be set
        expectSetBucketPolicy();

        // Bucket CORS policy should be set
        Capture<BucketCrossOriginConfiguration> corsConfigCapture = expectSetCorsPolicy();

        // Bucket tags should be set
        Capture<Map<String, String>> spacePropsCapture = expectSetBucketTags();

        EnableHlsTaskRunner runner =
            new EnableHlsTaskRunner(s3Provider, unwrappedS3Provider, s3Client,
                                    cfClient, cfAccountId, dcHost);
        // Replay mocks
        replayMocks();

        // Verify success
        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(true); // SECURE streaming

        List<String> allowedOrigins = new ArrayList<>();
        String corsOrigin0 = "https://test.com";
        String corsOrigin1 = "http://*.check.org";
        allowedOrigins.add(corsOrigin0);
        allowedOrigins.add(corsOrigin1);
        taskParams.setAllowedOrigins(allowedOrigins);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        EnableStreamingTaskResult taskResult = EnableStreamingTaskResult.deserialize(results);
        assertNotNull(taskResult);

        assertEquals(taskResult.getStreamingHost(), domainName);

        // Verify distribution request
        DistributionConfig distConfig = createDistRequestCapture.getValue().getDistributionConfig();
        assertTrue(distConfig.getEnabled());
        // One additional cache behavior for SECURE streaming
        assertEquals(new Integer(1), distConfig.getCacheBehaviors().getQuantity());
        assertEquals(1, distConfig.getCacheBehaviors().getItems().size());
        CacheBehavior altCacheBehavior = distConfig.getCacheBehaviors().getItems().get(0);
        assertEquals("/cookies", altCacheBehavior.getPathPattern());
        assertTrue(altCacheBehavior.getTargetOriginId().contains(dcHost));
        assertEquals(new Integer(2), altCacheBehavior.getAllowedMethods().getQuantity());
        assertEquals(2, altCacheBehavior.getAllowedMethods().getItems().size());
        assertTrue(altCacheBehavior.getForwardedValues().getQueryString());
        assertEquals("all", altCacheBehavior.getForwardedValues().getCookies().getForward());
        assertFalse(altCacheBehavior.getTrustedSigners().getEnabled());
        // Default cache behavior
        DefaultCacheBehavior cacheBehavior = distConfig.getDefaultCacheBehavior();
        assertTrue(cacheBehavior.getTrustedSigners().getEnabled()); // Trusted signers enabled for SECURE streaming
        assertEquals(new Integer(1), cacheBehavior.getTrustedSigners().getQuantity());
        assertEquals(1, cacheBehavior.getTrustedSigners().getItems().size());
        assertEquals(new Integer(3), cacheBehavior.getAllowedMethods().getQuantity());
        assertEquals(3, cacheBehavior.getAllowedMethods().getItems().size());
        assertEquals(new Integer(3), cacheBehavior.getForwardedValues().getHeaders().getQuantity());
        assertEquals(3, cacheBehavior.getForwardedValues().getHeaders().getItems().size());
        assertFalse(cacheBehavior.getForwardedValues().getQueryString());
        assertEquals("none", cacheBehavior.getForwardedValues().getCookies().getForward());
        // Two origins for SECURE streaming
        assertEquals(new Integer(2), distConfig.getOrigins().getQuantity());
        assertEquals(2, distConfig.getOrigins().getItems().size());
        assertTrue(distConfig.getOrigins().getItems().get(0).getDomainName().startsWith(bucketName));
        assertEquals("/durastore/aux", distConfig.getOrigins().getItems().get(1).getOriginPath());

        // Verify CORS policy
        BucketCrossOriginConfiguration corsConfig = corsConfigCapture.getValue();
        assertEquals(3, corsConfig.getRules().size());

        CORSRule corsRule0 = corsConfig.getRules().get(0);
        assertEquals(1, corsRule0.getAllowedOrigins().size());
        assertEquals(corsOrigin0, corsRule0.getAllowedOrigins().get(0));
        assertEquals(2, corsRule0.getAllowedMethods().size());
        assertEquals(1, corsRule0.getAllowedHeaders().size());
        assertEquals("*", corsRule0.getAllowedHeaders().get(0));

        CORSRule corsRule1 = corsConfig.getRules().get(1);
        assertEquals(1, corsRule1.getAllowedOrigins().size());
        assertEquals(corsOrigin1, corsRule1.getAllowedOrigins().get(0));
        assertEquals(2, corsRule1.getAllowedMethods().size());
        assertEquals(1, corsRule1.getAllowedHeaders().size());
        assertEquals("*", corsRule1.getAllowedHeaders().get(0));

        CORSRule corsRule2 = corsConfig.getRules().get(2);
        assertEquals(1, corsRule2.getAllowedOrigins().size());
        assertEquals("https://" + dcHost, corsRule2.getAllowedOrigins().get(0));
        assertEquals(2, corsRule2.getAllowedMethods().size());
        assertEquals(1, corsRule2.getAllowedHeaders().size());
        assertEquals("*", corsRule2.getAllowedHeaders().get(0));

        // Verify bucket tags
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        assertEquals(domainName, spaceProps.get(HLS_STREAMING_HOST_PROP));
        assertEquals("SECURE", spaceProps.get(HLS_STREAMING_TYPE_PROP));
    }

    /*
     * Adds expectations for calls where an Origin Access Id does not exist
     * and is created
     */
    private void expectNewOriginAccessId() {
        List<CloudFrontOriginAccessIdentitySummary> oaiSummaryList = new ArrayList<>();
        CloudFrontOriginAccessIdentityList oaiList =
            new CloudFrontOriginAccessIdentityList().withItems(oaiSummaryList);
        ListCloudFrontOriginAccessIdentitiesResult listOaiResult =
            new ListCloudFrontOriginAccessIdentitiesResult()
                .withCloudFrontOriginAccessIdentityList(oaiList);
        EasyMock.expect(cfClient.listCloudFrontOriginAccessIdentities(
            EasyMock.isA(ListCloudFrontOriginAccessIdentitiesRequest.class)))
                .andReturn(listOaiResult)
                .times(1);

        CloudFrontOriginAccessIdentity oai =
            new CloudFrontOriginAccessIdentity().withId(oaIdentity);
        CreateCloudFrontOriginAccessIdentityResult createOaiResult =
            new CreateCloudFrontOriginAccessIdentityResult()
                .withCloudFrontOriginAccessIdentity(oai);
        EasyMock
            .expect(cfClient.createCloudFrontOriginAccessIdentity(
                EasyMock.isA(CreateCloudFrontOriginAccessIdentityRequest.class)))
            .andReturn(createOaiResult)
            .times(1);
    }

    /*
     * Adds expectations for calls where an Origin Access Id already exists and is reused
     */
    private void expectExistingOriginAccessId() {
        List<CloudFrontOriginAccessIdentitySummary> oaiSummaryList = new ArrayList<>();
        oaiSummaryList.add(new CloudFrontOriginAccessIdentitySummary().withId(oaIdentity));
        CloudFrontOriginAccessIdentityList oaiList =
            new CloudFrontOriginAccessIdentityList().withItems(oaiSummaryList);
        ListCloudFrontOriginAccessIdentitiesResult listOaiResult =
            new ListCloudFrontOriginAccessIdentitiesResult()
                .withCloudFrontOriginAccessIdentityList(oaiList);
        EasyMock.expect(cfClient.listCloudFrontOriginAccessIdentities(
            EasyMock.isA(ListCloudFrontOriginAccessIdentitiesRequest.class)))
                .andReturn(listOaiResult)
                .times(1);
    }

    /*
     * Adds expectations for calls where a distribution for a bucket does not
     * exist and is created
     */
    private Capture<CreateDistributionRequest> expectNewDistribution() {
        // Call to list distributions returns a 0-length array
        ListDistributionsResult result = new ListDistributionsResult().withDistributionList(
            new DistributionList().withItems(new ArrayList()).withIsTruncated(false));

        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(result)
                .times(1);

        // Capture and return request to create distribution
        Distribution dist = new Distribution().withDomainName(domainName);
        CreateDistributionResult createDistResult =
            new CreateDistributionResult().withDistribution(dist);
        Capture<CreateDistributionRequest> createDistRequestCapture = Capture.newInstance();
        EasyMock.expect(cfClient.createDistribution(EasyMock.capture(createDistRequestCapture)))
                .andReturn(createDistResult)
                .times(1);

        return createDistRequestCapture;
    }

    /*
     * Adds expectations for calls where a distribution for a bucket exists and is
     * set to SECURE streaming
     */
    private void expectExistingSecureDistribution(boolean enabled) {
        TrustedSigners signers = new TrustedSigners()
            .withItems(Collections.singletonList(cfAccountId))
            .withQuantity(1)
            .withEnabled(true);
        DistributionSummary distSummary = new DistributionSummary()
            .withDefaultCacheBehavior(new DefaultCacheBehavior().withTrustedSigners(signers))
            .withOrigins(new Origins().withItems(new Origin().withDomainName(bucketName + S3_ORIGIN_SUFFIX)))
            .withEnabled(enabled);
        ListDistributionsResult result = new ListDistributionsResult().withDistributionList(
            new DistributionList().withItems(Collections.singletonList(distSummary)).withIsTruncated(false));

        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(result)
                .times(1);
    }

    /*
     * Adds expectations for calls where a distribution for a bucket exists and is
     * set to OPEN streaming
     */
    private void expectExistingOpenDistribution(boolean enabled) {
        TrustedSigners signers = new TrustedSigners().withItems(new ArrayList<>()).withEnabled(false);
        DistributionSummary distSummary = new DistributionSummary()
            .withDefaultCacheBehavior(new DefaultCacheBehavior().withTrustedSigners(signers))
            .withOrigins(new Origins().withItems(new Origin().withDomainName(bucketName + S3_ORIGIN_SUFFIX)))
            .withDomainName(domainName)
            .withEnabled(enabled);
        ListDistributionsResult result = new ListDistributionsResult().withDistributionList(
            new DistributionList().withItems(Collections.singletonList(distSummary)).withIsTruncated(false));

        EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                .andReturn(result)
                .times(1);
    }

    /*
     * Adds expectations for calls to update distribution state
     */
    private Capture<UpdateDistributionRequest> expectSetDistributionState() {
        DistributionConfig distConfig = new DistributionConfig().withEnabled(false);
        EasyMock.expect(cfClient.getDistributionConfig(EasyMock.isA(GetDistributionConfigRequest.class)))
                .andReturn(new GetDistributionConfigResult().withDistributionConfig(distConfig))
                .once();

        Capture<UpdateDistributionRequest> updateDistRequestCapture = Capture.newInstance();
        EasyMock.expect(cfClient.updateDistribution(EasyMock.capture(updateDistRequestCapture)))
                .andReturn(null)
                .once();
        return updateDistRequestCapture;
    }

    /*
     * Adds expectations for call to set the S3 bucket access policy
     */
    private void expectSetBucketPolicy() {
        CloudFrontOriginAccessIdentity oai =
            new CloudFrontOriginAccessIdentity().withId(oaIdentity);
        GetCloudFrontOriginAccessIdentityResult getOaiResult =
            new GetCloudFrontOriginAccessIdentityResult()
                .withCloudFrontOriginAccessIdentity(oai);

        EasyMock.expect(cfClient.getCloudFrontOriginAccessIdentity(
            EasyMock.isA(GetCloudFrontOriginAccessIdentityRequest.class)))
                .andReturn(getOaiResult)
                .times(1);

        s3Client.setBucketPolicy(EasyMock.eq(bucketName), EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);
    }

    /*
     * Adds expectations for calls to set S3 bucket CORS policy
     */
    private Capture<BucketCrossOriginConfiguration> expectSetCorsPolicy() {
        Capture<BucketCrossOriginConfiguration> corsConfigCapture = Capture.newInstance();
        s3Client.setBucketCrossOriginConfiguration(EasyMock.eq(bucketName),
                                                   EasyMock.capture(corsConfigCapture));
        EasyMock.expectLastCall().once();
        return corsConfigCapture;
    }

    /*
     * Adds expectations for calls to set bucket tags
     */
    private Capture<Map<String, String>> expectSetBucketTags() {
        EasyMock.expect(s3Provider.getSpaceProperties(spaceId))
                .andReturn(new HashMap<>());

        Capture<Map<String, String>> spacePropsCapture = Capture.newInstance();
        unwrappedS3Provider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                                  EasyMock.capture(spacePropsCapture));
        return spacePropsCapture;
    }

}
