/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.easymock.classextension.EasyMock;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.S3Service;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.cloudfront.OriginAccessIdentity;
import org.jets3t.service.model.cloudfront.StreamingDistribution;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class DisableStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    protected DisableStreamingTaskRunner createRunner(S3Service s3Service,
                                                      CloudFrontService cfService) {
        this.s3Provider = createMockS3StorageProvider();
        this.s3Service = s3Service;
        this.cfService = cfService;
        return new DisableStreamingTaskRunner(s3Provider, s3Service, cfService);
    }

    @Test
    public void testGetName() throws Exception {
        DisableStreamingTaskRunner runner =
            createRunner(createMockS3ServiceV1(), createMockCFServiceV1());

        String name = runner.getName();
        assertEquals("disable-streaming", name);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket. An exception should be thrown.
     */
    @Test
    public void testPerformTask1() throws Exception {
        DisableStreamingTaskRunner runner =
            createRunner(createMockS3ServiceV1(), createMockCFServiceV2());

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        try {
            runner.performTask("spaceId");
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }
    }

    /*
     * For testing the case where a distribution does not exist.
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - returns null
     */
    private CloudFrontService createMockCFServiceV2() throws Exception {
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
     * bucket and will be disabled.
     */
    @Test
    public void testPerformTask2() throws Exception {
        DisableStreamingTaskRunner runner =
            createRunner(createMockS3ServiceV2(), createMockCFServiceV3());

        String results = runner.performTask("spaceId");
        assertNotNull(results);
    }

    private S3Service createMockS3ServiceV2() throws Exception {
        S3Service service = EasyMock.createMock(S3Service.class);

        EasyMock.expect(service.getBucketAcl(EasyMock.isA(String.class)))
            .andReturn(new AccessControlList())
            .times(1);

        service.putObjectAcl(EasyMock.isA(String.class),
                             EasyMock.isA(String.class),
                             EasyMock.isA(AccessControlList.class));
        // Number determined by the number of items returned by the 
        // MockS3Provider.getSpaceContents()
        EasyMock.expectLastCall().times(3);

        EasyMock.replay(service);
        return service;
    }

    /*
     * For testing the case where a distribution exists and will be disabled
     * In short, these are the calls that are expected:
     *
     * listStreamingDistributions (1) - returns a list with a valid dist (matching bucket name)
     */
    private CloudFrontService createMockCFServiceV3() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);

        StreamingDistribution dist =
            new StreamingDistribution("id", "status", null, "domainName",
                                      "bucketName", null, "comment", true);
        StreamingDistribution[] distributions = {dist};

        EasyMock
            .expect(service.listStreamingDistributions())
            .andReturn(distributions)
            .times(1);

        EasyMock.replay(service);
        return service;
    }

}
