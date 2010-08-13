/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.services.fixity.domain.ContentLocationPair;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.worker.ServiceWorkerFactory;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashVerifierWorkerFactory implements ServiceWorkerFactory<ContentLocationPair> {

    private ContentStore contentStore;
    private File workDir;
    private ServiceResultListener resultListener;

    public HashVerifierWorkerFactory(ContentStore contentStore,
                                     File workDir,
                                     ServiceResultListener resultListener) {
        this.contentStore = contentStore;
        this.workDir = workDir;
        this.resultListener = resultListener;
    }

    @Override
    public Runnable newWorker(ContentLocationPair workItem) {
        return new HashVerifierWorker(contentStore,
                                      workItem,
                                      workDir,
                                      resultListener);
    }
}
