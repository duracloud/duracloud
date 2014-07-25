/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask.snapshot;

import org.apache.commons.lang.NotImplementedException;
import org.duracloud.storage.provider.TaskRunner;

/**
 * Completes the snapshot process by cleaning up content that is no longer
 * needed now that the snapshot has been transferred successfully.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class CompleteSnapshotTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "complete-snapshot";

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // Set bucket deletion policy

        throw new NotImplementedException();
    }

}
