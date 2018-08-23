/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaminghls;

import static org.duracloud.s3task.streaminghls.BaseHlsTaskRunner.S3_ORIGIN_SUFFIX;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.DefaultCacheBehavior;
import com.amazonaws.services.cloudfront.model.DistributionList;
import com.amazonaws.services.cloudfront.model.DistributionSummary;
import com.amazonaws.services.cloudfront.model.ListDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListDistributionsResult;
import com.amazonaws.services.cloudfront.model.Origin;
import com.amazonaws.services.cloudfront.model.Origins;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StringDataStore;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;

/**
 * @author Bill Branan
 * Date: Aug 17, 2018
 */
public class HlsTaskRunnerTestBase {

    protected StorageProvider s3Provider;
    protected S3StorageProvider unwrappedS3Provider;
    protected AmazonS3Client s3Client;
    protected AmazonCloudFrontClient cfClient;
    protected StringDataStoreFactory dataStoreFactory;
    protected StringDataStore dataStore;

    protected String cfKeyId = "cf-key-id";
    protected String cfKeyPath;
    protected String cfAccountId = "test";
    protected String dcHost = "test.duracloud.org";

    protected String spaceId = "space-id";
    protected String contentId = "content-id";
    protected String bucketName = "bucket-name";
    protected String domainName = "domain-name";
    protected String redirectUrl = "redirect-url";

    @Before
    public void setup() {
        s3Provider = EasyMock.createMock(StorageProvider.class);
        unwrappedS3Provider = EasyMock.createMock(S3StorageProvider.class);
        s3Client = EasyMock.createMock(AmazonS3Client.class);
        cfClient = EasyMock.createMock(AmazonCloudFrontClient.class);
        dataStoreFactory = EasyMock.createMock(StringDataStoreFactory.class);
        dataStore = EasyMock.createMock(StringDataStore.class);
    }

    protected void replayMocks() {
        EasyMock.replay(s3Provider, unwrappedS3Provider, s3Client, cfClient, dataStoreFactory, dataStore);
    }

    @After
    public void teardown() {
        EasyMock.verify(s3Provider, unwrappedS3Provider, s3Client, cfClient, dataStoreFactory, dataStore);
    }

    /**
     * Used when expecting a valid open distribution as a result of the
     * listDistributions call.
     */
    protected void cfClientExpectValidDistribution(AmazonCloudFrontClient cfClient) {
        cfClientExpectValidDistribution(cfClient, false);
    }

    /**
     * Used when expecting a valid distribution as a result of the
     * listDistributions call.
     *
     * @param secure defines if the returned distribution is secure or open
     */
    protected void cfClientExpectValidDistribution(AmazonCloudFrontClient cfClient,
                                                   boolean secure) {
        cfClientExpectValidDistribution(cfClient, secure, 1);
    }

    /**
     * Used when expecting a valid distribution as a result of the
     * listDistributions call.
     *
     * @param secure            defines if the returned distribution is secure or open
     * @param listCallsExpected the number of times the call to list distributions will
     *                          be called in order to retrieve the entire list
     */
    protected void cfClientExpectValidDistribution(AmazonCloudFrontClient cfClient,
                                                   boolean secure,
                                                   int listCallsExpected) {
        Origin s3Origin = new Origin().withDomainName(bucketName + S3_ORIGIN_SUFFIX);
        DistributionSummary distSummary = new DistributionSummary()
            .withId("id").withStatus("status").withDomainName(domainName)
            .withEnabled(true).withOrigins(new Origins().withItems(s3Origin));
        TrustedSigners trustedSigners = new TrustedSigners().withQuantity(0);
        if (secure) {
            trustedSigners = new TrustedSigners().withQuantity(1).withItems("trusted-signer-item");
        }

        DefaultCacheBehavior defaultCacheBehavior = new DefaultCacheBehavior();
        defaultCacheBehavior.setTrustedSigners(trustedSigners);
        distSummary.setDefaultCacheBehavior(defaultCacheBehavior);

        for (int i = 0; i < listCallsExpected; i++) {
            boolean truncated = false;
            if ((listCallsExpected - i) > 1) {
                truncated = true;
            }

            List<DistributionSummary> distSummaries = new ArrayList();
            distSummaries.add(distSummary);
            ListDistributionsResult distSummaryResult =
                new ListDistributionsResult().withDistributionList(new DistributionList()
                                                                       .withItems(distSummaries)
                                                                       .withIsTruncated(truncated)
                                                                       .withNextMarker("marker"));
            EasyMock.expect(cfClient.listDistributions(EasyMock.isA(ListDistributionsRequest.class)))
                    .andReturn(distSummaryResult);
        }
    }
}
