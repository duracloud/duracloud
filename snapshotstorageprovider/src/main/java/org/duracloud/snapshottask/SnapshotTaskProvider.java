/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask;

import org.duracloud.common.queue.TaskQueue;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.snapshottask.snapshot.CleanupSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.CompleteCancelSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.CompleteRestoreTaskRunner;
import org.duracloud.snapshottask.snapshot.CompleteSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.CreateSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.GetRestoreTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotContentsTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotHistoryTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotsTaskRunner;
import org.duracloud.snapshottask.snapshot.RequestRestoreSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.RestartSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.RestoreSnapshotTaskRunner;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3Client;

/**
 * @author: Bill Branan
 * Date: 1/29/14
 */
public class SnapshotTaskProvider extends TaskProviderBase {

    public SnapshotTaskProvider(StorageProvider snapshotProvider,
                                SnapshotStorageProvider unwrappedSnapshotProvider,
                                AmazonS3Client s3Client,
                                String dcHost,
                                String dcPort,
                                String dcStoreId,
                                String dcAccountName,
                                String dcSnapshotUser,
                                String bridgeHost,
                                String bridgePort,
                                String bridgeUser,
                                String bridgePass,
                                String bridgeMemberId,
                                TaskQueue auditQueue,
                                ManifestStore manifestStore) {
        log = LoggerFactory.getLogger(SnapshotTaskProvider.class);

        taskList.add(new CreateSnapshotTaskRunner(snapshotProvider,
                                                  unwrappedSnapshotProvider,
                                                  dcHost,
                                                  dcPort,
                                                  dcStoreId,
                                                  dcAccountName,
                                                  dcSnapshotUser,
                                                  bridgeHost,
                                                  bridgePort,
                                                  bridgeUser,
                                                  bridgePass,
                                                  bridgeMemberId));
        taskList.add(new GetSnapshotTaskRunner(bridgeHost,
                                               bridgePort,
                                               bridgeUser,
                                               bridgePass));
        taskList.add(new CleanupSnapshotTaskRunner(unwrappedSnapshotProvider,
                                                   s3Client,
                                                   auditQueue, 
                                                   manifestStore, 
                                                   dcAccountName,
                                                   dcStoreId));
        taskList.add(new CompleteSnapshotTaskRunner(snapshotProvider,
                                                    unwrappedSnapshotProvider,
                                                    s3Client));
        taskList.add(new GetSnapshotsTaskRunner(dcHost,
                                                dcStoreId,
                                                bridgeHost,
                                                bridgePort,
                                                bridgeUser,
                                                bridgePass,
                                                snapshotProvider));
        taskList.add(new GetSnapshotContentsTaskRunner(bridgeHost,
                                                       bridgePort,
                                                       bridgeUser,
                                                       bridgePass));
        taskList.add(new GetSnapshotHistoryTaskRunner(bridgeHost,
                                                       bridgePort,
                                                       bridgeUser,
                                                       bridgePass));
        taskList.add(new RestartSnapshotTaskRunner(bridgeHost,
                                                   bridgePort,
                                                   bridgeUser,
                                                   bridgePass));
        
        taskList.add(new RestoreSnapshotTaskRunner(snapshotProvider,
                                                   unwrappedSnapshotProvider,
                                                   dcHost,
                                                   dcPort,
                                                   dcStoreId,
                                                   dcSnapshotUser,
                                                   bridgeHost,
                                                   bridgePort,
                                                   bridgeUser,
                                                   bridgePass));
        taskList.add(new RequestRestoreSnapshotTaskRunner(dcHost,
                                                   dcPort,
                                                   dcStoreId,
                                                   dcSnapshotUser,
                                                   bridgeHost,
                                                   bridgePort,
                                                   bridgeUser,
                                                   bridgePass));

        taskList.add(new CompleteRestoreTaskRunner(snapshotProvider,
                                                   unwrappedSnapshotProvider,
                                                   s3Client));
        taskList.add(new GetRestoreTaskRunner(bridgeHost,
                                              bridgePort,
                                              bridgeUser,
                                              bridgePass));

        taskList.add(new CompleteCancelSnapshotTaskRunner(snapshotProvider,
                                                   unwrappedSnapshotProvider,
                                                   dcSnapshotUser,
                                                   bridgeHost,
                                                   bridgePort,
                                                   bridgeUser,
                                                   bridgePass));

    }

}
