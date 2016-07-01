/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.storage.probe;

import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.rackspacestorage.RackspaceStorageProvider;
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
    private static final String authUrl= "https://auth.api.rackspacecloud.com/v1.0";
    private ProbedSwiftClient probedCore;

    public ProbedRackspaceStorageProvider(String username, String apiAccessKey)
            throws StorageException {
        String trimmedAuthUrl = // JClouds expects authURL with no version
                authUrl.substring(0, authUrl.lastIndexOf("/"));
        probedCore = new ProbedSwiftClient(username, apiAccessKey, trimmedAuthUrl);
        setStorageProvider(new RackspaceStorageProvider(probedCore));
    }

    @Override
    protected MetricsProbed getProbedCore() {
        return probedCore;
    }
}
