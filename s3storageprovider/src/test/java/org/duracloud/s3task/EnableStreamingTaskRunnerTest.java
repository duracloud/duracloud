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
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Jun 3, 2010
 */
public class EnableStreamingTaskRunnerTest extends StreamingTaskRunnerTestBase {

    protected EnableStreamingTaskRunner createRunner(S3Service s3Service,
                                                   CloudFrontService cfService) {
        this.s3Provider = createMockS3StorageProvider();
        this.s3Service = s3Service;
        this.cfService = cfService;
        return new EnableStreamingTaskRunner(s3Provider, s3Service, cfService);
    }    

    @Test
    public void testGetName() throws Exception {
        EnableStreamingTaskRunner runner =
            createRunner(createMockS3ServiceV1(), createMockCFServiceV1());

        String name = runner.getName();
        assertEquals("enable-streaming", name);
    }

    /*
     * Testing the case where no streaming distribution exists for the given
     * bucket and no origin access id exists. Both should be created.
     */
    @Test
    public void testPerformTask1() throws Exception {
        EnableStreamingTaskRunner runner =
            createRunner(createMockS3ServiceV2(), createMockCFServiceV2());

        try {
            runner.performTask(null);
            fail("Exception expected");
        } catch(Exception expected) {
            assertNotNull(expected);
        }

        String results = runner.performTask("spaceId");
        assertNotNull(results);
        testResults(results);
    }

    private S3Service createMockS3ServiceV2() throws Exception {
        S3Service service = EasyMock.createMock(S3Service.class);

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
            // Number determined by the number of items returned by the
            // MockS3Provider.getSpaceContents()
            .times(3);

        service.putObjectAcl(EasyMock.isA(String.class),
                             EasyMock.isA(String.class),
                             EasyMock.isA(AccessControlList.class));
        EasyMock.expectLastCall().times(1);

        EasyMock.replay(service);
        return service;
    }

    /*
     * For testing the case where a distribution and origin access id do not
     * exist and are created.
     * In short, these are the calls that are expected:
     *
     * createStreamingDistribution (1) - returns valid dist
     * createOriginAccessIdentity (1) - returns valid oaid
     * getOriginAccessIdentityList (1) - returns null (or empty list)
     * getOriginAccessIdentity (1) - returns valid oaid
     * listStreamingDistributions (1) - returns null
     */
    private CloudFrontService createMockCFServiceV2() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);

        StreamingDistribution dist =
            new StreamingDistribution("id", "status", null, "domainName",
                                      "origin", null, "comment", true);

        EasyMock
            .expect(service.createStreamingDistribution(
                EasyMock.isA(String.class),
                EasyMock.<String>isNull(),
                EasyMock.<String[]>isNull(),
                EasyMock.<String>isNull(),
                EasyMock.eq(true),
                EasyMock.isA(String.class),
                EasyMock.eq(false),
                EasyMock.<String[]>isNull()))
            .andReturn(dist)
            .times(1);

        OriginAccessIdentity oaIdentity =
            new OriginAccessIdentity("id", "s3CanonicalUserId", "comment");

        EasyMock
            .expect(service.createOriginAccessIdentity(
                EasyMock.<String>isNull(),
                EasyMock.isA(String.class)))
            .andReturn(oaIdentity)
            .times(1);

        EasyMock
            .expect(service.getOriginAccessIdentityList())
            .andReturn(null)
            .times(1);

        EasyMock
            .expect(service.getOriginAccessIdentity(EasyMock.isA(String.class)))
            .andReturn(oaIdentity)
            .times(1);

        EasyMock
            .expect(service.listStreamingDistributions())
            .andReturn(null)
            .times(1);

        EasyMock.replay(service);
        return service;
    }

    /*
     * Testing the case where a streaming distribution exists for the given
     * bucket and an origin access id exists. Nothing should be created.
     */
    @Test
    public void testPerformTask2() throws Exception {
        EnableStreamingTaskRunner runner =
            createRunner(createMockS3ServiceV2(), createMockCFServiceV3());

        String results = runner.performTask("spaceId");
        assertNotNull(results);
        testResults(results);
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
    private CloudFrontService createMockCFServiceV3() throws Exception {
        CloudFrontService service =
            EasyMock.createMock(CloudFrontService.class);

        StreamingDistributionConfig config =
            new StreamingDistributionConfig("origin", "callerReference",
                                            new String[0], "comment", true,
                                            "originId", false, null);

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

    private void testResults(String results) {
        Map<String, String> resultMap =
            SerializationUtil.deserializeMap(results);
        assertNotNull(resultMap);
        assertEquals(resultMap.get("domain-name"), "domainName");
        assertTrue(resultMap.get("results").contains("completed"));
    }
    
}
