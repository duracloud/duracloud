/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshottask;

import org.duracloud.snapshottask.snapshot.SnapshotTaskRunner;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: 1/29/14
 */
public class SnapshotTaskProvider extends TaskProviderBase {

    public SnapshotTaskProvider(StorageProvider snapshotProvider,
                                String dcHost,
                                String dcPort,
                                String dcStoreId,
                                String dcAccountName,
                                String dcSnapshotUser,
                                String bridgeHost,
                                String bridgePort,
                                String bridgeUser,
                                String bridgePass) {
        log = LoggerFactory.getLogger(SnapshotTaskProvider.class);

        taskList.add(new SnapshotTaskRunner(snapshotProvider,
                                            dcHost,
                                            dcPort,
                                            dcStoreId,
                                            dcAccountName,
                                            dcSnapshotUser,
                                            bridgeHost,
                                            bridgePort,
                                            bridgeUser,
                                            bridgePass));
    }

}
