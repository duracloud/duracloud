package org.duracloud.durastore.storage.probe;

import org.duracloud.azurestorage.AzureStorageProvider;
import org.duracloud.common.util.metrics.MetricsProbed;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.ProbedStorageProvider;

/**
 * This class implements the StorageProvider interface using a Metrics-Probed
 * Azure BlobClient as the underlying storage service.
 *
 * @author Kristen Cannava
 */
public class ProbedAzureStorageProvider extends ProbedStorageProvider {

    private ProbedAzureBlobClient probedCore;

    public ProbedAzureStorageProvider(String username, String apiAccessKey)
        throws StorageException {
        try {
            probedCore = new ProbedAzureBlobClient(username, apiAccessKey);
        } catch (Exception e) {
            String err = "Could not create connection to Azure due to error: " +
                e.getMessage();
            throw new StorageException(err, e);
        }

        setStorageProvider(new AzureStorageProvider(probedCore));
    }

    @Override
    protected MetricsProbed getProbedCore() {
        return probedCore;
    }
}
