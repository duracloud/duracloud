/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.duracloud.services.fixity.results.HashFinderResult;
import org.duracloud.services.fixity.results.ServiceResult;
import org.duracloud.services.fixity.results.ServiceResultListener;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach.GENERATED;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode;


/**
 * @author Andrew Woods
 *         Date: Aug 5, 2010
 */
public class HashFinderWorkerGeneratedTest extends HashFinderWorkerTestBase {

    private FixityServiceOptions.Mode mode = Mode.ALL_IN_ONE_LIST;
    private FixityServiceOptions.HashApproach hashApproach = GENERATED;

    private String text = "...and bingo was his name 'o";
    private final ServiceResult expectedResult = createExpectedResult();

    private ServiceResult createExpectedResult() {
        return new HashFinderResult(true,
                                    providedListingSpaceIdA,
                                    providedListingContentIdA,
                                    getHash(text));
    }

    private void doInitialize(ContentLocation contentLocation)
        throws ContentStoreException {
        super.initialize(mode, hashApproach);

        if (null == contentLocation) {
            contentLocation = workItemLocation;
        }

        worker = new HashFinderWorker(serviceOptions,
                                      contentStore,
                                      contentLocation,
                                      resultListener);
    }

    private void doInitialize() throws ContentStoreException {
        this.doInitialize(null);
    }

    @Test
    public void testRun() throws Exception {
        // This test has one retry on content retrieval.
        doInitialize();
        worker.run();
    }

    @Test
    public void testRunBAD() throws Exception {
        text = "bad-text";
        doInitialize();

        boolean exceptionThrown = false;
        try {
            worker.run();
        } catch (AssertionError e) {
            exceptionThrown = true;
        }
        Assert.assertTrue("There should be an exception.", exceptionThrown);
    }

    @Test
    public void testRunBADLine() throws ContentStoreException {
        ContentLocation contentLocation = new ContentLocation(null,null);
        doInitialize(contentLocation);

        worker.run();
    }

    @Override
    protected ContentStore createContentStore() throws ContentStoreException {
        InputStream inputStream = getInputStream(text);
        InputStream badStream = getInputStream("bad-text");
        Content content = EasyMock.createMock("Content", Content.class);
        EasyMock.expect(content.getStream()).andReturn(badStream);
        EasyMock.expect(content.getStream()).andReturn(inputStream);
        EasyMock.replay(content);

        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getContent(providedListingSpaceIdA,
                                         providedListingContentIdA)).andReturn(
            content).times(2);

        EasyMock.expect(store.getContentProperties(providedListingSpaceIdA,
                                                   providedListingContentIdA))
            .andReturn(getProperties(getHash(text)));

        EasyMock.replay(store);

        return store;
    }

    @Override
    protected ServiceResultListener createResultListener() {
        ServiceResultListener listener = EasyMock.createMock(
            "ServiceResultListener",
            ServiceResultListener.class);
        listener.processServiceResult(HashWorkerMockSupport.eqResult(
            expectedResult));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(listener);
        return listener;
    }

}
