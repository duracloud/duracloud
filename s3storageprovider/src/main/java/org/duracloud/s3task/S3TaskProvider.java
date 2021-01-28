/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.s3task.storage.SetStoragePolicyTaskRunner;
import org.duracloud.s3task.streaminghls.DeleteHlsTaskRunner;
import org.duracloud.s3task.streaminghls.DisableHlsTaskRunner;
import org.duracloud.s3task.streaminghls.EnableHlsTaskRunner;
import org.duracloud.s3task.streaminghls.GetHlsSignedCookiesUrlTaskRunner;
import org.duracloud.s3task.streaminghls.GetUrlHlsTaskRunner;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

/**
 * Handles tasks specific to content stored in Amazon S3
 *
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3TaskProvider extends TaskProviderBase {

    public S3TaskProvider(StorageProvider s3Provider,
                          S3StorageProvider unwrappedS3Provider,
                          AmazonS3 s3Client,
                          AmazonCloudFrontClient cfClient,
                          StringDataStoreFactory dataStoreFactory,
                          String cfAccountId,
                          String cfKeyId,
                          String cfKeyPath,
                          String storeId,
                          String dcHost) {
        super(storeId);
        log = LoggerFactory.getLogger(S3TaskProvider.class);

        taskList.add(new NoopTaskRunner());

        taskList.add(new SetStoragePolicyTaskRunner(unwrappedS3Provider));

        // HLS Streaming
        taskList.add(new EnableHlsTaskRunner(s3Provider,
                                             unwrappedS3Provider,
                                             s3Client,
                                             cfClient,
                                             cfAccountId,
                                             dcHost));
        taskList.add(new GetUrlHlsTaskRunner(s3Provider,
                                             unwrappedS3Provider,
                                             cfClient));
        taskList.add(new GetHlsSignedCookiesUrlTaskRunner(s3Provider,
                                                          unwrappedS3Provider,
                                                          cfClient,
                                                          dataStoreFactory,
                                                          cfKeyId,
                                                          cfKeyPath));
        taskList.add(new DisableHlsTaskRunner(s3Provider,
                                              unwrappedS3Provider,
                                              s3Client,
                                              cfClient));
        taskList.add(new DeleteHlsTaskRunner(s3Provider,
                                             unwrappedS3Provider,
                                             s3Client,
                                             cfClient));
    }

}
