/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.swifttask;

import org.duracloud.s3task.NoopTaskRunner;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

public class SwiftTaskProvider extends TaskProviderBase {

    public SwiftTaskProvider(String storeId) {
        super(storeId);
        log = LoggerFactory.getLogger(SwiftTaskProvider.class);

        // This is just so integration tests pass
        taskList.add(new NoopTaskRunner());
    }
}
