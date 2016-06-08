/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.duracloud.audit.task.AuditTask;
import org.duracloud.audit.task.AuditTask.ActionType;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.queue.TaskQueue;
import org.duracloud.common.queue.task.Task;
import org.duracloud.common.retry.Retriable;
import org.duracloud.common.retry.Retrier;
import org.duracloud.mill.db.model.ManifestItem;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.CleanupSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.CleanupSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.TaskRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;

/**
 * Cleans up the snapshot by removing content that is no longer
 * needed now that the snapshot has been transferred successfully.
 *
 *
 * @author Bill Branan
 *         Date: 8/14/14
 */
public class CleanupSnapshotTaskRunner implements TaskRunner {

    private Logger log =
        LoggerFactory.getLogger(CleanupSnapshotTaskRunner.class);

    private static int EXPIRATION_DAYS = 1;

    private SnapshotStorageProvider unwrappedSnapshotProvider;
    private AmazonS3Client s3Client;
    private TaskQueue auditTaskQueue;
    private ManifestStore manifestStore;
    private String account;
    private String storeId;
    
    public CleanupSnapshotTaskRunner(SnapshotStorageProvider unwrappedSnapshotProvider,
                                     AmazonS3Client s3Client, 
                                     TaskQueue auditTaskQueue,
                                     ManifestStore manifestStore, 
                                     String account,
                                     String storeId) {
        this.unwrappedSnapshotProvider = unwrappedSnapshotProvider;
        this.s3Client = s3Client;
        this.auditTaskQueue = auditTaskQueue;
        this.manifestStore = manifestStore;
        this.storeId = storeId;
        this.account = account;
    }

    @Override
    public String getName() {
        return SnapshotConstants.CLEANUP_SNAPSHOT_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        CleanupSnapshotTaskParameters taskParams =
            CleanupSnapshotTaskParameters.deserialize(taskParameters);
        final String spaceId = taskParams.getSpaceId();
        final String userId =
            SecurityContextHolder.getContext().getAuthentication().getName();

        String bucketName = unwrappedSnapshotProvider.getBucketName(spaceId);

        log.info("Performing Cleanup Snapshot Task for spaceID: " + spaceId);

        // Create bucket deletion policy
        BucketLifecycleConfiguration.Rule expireRule =
            new BucketLifecycleConfiguration.Rule()
                .withId("clear-content-rule")
                .withExpirationInDays(EXPIRATION_DAYS)
                .withStatus(BucketLifecycleConfiguration.ENABLED.toString());

        List<BucketLifecycleConfiguration.Rule> rules = new ArrayList<>();
        rules.add(expireRule);

        BucketLifecycleConfiguration configuration =
            new BucketLifecycleConfiguration().withRules(rules);

        // Set policy on bucket
        s3Client.setBucketLifecycleConfiguration(bucketName, configuration);

        queueContentDeleteAuditTasks(spaceId,userId);

       log.info("Cleanup Snapshot Task for space " + spaceId +
                 " completed successfully");

        return new CleanupSnapshotTaskResult(EXPIRATION_DAYS).serialize();
    }

    protected void queueContentDeleteAuditTasks(final String spaceId, final String userId) {

        final String storeType = unwrappedSnapshotProvider.getStorageProviderType().name();

        new Thread(new Runnable() {
            public void run() {

                try {
                    final File tmpFile = File.createTempFile(account+"-"+storeId + "-" + spaceId, ".tsv");
                    
                    tmpFile.deleteOnExit();
                    final long time = System.currentTimeMillis();
                    new Retrier(4, 60*1000, 2).execute(new Retriable() {
                        public Object retry() throws Exception {
                            //create delete audit messages for each item in the space.
                            Iterator<ManifestItem> items = manifestStore.getItems(account, storeId, spaceId);
                            try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpFile)))){
                                while(items.hasNext()){
                                    ManifestItem item = items.next();
                                    String line = marshalItem(item);
                                    writer.write(line+"\n");
                                }
                                
                                writer.close();
                                    
                            }catch(Exception ex){
                                throw new DuraCloudRuntimeException(ex);
                            }
                            long count = 0;
                            Set<Task> tasks = new HashSet<Task>();

                            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(tmpFile)))){
                                String line = null;
                                while((line = reader.readLine()) != null){
                                    String[] values  = unmarshalItem(line);
                                    AuditTask task = new AuditTask();
                                    task.setAccount(account);
                                    task.setSpaceId(spaceId);
                                    task.setStoreId(storeId);
                                    task.setDateTime(String.valueOf(time));
                                    task.setContentId(values[0]);
                                    task.setContentSize(values[1]);
                                    task.setStoreType(storeType);
                                    task.setContentChecksum(values[2]);
                                    task.setAction(ActionType.DELETE_CONTENT.name());
                                    task.setUserId(userId);
                                    tasks.add(task.writeTask());
    
                                    if(tasks.size() >= 10){
                                        auditTaskQueue.put(tasks);
                                        tasks = new HashSet<>();
                                    }
                                    
                                    count++;
                                }
                                
                                if(tasks.size() > 0){
                                    auditTaskQueue.put(tasks);
                                }
                                log.info("Added {} delete audit tasks.", count);
                            }catch(Exception ex){
                                throw new DuraCloudRuntimeException(ex);
                            }

                            return null;
                        }
                        
                        
                    });
                    
                    tmpFile.delete();

                } catch (Exception e) {
                    String message = "Failed to complete queue of deletion audit tasks for " + 
                                     spaceId + " : message =" + e.getMessage();
                    log.error(message, e);
                }
            }
        }, "snapshot-cleanup-" + spaceId).start();
    }
    
    private String marshalItem(ManifestItem item){
        return item.getContentId() + "\t" + item.getContentSize() + "\t"  + item.getContentChecksum();
    }
    
    private String[] unmarshalItem(String line){
        return line.split("\t");
    }
}
