/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.jets3t.service.CloudFrontService;
import org.junit.After;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class StreamingTaskRunnerTestBase {

    protected StorageProvider s3Provider;
    protected S3StorageProvider unwrappedS3Provider;
    protected AmazonS3Client s3Client;
    protected CloudFrontService cfService;

    protected String spaceId = "space-id";
    protected String bucketName = "bucket-name";
    protected String domainName = "domain-name";
    protected Capture<Map<String, String>> spacePropsCapture;

    @After
    public void tearDown() {
        EasyMock.verify(s3Provider, unwrappedS3Provider, s3Client, cfService);
        s3Provider = null;
        unwrappedS3Provider = null;
        s3Client = null;
        cfService = null;
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

    protected CloudFrontService createMockCFServiceV1() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);
        EasyMock.replay(service);
        return service;
    }

}
