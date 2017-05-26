/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.streaming;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 *         date: 5/26/2017
 */
public class BaseStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    protected BaseStreamingTaskRunner createRunner(StorageProvider s3Provider,
                                                   S3StorageProvider unwrappedS3Provider,
                                                   AmazonS3Client s3Client,
                                                   AmazonCloudFrontClient cfClient) {
        this.s3Provider = s3Provider;
        this.unwrappedS3Provider = unwrappedS3Provider;
        this.s3Client = s3Client;
        this.cfClient = cfClient;
        return new GetUrlTaskRunner(s3Provider, unwrappedS3Provider, cfClient);
    }

    /**
     * Tests the call to get existing distributions when the total number
     * of distributions is over the max that can be returned in a single call.
     */
    @Test
    public void testGetAllExistingDistributionsAboveMax() throws Exception {
        BaseStreamingTaskRunner runner =
            createRunner(createMockStorageProvider(),
                         createMockUnwrappedS3StorageProvider(),
                         createMockS3ClientV1(),
                         createMockCFClientV5(false, 3));

        runner.getAllExistingDistributions("bucketName");
    }



}
