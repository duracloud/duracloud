/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task.storage;

import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storageprovider.dto.SetStoragePolicyTaskParameters;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Bill Branan
 *         Date: 9/25/2015
 */
public class SetStoragePolicyTaskRunnerTest {

    protected S3StorageProvider unwrappedS3Provider;

    @Before
    public void setUp() throws Exception {
        unwrappedS3Provider = EasyMock.createMock(S3StorageProvider.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(unwrappedS3Provider);
    }

    private void replayAll() {
        EasyMock.replay(unwrappedS3Provider);
    }

    @Test
    public void testGetName() {
        SetStoragePolicyTaskRunner taskRunner =
            new SetStoragePolicyTaskRunner(unwrappedS3Provider);

        replayAll();

        assertEquals("set-storage-policy", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        String spaceId = "space-id";
        String storageClass = "STANDARD_IA";
        int days = 42;

        SetStoragePolicyTaskParameters params = new SetStoragePolicyTaskParameters();
        params.setSpaceId(spaceId);
        params.setStorageClass(storageClass);
        params.setDaysToTransition(days);

        SetStoragePolicyTaskRunner taskRunner =
            new SetStoragePolicyTaskRunner(unwrappedS3Provider);

        EasyMock.expect(unwrappedS3Provider.getBucketName(spaceId)).andReturn(spaceId);
        unwrappedS3Provider.setSpaceLifecycle(EasyMock.eq(spaceId),
                                              EasyMock.isA(BucketLifecycleConfiguration.class));
        EasyMock.expectLastCall();

        replayAll();

        taskRunner.performTask(params.serialize());
    }

}
