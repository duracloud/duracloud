/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.glaciertask;

import com.amazonaws.services.s3.AmazonS3Client;
import org.duracloud.glacierstorage.GlacierStorageProvider;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: 2/1/13
 */
public class GlacierTaskProvider extends TaskProviderBase {

    public GlacierTaskProvider(StorageProvider glacierProvider,
                               GlacierStorageProvider unwrappedGlacierProvider,
                               AmazonS3Client s3Client) {
        log = LoggerFactory.getLogger(GlacierTaskProvider.class);

        taskList.add(new RestoreContentTaskRunner(glacierProvider,
                                                  unwrappedGlacierProvider,
                                                  s3Client));
    }

}
