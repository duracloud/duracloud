/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.emcstorage;

import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.ProbedStorageProvider;

/**
 * This class implements the StorageProvider interface using a Metrics-Probed
 * EMC-EsuApi as the underlying storage service.
 *
 * @author Andrew Woods
 */
public class ProbedEMCStorageProvider
        extends ProbedStorageProvider {

    private final ProbedEsuApi probedCore;

    public ProbedEMCStorageProvider(String uid, String sharedSecret)
            throws StorageException {
        probedCore = new ProbedEsuApi(uid, sharedSecret);
        storageProvider = new EMCStorageProvider(probedCore);
    }

    @Override
    protected MetricsProbed getProbedCore() {
        return probedCore;
    }

}
