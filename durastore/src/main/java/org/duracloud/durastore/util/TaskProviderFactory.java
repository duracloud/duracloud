/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.glaciertask.GlacierTaskProvider;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3task.S3TaskProvider;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.snapshottask.SnapshotTaskProvider;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProvider;
import org.duracloud.storage.util.StorageProviderFactory;
import org.jets3t.service.CloudFrontService;

/**
 * Provides access to TaskProvider implementations
 *
 * @author Bill Branan
 * Date: May 20, 2010
 */
public class TaskProviderFactory extends ProviderFactoryBase {

    private StorageProviderFactory storageProviderFactory;

    public TaskProviderFactory(StorageAccountManager storageAccountManager,
                               StorageProviderFactory storageProviderFactory) {
        super(storageAccountManager);
        this.storageProviderFactory = storageProviderFactory;
    }

    /**
     * Retrieves the primary task provider for a given customer.
     *
     * @return
     * @throws org.duracloud.storage.error.StorageException
     */
    public TaskProvider getTaskProvider() {
        return getTaskProvider(null);
    }

    /**
     * Retrieves a particular task provider based on the storage account ID.
     *
     * @param storageAccountId - the ID of the provider account
     * @return
     */
    public TaskProvider getTaskProvider(String storageAccountId)
            throws TaskException {
        StorageAccountManager storageAccountManager = getAccountManager();
        StorageAccount account =
            storageAccountManager.getStorageAccount(storageAccountId);
        if (account == null) {
            account = storageAccountManager.getPrimaryStorageAccount();
            storageAccountId = account.getId();
        }
        String username = account.getUsername();
        String password = account.getPassword();
        StorageProviderType type = account.getType();
        StorageProvider storageProvider =
            storageProviderFactory.getStorageProvider(storageAccountId);

        TaskProvider taskProvider;
        if (type.equals(StorageProviderType.AMAZON_S3)) {
            S3StorageProvider unwrappedS3Provider =
                new S3StorageProvider(username, password);
            AmazonS3Client s3Client =
                S3ProviderUtil.getAmazonS3Client(username, password);
            CloudFrontService cfService =
                S3ProviderUtil.getCloudFrontService(username, password);
            taskProvider = new S3TaskProvider(storageProvider,
                                              unwrappedS3Provider,
                                              s3Client,
                                              cfService);
        } else if (type.equals(StorageProviderType.AMAZON_GLACIER)) {
            GlacierStorageProvider unwrappedGlacierProvider =
                new GlacierStorageProvider(username, password);
            AmazonS3Client s3Client =
                S3ProviderUtil.getAmazonS3Client(username, password);
            taskProvider = new GlacierTaskProvider(storageProvider,
                                                   unwrappedGlacierProvider,
                                                   s3Client);
        } else if (type.equals(StorageProviderType.SNAPSHOT)) {
            SnapshotStorageProvider unwrappedSnapshotProvider =
                new SnapshotStorageProvider(username, password);
            AmazonS3Client s3Client =
                S3ProviderUtil.getAmazonS3Client(username, password);

            String dcHost = storageAccountManager.getInstanceHost();
            String dcPort = storageAccountManager.getInstancePort();
            String dcAccountName = storageAccountManager.getAccountName();
            String dcSnapshotUser =
                account.getOptions().get(StorageAccount.OPTS.SNAPSHOT_USER.name());
            String bridgeHost =
                account.getOptions().get(StorageAccount.OPTS.BRIDGE_HOST.name());
            String bridgePort =
                account.getOptions().get(StorageAccount.OPTS.BRIDGE_PORT.name());
            String bridgeUser =
                account.getOptions().get(StorageAccount.OPTS.BRIDGE_USER.name());
            String bridgePass =
                account.getOptions().get(StorageAccount.OPTS.BRIDGE_PASS.name());

            taskProvider = new SnapshotTaskProvider(storageProvider,
                                                    unwrappedSnapshotProvider,
                                                    s3Client,
                                                    dcHost,
                                                    dcPort,
                                                    storageAccountId,
                                                    dcAccountName,
                                                    dcSnapshotUser,
                                                    bridgeHost,
                                                    bridgePort,
                                                    bridgeUser,
                                                    bridgePass);
        } else {
            throw new TaskException("No TaskProvider is available for " + type);
        }

        return taskProvider;
    }

}