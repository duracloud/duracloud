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
 * Gets the list of content items that are contained in the snapshot. This is
 * the same as the list of content that existed in the original space at the
 * moment the snapshot was initiated.
 *
 * @author Bill Branan
 *         Date: 7/23/14
 */
public class GetSnapshotContentsTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "get-snapshot-contents";

    @Override
    public String getName() {
        return TASK_NAME;
    }

    @Override
    public String performTask(String taskParameters) {
        // Call to bridge for a list of content items with the given offset
        // and maxresults values

        throw new NotImplementedException();
    }

}
