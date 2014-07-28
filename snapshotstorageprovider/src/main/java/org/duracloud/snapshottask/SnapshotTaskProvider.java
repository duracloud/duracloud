/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.snapshottask.snapshot.CompleteSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.CreateSnapshotTaskRunner;
import org.duracloud.snapshottask.snapshot.GetRestoreStatusTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotContentsTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotStatusTaskRunner;
import org.duracloud.snapshottask.snapshot.GetSnapshotsTaskRunner;
import org.duracloud.snapshottask.snapshot.RestoreSnapshotTaskRunner;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: 1/29/14
 */
public class SnapshotTaskProvider extends TaskProviderBase {

    public SnapshotTaskProvider(SnapshotStorageProvider snapshotProvider,
                                AmazonS3Client s3Client,
                                String dcHost,
                                String dcPort,
                                String dcStoreId,
                                String dcAccountName,
                                String dcSnapshotUser,
                                String bridgeHost,
                                String bridgePort,
                                String bridgeUser,
                                String bridgePass) {
        log = LoggerFactory.getLogger(SnapshotTaskProvider.class);

        taskList.add(new CreateSnapshotTaskRunner(snapshotProvider,
                                                  dcHost,
                                                  dcPort,
                                                  dcStoreId,
                                                  dcAccountName,
                                                  dcSnapshotUser,
                                                  bridgeHost,
                                                  bridgePort,
                                                  bridgeUser,
                                                  bridgePass));
        taskList.add(new GetSnapshotStatusTaskRunner());
        taskList.add(new CompleteSnapshotTaskRunner(snapshotProvider, s3Client));
        taskList.add(new GetSnapshotsTaskRunner());
        taskList.add(new GetSnapshotContentsTaskRunner());
        taskList.add(new RestoreSnapshotTaskRunner());
        taskList.add(new GetRestoreStatusTaskRunner());
    }

}
