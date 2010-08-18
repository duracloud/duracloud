/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.duracloud.services.fixity.worker.ServiceWorkerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class HashFinderWorkerFactory implements ServiceWorkerFactory<ContentLocation> {

    private final Logger log = LoggerFactory.getLogger(HashFinderWorkerFactory.class);

    private FixityServiceOptions serviceOptions;
    private ContentStore contentStore;
    private ServiceResultListener resultListener;

    public HashFinderWorkerFactory(FixityServiceOptions serviceOptions,
                                   ContentStore contentStore,
                                   ServiceResultListener resultListener) {
        this.serviceOptions = serviceOptions;
        this.contentStore = contentStore;
        this.resultListener = resultListener;
    }

    @Override
    public Runnable newWorker(ContentLocation workItemLocation) {
        log.debug("new worker: " + workItemLocation);
        return new HashFinderWorker(serviceOptions,
                                    contentStore,
                                    workItemLocation,
                                    resultListener);
    }

}
