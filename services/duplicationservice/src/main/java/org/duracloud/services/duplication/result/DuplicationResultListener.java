/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

import org.duracloud.client.ContentStore;

/**
 * This class listens for duplication events and reports on their success or
 * failure.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public class DuplicationResultListener implements ResultListener {


    public DuplicationResultListener(ContentStore contentStore,
                                     String spaceId,
                                     String reportId,
                                     String workDir) {
        // Default method body
    }

    @Override
    public void processResult(DuplicationResult result) {
        // Default method body
    }
}
