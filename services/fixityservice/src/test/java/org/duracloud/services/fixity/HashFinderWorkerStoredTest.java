/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.HashFinderResult;
import org.duracloud.services.fixity.results.ServiceResult;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach.STORED;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;

/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class HashFinderWorkerStoredTest extends HashFinderWorkerTestBase {

    private FixityServiceOptions.Mode mode = Mode.ALL_IN_ONE_LIST;
    private FixityServiceOptions.HashApproach hashApproach = STORED;
    
    private String hash = "stored-hash";
    private final ServiceResult expectedResult = createExpectedResult();

    private ServiceResult createExpectedResult() {
        return new HashFinderResult(true,
                                providedListingSpaceIdA,
                                providedListingContentIdA,
                                hash);
    }

    private void doInitialize() throws ContentStoreException {
        super.initialize(mode, hashApproach);
        worker = new HashFinderWorker(serviceOptions,
                                      contentStore,
                                      workItemLocation,
                                      resultListener);
    }

    @Test
    public void testRun() throws Exception {
        doInitialize();
        worker.run();
    }

    @Test
    public void testRunBAD() throws Exception {
        hash = "bad-hash";
        doInitialize();

        boolean exceptionThrown = false;
        try {
            worker.run();
        } catch (AssertionError e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("There should be an exception.", exceptionThrown);
    }

    @Override
    protected ContentStore createContentStore() throws ContentStoreException {
        Map<String, String> properties = getProperties(hash);
        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getContentProperties(providedListingSpaceIdA,
                                                   providedListingContentIdA))
            .andReturn(properties);
        EasyMock.replay(store);

        return store;
    }

    @Override
    protected ServiceResultListener createResultListener() {
        ServiceResultListener listener = EasyMock.createMock(
            "ServiceResultListener",
            ServiceResultListener.class);
        listener.processServiceResult(HashWorkerMockSupport.eqResult(expectedResult));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(listener);
        return listener;
    }

}
