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
 * Get a listing of snapshots which are accessible to this account.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetSnapshotsTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "get-snapshots";

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // Call to bridge for listing of snapshots

        throw new NotImplementedException();
    }

}
