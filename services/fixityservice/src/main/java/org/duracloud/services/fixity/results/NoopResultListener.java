/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity.results;

import org.duracloud.client.ContentStore;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class NoopResultListener implements ServiceResultListener {

    @Override
    public void processServiceResult(ServiceResult result) {
        throw new UnsupportedOperationException("called with: " + result);
    }

    @Override
    public void setTotalWorkitems(long total) {
        // do nothing
    }

    @Override
    public void setProcessingComplete() {
        // do nothing.
    }

    @Override
    public String getProcessingStatus() {
        return "no-status";
    }
}
