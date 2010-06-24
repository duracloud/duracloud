/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class TestDuraStoreSyncEndpoint extends DuraStoreSyncTestBase {

    @Test
    public void testDuraStoreSyncEndpoint() throws Exception {
        DuraStoreSyncEndpoint endpoint =
            new DuraStoreSyncEndpoint(host,
                                      Integer.parseInt(port),
                                      context,
                                      getRootCredential().getUsername(),
                                      getRootCredential().getPassword(),
                                      spaceId,
                                      true);
        testSync(endpoint);

        endpoint =
            new DuraStoreSyncEndpoint(host,
                                      Integer.parseInt(port),
                                      context,
                                      getRootCredential().getUsername(),
                                      getRootCredential().getPassword(),
                                      spaceId,
                                      false);
        testSyncNoDeletes(endpoint);
    }
    
}
