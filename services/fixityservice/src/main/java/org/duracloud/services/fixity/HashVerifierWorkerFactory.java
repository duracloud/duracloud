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
import org.duracloud.services.fixity.worker.ServiceWorkerFactory;

import java.io.File;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashVerifierWorkerFactory implements ServiceWorkerFactory<ContentLocationPair> {
    public HashVerifierWorkerFactory(FixityServiceOptions serviceOptions,
                                     ContentStore contentStore,
                                     File workDir) {
    }

    @Override
    public Runnable newWorker(ContentLocationPair workItem) {
        // Default method body
        return null;
    }
}
