/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.duracloud.audit.task.AuditTask;
import org.duracloud.audit.task.AuditTask.ActionType;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.task.Task;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;

/**
 * @author Bill Branan
 *         Date: 8/14/14
 */
public class CleanupSnapshotTaskRunnerTest extends EasyMockSupport{

    private StorageProvider snapshotProvider;
    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3Client s3Client;
    private CleanupSnapshotTaskRunner taskRunner;
    private TaskQueue auditQueue;
    private String storeId = "store-id";
    private String account = "account-id";
    private ManifestStore manifestStore;
    @Before
    public void setup() {
        snapshotProvider = createMock("StorageProvider",
                                               StorageProvider.class);
        unwrappedSnapshotProvider =
            createMock("SnapshotStorageProvider",
                                SnapshotStorageProvider.class);
        s3Client = createMock("AmazonS3Client", AmazonS3Client.class);
        manifestStore = createMock("ManifestStore", ManifestStore.class);
        auditQueue = createMock("TaskQueue", TaskQueue.class);
        taskRunner =
            new CleanupSnapshotTaskRunner(unwrappedSnapshotProvider,
                                          s3Client,
                                          auditQueue,
                                          manifestStore,
                                          account,
                                          storeId);
    }


    @After
    public void tearDown(){
        verifyAll();
    }

    @Test
    public void testGetName() {
        replayAll();
        assertEquals("cleanup-snapshot", taskRunner.getName());
    }

    @Test
    public void testPerformTask() throws Exception {
        String spaceId = "space-id";
        String bucketName = "bucket-name";

        expect(unwrappedSnapshotProvider.getBucketName(spaceId))
                .andReturn(bucketName);
        Capture<BucketLifecycleConfiguration> lifecycleConfigCapture =
            new Capture<>();
        s3Client.setBucketLifecycleConfiguration(eq(bucketName),
                                                 capture(
                                                     lifecycleConfigCapture));
        expectLastCall();

        List<ManifestItem> manifestItems = new LinkedList<>();
        for(int i = 0; i < 10; i++){
            ManifestItem item = new ManifestItem();
            item.setContentId("content-id-"+i);
            manifestItems.add(item);
        }
        
        Iterator<ManifestItem> it = manifestItems.iterator();
        
        expect(manifestStore.getItems(account, storeId, spaceId)).andReturn(it);

        Capture<Set<Task>> taskCapture = new Capture<>();
        
        auditQueue.put(capture(taskCapture));
        expectLastCall();
        Authentication auth = createMock(Authentication.class);
        String userId = "user-id";
        expect(auth.getName()).andReturn(userId);
        SecurityContext context = createMock(SecurityContext.class);
        expect(context.getAuthentication()).andReturn(auth);
        
        SecurityContextHolder.setContext(context);
        replayAll();
        taskRunner.performTask("{\"spaceId\":\""+spaceId+"\"}");
        BucketLifecycleConfiguration lifecycleConfig =
            lifecycleConfigCapture.getValue();
        BucketLifecycleConfiguration.Rule rule =
            lifecycleConfig.getRules().get(0);
        assertEquals(1, rule.getExpirationInDays());
        assertEquals("clear-content-rule", rule.getId());
        assertEquals("Enabled", rule.getStatus());
        Thread.sleep(500);

        Set<Task> tasks = taskCapture.getValue();
        assertEquals(manifestItems.size(), tasks.size());
        
        for(Task task : tasks){
            assertNotNull(task.getProperty(AuditTask.DATE_TIME_PROP));
            assertNotNull(task.getProperty(AuditTask.CONTENT_ID_PROP));
            assertEquals(ActionType.DELETE_CONTENT.name(), task.getProperty(AuditTask.ACTION_PROP));
            assertEquals(account, task.getProperty(AuditTask.ACCOUNT_PROP));
            assertEquals(storeId, task.getProperty(AuditTask.STORE_ID_PROP));
            assertEquals(spaceId, task.getProperty(AuditTask.SPACE_ID_PROP));
        }
        
    }

}
