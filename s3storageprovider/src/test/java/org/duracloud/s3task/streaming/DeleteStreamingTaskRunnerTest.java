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
import org.easymock.EasyMock;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.model.cloudfront.S3Origin;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.junit.Test;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class DeleteStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    protected DeleteStreamingTaskRunner createRunner(StorageProvider s3Provider,
                                                     S3StorageProvider unwrappedS3Provider,
                                                     AmazonS3Client s3Client,
                                                     CloudFrontService cfService) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfService = cfService;
        return new DeleteStreamingTaskRunner(s3Provider, unwrappedS3Provider,
                                             s3Client, cfService);
    }

    @Test
    public void testGetName() throws Exception {
        DeleteStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFServiceV1());

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
                         createMockCFServiceV3());

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        try {
            runner.performTask(spaceId);
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
     * For testing the case where a distribution does not exist.
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - returns null
     */
    private CloudFrontService createMockCFServiceV3() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);

        EasyMock
            .expect(service.listStreamingDistributions())
            .andReturn(null)
            .times(1);

        EasyMock.replay(service);
        return service;
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
                         createMockCFServiceV4());

        String results = runner.performTask(spaceId);
        assertNotNull(results);
        testCapturedProps();
    }

    /*
     * For testing the case where a distribution exists and will be deleted
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - return dist with matching bucket name, enabled
     * disableStreamingDistributionForDeletion (1) - void return
     * getStreamingDistributionInfo (1) - return valid info, deployed
     * deleteStreamingDistribution (1) - void return
     */
    private CloudFrontService createMockCFServiceV4() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);

        S3Origin origin = new S3Origin(bucketName);
        StreamingDistribution dist =
            new StreamingDistribution("id", "status", null, domainName,
                                      origin, null, "comment", true);
        StreamingDistribution[] distributions = {dist};

        EasyMock
            .expect(service.listStreamingDistributions())
            .andReturn(distributions)
            .times(1);

        service.disableStreamingDistributionForDeletion(
            EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);

        S3Origin origin2 = new S3Origin("origin");
        StreamingDistribution info =
            new StreamingDistribution("id", "Deployed", null, domainName,
                                      origin2, null, "comment", false);

        EasyMock
            .expect(service.getStreamingDistributionInfo(
                EasyMock.isA(String.class)))
            .andReturn(info)
            .times(1);

        service.deleteStreamingDistribution(EasyMock.isA(String.class));
        EasyMock.expectLastCall().times(1);        

        EasyMock.replay(service);
        return service;
    }

}
