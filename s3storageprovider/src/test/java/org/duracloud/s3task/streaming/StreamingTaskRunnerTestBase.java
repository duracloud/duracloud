/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AccessControlList;
import org.duracloud.s3storage.S3StorageProvider;
import org.easymock.classextension.EasyMock;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.S3Origin;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.junit.After;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class StreamingTaskRunnerTestBase {

    protected S3StorageProvider s3Provider;
    protected AmazonS3Client s3Client;
    protected CloudFrontService cfService;

    @After
    public void tearDown() {
        EasyMock.verify(s3Provider);
        s3Provider = null;

        EasyMock.verify(s3Client);
        s3Client = null;

        EasyMock.verify(cfService);
        cfService = null;
    }

    protected S3StorageProvider createMockS3StorageProvider() {
        S3StorageProvider provider =
            EasyMock.createMock(S3StorageProvider.class);

        EasyMock
            .expect(provider.getBucketName(EasyMock.isA(String.class)))
            .andReturn("bucketName")
            .anyTimes();

        List<String> contents = new ArrayList<String>();
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

    protected AmazonS3Client createMockS3ClientV1() throws Exception {
        AmazonS3Client service = EasyMock.createMock(AmazonS3Client.class);
        EasyMock.replay(service);
        return service;
    }
    
    protected CloudFrontService createMockCFServiceV1() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);
        EasyMock.replay(service);
        return service;
    }

    protected AmazonS3Client createMockS3ClientV2() throws Exception {
        // Number determined by the number of items returned by the
        // MockS3Provider.getSpaceContents()
        int numExpected = 3;
        return createMockS3ClientV2(numExpected);
    }

    protected AmazonS3Client createMockS3ClientV2(int numExpected) throws Exception {
        AmazonS3Client service = EasyMock.createMock(AmazonS3Client.class);

        // Note that EasyMock appears to return the same ACL object
        // each time this method is called, meaning that once a grant
        // is added to the ACL returned from the first call to getObjectAcl
        // all subsequent calls to getObjectAcl also have that grant.
        // That's why putObjectAcl is expected only once, because when the
        // grant exists, that call is skipped.
        EasyMock
            .expect(service.getObjectAcl(EasyMock.isA(String.class),
                                         EasyMock.isA(String.class)))
            .andReturn(new AccessControlList())
            .times(numExpected);

        service.setObjectAcl(EasyMock.isA(String.class),
                             EasyMock.isA(String.class),
                             EasyMock.isA(AccessControlList.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(service);
        return service;
    }

    /*
     * For testing the case where a distribution and origin access identity
     * already exist and are used as is.
     * In short, these are the calls that are expected:
     *
     * getStreamingDistributionConfig (1) - returns valid config (includes oaid, enabled)
     * getOriginAccessIdentity (1) - returns valid oaid
     * listStreamingDistributions (1) - returns a list with a valid dist (matching bucket name)
     */
    protected CloudFrontService createMockCFServiceV2() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);

        S3Origin origin = new S3Origin("origin", "originAccessId");
        StreamingDistributionConfig config =
            new StreamingDistributionConfig(origin, "callerReference",
                                            new String[0], "comment", true,
                                            null, false, null, null);

        EasyMock
            .expect(service.getStreamingDistributionConfig(
                EasyMock.isA(String.class)))
            .andReturn(config)
            .times(1);

        OriginAccessIdentity oaIdentity =
            new OriginAccessIdentity("id", "s3CanonicalUserId", "comment");

        EasyMock
            .expect(service.getOriginAccessIdentity(EasyMock.isA(String.class)))
            .andReturn(oaIdentity)
            .times(1);

        S3Origin origin2 = new S3Origin("bucketName");
        StreamingDistribution dist =
            new StreamingDistribution("id", "status", null, "domainName",
                                      origin2, null, "comment", true);
        StreamingDistribution[] distributions = {dist};

        EasyMock
            .expect(service.listStreamingDistributions())
            .andReturn(distributions)
            .times(1);

        EasyMock.replay(service);
        return service;
    }

}
