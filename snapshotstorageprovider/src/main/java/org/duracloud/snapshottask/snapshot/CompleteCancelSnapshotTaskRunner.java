/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.duracloud.snapshot.SnapshotConstants;
import org.duracloud.snapshot.dto.task.CompleteCancelSnapshotTaskParameters;
import org.duracloud.snapshot.dto.task.CompleteCancelSnapshotTaskResult;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Completes the process of cancelling a snapshot by cleaning
 * up the snapshot properties file, removing readonly access for the 
 * snapshot user, and removing the snapshot space property.
 *
 *
 * @author Daniel Bernstein
 *         Date: 9/22/15
 */
public class CompleteCancelSnapshotTaskRunner extends SpaceModifyingSnapshotTaskRunner {

    private Logger log =
        LoggerFactory.getLogger(CompleteCancelSnapshotTaskRunner.class);

    public CompleteCancelSnapshotTaskRunner(StorageProvider snapshotProvider,
                                            SnapshotStorageProvider unwrappedSnapshotProvider,
                                            String dcSnapshotUser,
                                            String bridgeAppHost,
                                            String bridgeAppPort,
                                            String bridgeAppUser,
                                            String bridgeAppPass) {
        super(snapshotProvider,
              unwrappedSnapshotProvider,
              dcSnapshotUser,
              bridgeAppHost,
              bridgeAppPort,
              bridgeAppUser,
              bridgeAppPass);
    }

    @Override
    public String getName() {
        return SnapshotConstants.COMPLETE_SNAPSHOT_CANCEL_TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        log.info("performing task:  params={}", taskParameters);
        CompleteCancelSnapshotTaskParameters taskParams =
            CompleteCancelSnapshotTaskParameters.deserialize(taskParameters);
        String spaceId = taskParams.getSpaceId();
        
        removeSnapshotIdFromSpaceProps(spaceId);
        removeSnapshotProps(spaceId);
        removeSnapshotUserPermissions(spaceId);
        return new CompleteCancelSnapshotTaskResult("completed cancellation for " + spaceId).serialize();
    }

}
