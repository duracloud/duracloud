/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import com.amazonaws.services.cloudfront.AmazonCloudFrontClient;
import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.s3storage.S3StorageProvider;
import org.duracloud.s3storage.StringDataStoreFactory;
import org.duracloud.s3task.storage.SetStoragePolicyTaskRunner;
import org.duracloud.s3task.streaming.DeleteStreamingTaskRunner;
import org.duracloud.s3task.streaming.DisableStreamingTaskRunner;
import org.duracloud.s3task.streaming.EnableStreamingTaskRunner;
import org.duracloud.s3task.streaming.GetSignedUrlTaskRunner;
import org.duracloud.s3task.streaming.GetUrlTaskRunner;
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
                          AmazonS3Client s3Client,
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

        // RTMP Streaming
        taskList.add(new EnableStreamingTaskRunner(s3Provider,
                                                   unwrappedS3Provider,
                                                   s3Client,
                                                   cfClient,
                                                   cfAccountId));
        taskList.add(new GetUrlTaskRunner(s3Provider,
                                          unwrappedS3Provider,
                                          cfClient));
        taskList.add(new GetSignedUrlTaskRunner(s3Provider,
                                                unwrappedS3Provider,
                                                cfClient,
                                                cfKeyId,
                                                cfKeyPath));
        taskList.add(new DisableStreamingTaskRunner(s3Provider,
                                                    unwrappedS3Provider,
                                                    s3Client,
                                                    cfClient));
        taskList.add(new DeleteStreamingTaskRunner(s3Provider,
                                                   unwrappedS3Provider,
                                                   s3Client,
                                                   cfClient));

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
