/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chrontask;

import org.duracloud.chronstorage.ChronStageStorageProvider;
import org.duracloud.storage.provider.TaskRunner;

/**
 * @author: Bill Branan
 *          Date: 2/1/13
 */
public class SnapshotTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "snapshot";

    private ChronStageStorageProvider chronProvider;

    public SnapshotTaskRunner(ChronStageStorageProvider chronProvider) {
        this.chronProvider = chronProvider;
    }

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // TODO: Parse spaceId and snapshot properties from taskParams

        // TODO: Set space to read-only

        // TODO: Make call to DPN bridge ingest app to kick off transfer

        return "Snapshot action not yet implemented";
    }

}
