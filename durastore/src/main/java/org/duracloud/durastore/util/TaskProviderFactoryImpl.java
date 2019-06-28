/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import java.util.Map;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.glaciertask.GlacierTaskProvider;
import org.duracloud.mill.manifest.ManifestStore;
import org.duracloud.s3storage.S3ProviderUtil;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.s3task.S3TaskProvider;
import org.duracloud.snapshotstorage.ChronopolisStorageProvider;
import org.duracloud.snapshotstorage.SnapshotStorageProvider;
import org.duracloud.snapshottask.SnapshotTaskProvider;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.TaskException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProvider;
import org.duracloud.storage.provider.TaskProviderFactory;
import org.duracloud.storage.util.StorageProviderFactory;
import org.duracloud.swifttask.SwiftTaskProvider;

/**
 * Provides access to TaskProvider implementations
 *
 * @author Bill Branan
 * Date: May 20, 2010
 */
public class TaskProviderFactoryImpl extends ProviderFactoryBase
    implements TaskProviderFactory {

    private StorageProviderFactory storageProviderFactory;
    private ManifestStore manifestStore;

    public TaskProviderFactoryImpl(StorageAccountManager storageAccountManager,
                                   StorageProviderFactory storageProviderFactory,
                                   ManifestStore manifestStore) {
        super(storageAccountManager);
        this.storageProviderFactory = storageProviderFactory;
        this.manifestStore = manifestStore;
    }

    @Override
    public TaskProvider getTaskProvider() {
        return getTaskProvider(null);
    }

    @Override
    public TaskProvider getTaskProvider(String storageAccountId)
        throws TaskException {
        StorageAccountManager storageAccountManager = getAccountManager();
        StorageAccount account =
            storageAccountManager.getStorageAccount(storageAccountId);
        if (account == null) {
            account = storageAccountManager.getPrimaryStorageAccount();
            storageAccountId = account.getId();
        }

        String dcHost = storageAccountManager.getInstanceHost();
        String dcPort = storageAccountManager.getInstancePort();
        String dcAccountName = storageAccountManager.getAccountName();

        String username = account.getUsername();
        String password = account.getPassword();
        StorageProviderType type = account.getType();
        StorageProvider storageProvider =
            storageProviderFactory.getStorageProvider(storageAccountId);

        TaskProvider taskProvider;
        if (type.equals(StorageProviderType.AMAZON_S3)) {
            S3StorageProvider unwrappedS3Provider =
                new S3StorageProvider(username, password, account.getOptions());
            AmazonS3 s3Client =
                S3ProviderUtil.getAmazonS3Client(username, password, account.getOptions());
            AmazonCloudFrontClient cfClient =
                S3ProviderUtil.getAmazonCloudFrontClient(username, password);
            StringDataStoreFactory dataStoreFactory =
                new StringDataStoreFactory(storageAccountManager);
            Map<String, String> opts = account.getOptions();
            String cfAccountId =
                opts.get(StorageAccount.OPTS.CF_ACCOUNT_ID.name());
            String cfKeyId =
                opts.get(StorageAccount.OPTS.CF_KEY_ID.name());
            String cfKeyPath =
                opts.get(StorageAccount.OPTS.CF_KEY_PATH.name());
            taskProvider = new S3TaskProvider(storageProvider,
                                              unwrappedS3Provider,
                                              s3Client,
                                              cfClient,
                                              dataStoreFactory,
                                              cfAccountId,
                                              cfKeyId,
                                              cfKeyPath,
                                              storageAccountId,
                                              dcHost);
        } else if (type.equals(StorageProviderType.SWIFT_S3)) {
            taskProvider = new SwiftTaskProvider(storageAccountId);
        } else if (type.equals(StorageProviderType.AMAZON_GLACIER)) {
            GlacierStorageProvider unwrappedGlacierProvider =
                new GlacierStorageProvider(username, password, account.getOptions());
            AmazonS3 s3Client =
                S3ProviderUtil.getAmazonS3Client(username, password, account.getOptions());
            taskProvider = new GlacierTaskProvider(storageProvider,
                                                   unwrappedGlacierProvider,
                                                   s3Client,
                                                   storageAccountId);
        } else if (type.equals(StorageProviderType.CHRONOPOLIS)) {
            SnapshotStorageProvider unwrappedSnapshotProvider =
                new ChronopolisStorageProvider(username, password, account.getOptions());
            AmazonS3 s3Client =
                S3ProviderUtil.getAmazonS3Client(username, password, account.getOptions());

            Map<String, String> opts = account.getOptions();
            String dcSnapshotUser =
                opts.get(StorageAccount.OPTS.SNAPSHOT_USER.name());
            String bridgeHost =
                opts.get(StorageAccount.OPTS.BRIDGE_HOST.name());
            String bridgePort =
                opts.get(StorageAccount.OPTS.BRIDGE_PORT.name());
            String bridgeUser =
                opts.get(StorageAccount.OPTS.BRIDGE_USER.name());
            String bridgePass =
                opts.get(StorageAccount.OPTS.BRIDGE_PASS.name());
            String bridgeMemberId =
                opts.get(StorageAccount.OPTS.BRIDGE_MEMBER_ID.name());

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
                                                    bridgePass,
                                                    bridgeMemberId,
                                                    this.storageProviderFactory.getAuditQueue(),
                                                    this.manifestStore);
        } else {
            throw new TaskException("No TaskProvider is available for " + type);
        }

        return taskProvider;
    }

}