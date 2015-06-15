/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentity;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentityList;
import com.amazonaws.services.cloudfront.model.CloudFrontOriginAccessIdentitySummary;
import com.amazonaws.services.cloudfront.model.CreateCloudFrontOriginAccessIdentityRequest;
import com.amazonaws.services.cloudfront.model.CreateCloudFrontOriginAccessIdentityResult;
import com.amazonaws.services.cloudfront.model.CreateStreamingDistributionRequest;
import com.amazonaws.services.cloudfront.model.CreateStreamingDistributionResult;
import com.amazonaws.services.cloudfront.model.GetCloudFrontOriginAccessIdentityRequest;
import com.amazonaws.services.cloudfront.model.GetCloudFrontOriginAccessIdentityResult;
import com.amazonaws.services.cloudfront.model.ListCloudFrontOriginAccessIdentitiesRequest;
import com.amazonaws.services.cloudfront.model.ListCloudFrontOriginAccessIdentitiesResult;
import com.amazonaws.services.cloudfront.model.ListStreamingDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListStreamingDistributionsResult;
import com.amazonaws.services.cloudfront.model.StreamingDistribution;
import com.amazonaws.services.cloudfront.model.StreamingDistributionConfig;
import com.amazonaws.services.cloudfront.model.StreamingDistributionList;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskParameters;
import org.duracloud.s3storageprovider.dto.EnableStreamingTaskResult;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class EnableStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    private Capture<CreateStreamingDistributionRequest> createDistRequestCapture;

    protected EnableStreamingTaskRunner createRunner(StorageProvider s3Provider,
                                                     S3StorageProvider unwrappedS3Provider,
                                                     AmazonS3Client s3Client,
                                                     AmazonCloudFrontClient cfClient,
                                                     String cfAccountId) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
        this.cfAccountId = cfAccountId;
        return new EnableStreamingTaskRunner(s3Provider, unwrappedS3Provider,
                                             s3Client, cfClient, cfAccountId);
    }

    @Test
    public void testGetName() throws Exception {
        EnableStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV1(),
                         cfAccountId);

        String name = runner.getName();
        assertEquals("enable-streaming", name);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket and no origin access id exists. Both should be created.
     * The distribution created should be open.
     */
    @Test
    public void testPerformTask1Secure() throws Exception {
        performTask1(true);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket and no origin access id exists. Both should be created.
     * The distribution created should be open.
     */
    @Test
    public void testPerformTask1Open() throws Exception {
        performTask1(false);
    }

    private void performTask1(boolean secure) throws Exception {
        EnableStreamingTaskRunner runner =
            createRunner(createMockStorageProviderV2(false),
                         createMockUnwrappedS3StorageProviderV2(),
                         createMockS3ClientV3(),
                         createMockCFClientV4(),
                         cfAccountId);

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        taskParams.setSecure(secure);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        testResults(results);
        testCapturedProps(secure);
        testCreateRequestCapture(secure);
    }


    /*
     * For testing the case where a distribution and origin access id do not
     * exist and are created.
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - returns empty list
     * listCloudFrontOriginAccessIdentities (1) - returns empty list
     * createCloudFrontOriginAccessIdentity (1) - return access id
     * createStreamingDistribution (1) - returns distribution with domain name
     * getCloudFrontOriginAccessIdentity (1) - returns OAIdentity
     */
    private AmazonCloudFrontClient createMockCFClientV4() throws Exception {
        cfClient = EasyMock.createMock(AmazonCloudFrontClient.class);

        ListStreamingDistributionsResult result =
            new ListStreamingDistributionsResult()
                .withStreamingDistributionList(
                    new StreamingDistributionList()
                        .withItems(new ArrayList()));
        EasyMock
            .expect(cfClient.listStreamingDistributions(
                EasyMock.isA(ListStreamingDistributionsRequest.class)))
            .andReturn(result)
            .times(1);

        List<CloudFrontOriginAccessIdentitySummary> oaiSummaryList = new ArrayList<>();
        CloudFrontOriginAccessIdentityList oaiList =
            new CloudFrontOriginAccessIdentityList().withItems(oaiSummaryList);
        ListCloudFrontOriginAccessIdentitiesResult listOaiResult =
            new ListCloudFrontOriginAccessIdentitiesResult()
                .withCloudFrontOriginAccessIdentityList(oaiList);
        EasyMock
            .expect(cfClient.listCloudFrontOriginAccessIdentities(
                EasyMock.isA(ListCloudFrontOriginAccessIdentitiesRequest.class)))
            .andReturn(listOaiResult)
            .times(1);

        String oaIdentity = "origin-access-identity";
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

        StreamingDistribution dist =
            new StreamingDistribution().withDomainName(domainName);
        CreateStreamingDistributionResult createDistResult =
            new CreateStreamingDistributionResult()
                .withStreamingDistribution(dist);
        createDistRequestCapture = new Capture<>();
        EasyMock
            .expect(cfClient.createStreamingDistribution(
                EasyMock.capture(createDistRequestCapture)))
            .andReturn(createDistResult)
            .times(1);

        GetCloudFrontOriginAccessIdentityResult getOaiResult =
            new GetCloudFrontOriginAccessIdentityResult()
                .withCloudFrontOriginAccessIdentity(oai);
        EasyMock
            .expect(cfClient.getCloudFrontOriginAccessIdentity(
            EasyMock.isA(GetCloudFrontOriginAccessIdentityRequest.class)))
            .andReturn(getOaiResult)
            .times(1);

        EasyMock.replay(cfClient);
        return cfClient;
    }

    protected AmazonS3Client createMockS3ClientV3() throws Exception {
        AmazonS3Client service = EasyMock.createMock(AmazonS3Client.class);

        service.setBucketPolicy(EasyMock.isA(String.class),
                                EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        EasyMock.replay(service);
        return service;
    }

    private void testResults(String results) {
        EnableStreamingTaskResult taskResult =
            EnableStreamingTaskResult.deserialize(results);
        assertEquals(taskResult.getStreamingHost(), domainName);
        assertTrue(taskResult.getResult().contains("completed"));
    }

    private void testCapturedProps(boolean secure) {
        Map<String, String> spaceProps = spacePropsCapture.getValue();
        String streamHostPropName = EnableStreamingTaskRunner.STREAMING_HOST_PROP;
        assertEquals(spaceProps.get(streamHostPropName), domainName);

        String streamTypePropName = EnableStreamingTaskRunner.STREAMING_TYPE_PROP;
        if(secure) {
            assertEquals(spaceProps.get(streamTypePropName),
                         EnableStreamingTaskRunner.STREAMING_TYPE.SECURE.name());
        } else {
            assertEquals(spaceProps.get(streamTypePropName),
                         EnableStreamingTaskRunner.STREAMING_TYPE.OPEN.name());
        }
    }

    private void testCreateRequestCapture(boolean secure) {
        StreamingDistributionConfig distConfig =
            createDistRequestCapture.getValue().getStreamingDistributionConfig();
        assertNotNull(distConfig.getCallerReference());
        assertTrue(distConfig.getS3Origin()
                             .getDomainName().startsWith(bucketName));
        assertTrue(distConfig.isEnabled());
        assertNotNull(distConfig.getComment());
        if(secure) {
            assertTrue(distConfig.getTrustedSigners().isEnabled());
            assertEquals(new Integer(1), distConfig.getTrustedSigners().getQuantity());
        } else {
            assertFalse(distConfig.getTrustedSigners().isEnabled());
            assertEquals(new Integer(0), distConfig.getTrustedSigners().getQuantity());
        }
    }

    /*
     * Testing the case where a streaming distribution exists for the given
     * bucket and an origin access id exists. Nothing should be created.
     */
    @Test
    public void testPerformTask2() throws Exception {
        EnableStreamingTaskRunner runner =
            createRunner(createMockStorageProviderV2(false),
                         createMockUnwrappedS3StorageProviderV2(),
                         createMockS3ClientV3(),
                         createMockCFClientV2(),
                         cfAccountId);

        EnableStreamingTaskParameters taskParams = new EnableStreamingTaskParameters();
        taskParams.setSpaceId(spaceId);
        boolean secure = false;
        taskParams.setSecure(secure);

        String results = runner.performTask(taskParams.serialize());
        assertNotNull(results);
        testResults(results);
        testCapturedProps(secure);
    }

    /*
     * For testing the case where a distribution and origin access identity
     * already exist and are used as is.
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - returns a list with a valid dist (matching bucket name)
     * getOriginAccessIdentity (1) - returns valid oaid
     */
    protected AmazonCloudFrontClient createMockCFClientV2() throws Exception {
        cfClient = EasyMock.createMock(AmazonCloudFrontClient.class);

        cfClientExpectValidDistribution(cfClient);

        String oaIdentity = "origin-access-identity";

        CloudFrontOriginAccessIdentitySummary oaiSummary =
            new CloudFrontOriginAccessIdentitySummary()
                .withId(oaIdentity).withS3CanonicalUserId(oaIdentity + "-canonical");
        List<CloudFrontOriginAccessIdentitySummary> oaiSummaryList = new ArrayList<>();
        oaiSummaryList.add(oaiSummary);
        CloudFrontOriginAccessIdentityList oaiList =
            new CloudFrontOriginAccessIdentityList().withItems(oaiSummaryList);
        ListCloudFrontOriginAccessIdentitiesResult listOaiResult =
            new ListCloudFrontOriginAccessIdentitiesResult()
                .withCloudFrontOriginAccessIdentityList(oaiList);
        EasyMock
            .expect(cfClient.listCloudFrontOriginAccessIdentities(
                EasyMock.isA(ListCloudFrontOriginAccessIdentitiesRequest.class)))
            .andReturn(listOaiResult)
            .times(1);

        CloudFrontOriginAccessIdentity oai =
            new CloudFrontOriginAccessIdentity().withId(oaIdentity);
        GetCloudFrontOriginAccessIdentityResult getOaiResult =
            new GetCloudFrontOriginAccessIdentityResult()
                .withCloudFrontOriginAccessIdentity(oai);
        EasyMock
            .expect(cfClient.getCloudFrontOriginAccessIdentity(
                EasyMock.isA(GetCloudFrontOriginAccessIdentityRequest.class)))
            .andReturn(getOaiResult)
            .times(1);

        EasyMock.replay(cfClient);
        return cfClient;
    }

}
