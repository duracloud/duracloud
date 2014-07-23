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
 * Begins the process of restoring a snapshot by creating a landing space and
 * informing the snapshot bridge application that a restore action needs to be
 * performed.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class RestoreSnapshotTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "restore-snapshot";

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // Verify that a restore of the requested snapshot is not already
        // available

        // Create a space in which the contents of the snapshot will be place
        // during the restore process

        // Call to brige to request restore

        throw new NotImplementedException();
    }

}
