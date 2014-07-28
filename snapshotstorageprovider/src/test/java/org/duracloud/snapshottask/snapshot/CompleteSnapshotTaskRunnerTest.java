/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.easymock.Mock;
import org.easymock.TestSubject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * @author Bill Branan
 *         Date: 7/25/14
 */
@RunWith(EasyMockRunner.class)
public class CompleteSnapshotTaskRunnerTest extends EasyMockSupport {

    @Mock
    private SnapshotStorageProvider snapshotProvider;

    @Mock
    private AmazonS3Client s3Client;

    @TestSubject
    private CompleteSnapshotTaskRunner taskRunner =
        new CompleteSnapshotTaskRunner(snapshotProvider, s3Client);

    @After
    public void tearDown(){
        verifyAll();
    }

    @Test
    public void testGetName() {
        replayAll();
        assertEquals("complete-snapshot", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        String spaceId = "space-id";
        String bucketName = "bucket-name";

        EasyMock.expect(snapshotProvider.getBucketName(spaceId))
                .andReturn(bucketName);
        Capture<BucketLifecycleConfiguration> lifecycleConfigCapture =
            new Capture<>();
        s3Client.setBucketLifecycleConfiguration(EasyMock.eq(bucketName),
                                                 EasyMock.capture(
                                                     lifecycleConfigCapture));
        EasyMock.expectLastCall();

        replayAll();

        taskRunner.performTask("{\"spaceId\":\""+spaceId+"\"}");
        BucketLifecycleConfiguration lifecycleConfig =
            lifecycleConfigCapture.getValue();
        BucketLifecycleConfiguration.Rule rule =
            lifecycleConfig.getRules().get(0);
        assertEquals(1, rule.getExpirationInDays());
        assertEquals("clear-content-rule", rule.getId());
        assertEquals("Enabled", rule.getStatus());
    }

}
