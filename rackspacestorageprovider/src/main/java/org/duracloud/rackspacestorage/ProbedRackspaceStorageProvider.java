/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.ProbedStorageProvider;

/**
 * This class implements the StorageProvider interface using a Metrics-Probed
 * Rackspace FilesClient as the underlying storage service.
 *
 * @author Andrew Woods
 */
public class ProbedRackspaceStorageProvider
        extends ProbedStorageProvider {

    private ProbedFilesClient probedCore;

    public ProbedRackspaceStorageProvider(String username, String apiAccessKey)
            throws StorageException {
        try {
            probedCore = new ProbedFilesClient(username, apiAccessKey);
            if (!probedCore.login()) {
                throw new Exception("Login to Rackspace failed");
            }
        } catch (Exception e) {
            String err =
                    "Could not create connection to Rackspace due to error: "
                            + e.getMessage();
            throw new StorageException(err, e);
        }

        setStorageProvider(new RackspaceStorageProvider(probedCore));
    }

    @Override
    protected MetricsProbed getProbedCore() {
        return probedCore;
    }
}
