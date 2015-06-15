/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.junit.After;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.cloudfront.model.ListStreamingDistributionsRequest;
import com.amazonaws.services.cloudfront.model.ListStreamingDistributionsResult;
import com.amazonaws.services.cloudfront.model.S3Origin;
import com.amazonaws.services.cloudfront.model.StreamingDistributionList;
import com.amazonaws.services.cloudfront.model.StreamingDistributionSummary;
import com.amazonaws.services.cloudfront.model.TrustedSigners;
import com.amazonaws.services.s3.AmazonS3Client;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class StreamingTaskRunnerTestBase {

    protected StorageProvider s3Provider;
    protected S3StorageProvider unwrappedS3Provider;
    protected AmazonS3Client s3Client;
    protected AmazonCloudFrontClient cfClient;

    protected String cfAccountId = "cf-account-id";
    protected String cfKeyId = "cf-key-id";
    protected String cfKeyPath = "cf-key-path";

    protected String spaceId = "space-id";
    protected String bucketName = "bucket-name";
    protected String domainName = "domain-name";
    protected Capture<Map<String, String>> spacePropsCapture;

    @After
    public void tearDown() {
        EasyMock.verify(s3Provider, unwrappedS3Provider, s3Client, cfClient);
        s3Provider = null;
        unwrappedS3Provider = null;
        s3Client = null;
        cfClient = null;
    }

    protected S3StorageProvider createMockUnwrappedS3StorageProvider() {
        S3StorageProvider provider =
            EasyMock.createMock(S3StorageProvider.class);

        EasyMock
            .expect(provider.getBucketName(EasyMock.isA(String.class)))
            .andReturn(bucketName)
            .anyTimes();

        EasyMock.replay(provider);
        return provider;
    }

    protected S3StorageProvider createMockUnwrappedS3StorageProviderV2() {
        S3StorageProvider provider =
            EasyMock.createMock(S3StorageProvider.class);

        EasyMock
            .expect(provider.getBucketName(EasyMock.isA(String.class)))
            .andReturn(bucketName)
            .anyTimes();

        spacePropsCapture = new Capture<>();
        provider.setNewSpaceProperties(EasyMock.eq(spaceId),
                                       EasyMock.capture(spacePropsCapture));
        EasyMock.expectLastCall();

        EasyMock.replay(provider);
        return provider;
    }

    protected StorageProvider createMockStorageProvider() {
        return createMockStorageProvider(true);
    }
    protected StorageProvider createMockStorageProvider(boolean contentIdExists) {
       Map<String,String> props = new HashMap<>();
       props.put(StorageProvider.PROPERTIES_STREAMING_TYPE, "any-streaming-type");
       return createMockStorageProvider(props, contentIdExists);
    }

    protected StorageProvider createMockStorageProvider(Map<String,String> spaceProps, boolean contentIdExists) {
        StorageProvider provider = EasyMock.createMock(StorageProvider.class);

        List<String> contents = new ArrayList<>();
        contents.add("item1");
        contents.add("item2");
        contents.add("item3");        

        EasyMock
            .expect(provider.getSpaceContents(EasyMock.isA(String.class),
                                              EasyMock.<String>isNull()))
            .andReturn(contents.iterator())
            .anyTimes();
        
        EasyMock.expect(provider.getSpaceProperties(EasyMock.isA(String.class)))
                .andReturn(spaceProps)
                .anyTimes();
        
        IExpectationSetters<Map<String, String>> expection =
            EasyMock.expect(provider.getContentProperties(EasyMock.isA(String.class),
                                                          EasyMock.isA(String.class)));

        if(contentIdExists){
            expection.andReturn(new HashMap<String,String>());
        }else{
            expection.andThrow(new NotFoundException(""));
        }
        
        expection.anyTimes();
        
        EasyMock.replay(provider);
        return provider;
    }

    /**
     * Calls expected:
     * getBucketName () - returns the S3 bucket name
     * getSpaceProperties () - returns the set of space properties
     * setNewSpaceProperties () - set the space props with a streaming host
     */
    protected StorageProvider createMockStorageProviderV2(
        boolean includeStreamingProp) {
        StorageProvider provider =
            EasyMock.createMock(StorageProvider.class);

        Map<String, String> props = new HashMap<>();
        if(includeStreamingProp) {
            props.put(BaseStreamingTaskRunner.STREAMING_HOST_PROP, domainName);
        }
        EasyMock.expect(provider.getSpaceProperties(spaceId))
                .andReturn(props);

        EasyMock.replay(provider);
        return provider;
    }

    protected AmazonS3Client createMockS3ClientV1() throws Exception {
        AmazonS3Client service = EasyMock.createMock(AmazonS3Client.class);
        EasyMock.replay(service);
        return service;
    }

    protected AmazonS3Client createMockS3ClientV3() throws Exception {
        AmazonS3Client service = EasyMock.createMock(AmazonS3Client.class);

        service.deleteBucketPolicy(EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        EasyMock.replay(service);
        return service;
    }

    protected AmazonCloudFrontClient createMockCFClientV1() throws Exception {
        AmazonCloudFrontClient cfClient =
            EasyMock.createMock(AmazonCloudFrontClient.class);
        EasyMock.replay(cfClient);
        return cfClient;
    }

    /*
     * For testing the case where a distribution does not exist.
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - returns empty result set
     */
    protected AmazonCloudFrontClient createMockCFClientV3() throws Exception {
        AmazonCloudFrontClient cfClient =
            EasyMock.createMock(AmazonCloudFrontClient.class);

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

        EasyMock.replay(cfClient);
        return cfClient;
    }

    /**
     * Used when expecting a valid distribution as a result of the
     * listStreamingDistributions call.
     */
    protected void cfClientExpectValidDistribution(AmazonCloudFrontClient cfClient) {
        cfClientExpectValidDistribution(cfClient, false);
    }

    /**
     * Used when expecting a valid distribution as a result of the
     * listStreamingDistributions call.
     * @param secure defines if the returned distribution is secure or open
     */
    protected void cfClientExpectValidDistribution(AmazonCloudFrontClient cfClient,
                                                   boolean secure) {
        S3Origin origin = new S3Origin().withDomainName(
            bucketName + DeleteStreamingTaskRunner.S3_ORIGIN_SUFFIX);
        StreamingDistributionSummary distSummary =
            new StreamingDistributionSummary()
                .withId("id").withStatus("status").withDomainName(domainName)
                .withEnabled(true).withS3Origin(origin);
        TrustedSigners trustedSigners = new TrustedSigners().withQuantity(0);
        if(secure) {
            trustedSigners =
                new TrustedSigners().withQuantity(1).withItems("trusted-signer-item");
        }
        distSummary.setTrustedSigners(trustedSigners);

        List<StreamingDistributionSummary> distSummaries = new ArrayList();
        distSummaries.add(distSummary);
        ListStreamingDistributionsResult distSummaryResult =
            new ListStreamingDistributionsResult()
                .withStreamingDistributionList(
                    new StreamingDistributionList()
                        .withItems(distSummaries));
        EasyMock
            .expect(cfClient.listStreamingDistributions(
                EasyMock.isA(ListStreamingDistributionsRequest.class)))
            .andReturn(distSummaryResult)
            .times(1);
    }

}
