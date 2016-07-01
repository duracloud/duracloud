/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.sync.endpoint;

import org.duracloud.integration.sync.SyncIntegrationTestBase;
import org.duracloud.sync.endpoint.DuraStoreSyncEndpoint;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class TestDuraStoreSyncEndpoint extends DuraStoreSyncTestBase {

    //@Test
    public void testDuraStoreSyncEndpoint() throws Exception {
        DuraStoreSyncEndpoint endpoint =
            new DuraStoreSyncEndpoint(getContentStore(),
                                      DuraStoreSyncTestBase.getRootCredential().getUsername(),
                                      SyncIntegrationTestBase.spaceId,
                                      true,
                                      false);
        testSync(endpoint);

        endpoint =
            new DuraStoreSyncEndpoint(getContentStore(),
                                      DuraStoreSyncTestBase.getRootCredential().getUsername(),
                                      SyncIntegrationTestBase.spaceId,
                                      false,
                                      false);
        testSyncNoDeletes(endpoint);
    }
    
}
