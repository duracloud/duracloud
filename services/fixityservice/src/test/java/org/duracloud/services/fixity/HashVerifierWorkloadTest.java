/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.duracloud.client.ContentStore;
import org.duracloud.common.util.CountListener;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.ContentLocationPair;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Aug 12, 2010
 */
public class HashVerifierWorkloadTest extends HashWorkloadTestBase {

    private HashVerifierWorkload workload;
    private FixityServiceOptions.HashApproach hashApproach = null;

    private final int NUM_ITEMS = 1;

    private void doSetUp(FixityServiceOptions.Mode mode)
        throws ContentStoreException {
        super.initialize(mode, hashApproach);
        workload = new HashVerifierWorkload(serviceOptions);
    }

    @Test
    public void testHasNext() throws Exception {
        doSetUp(FixityServiceOptions.Mode.COMPARE);

        for (int i = 0; i < NUM_ITEMS; ++i) {
            Assert.assertTrue(i + " of " + NUM_ITEMS, workload.hasNext());
            workload.next();
        }
        Assert.assertFalse(workload.hasNext());
    }

    @Test
    public void testNextCompareMode() throws Exception {
        FixityServiceOptions.Mode mode = FixityServiceOptions.Mode.COMPARE;

        doSetUp(mode);
        doTest(mode);
    }

    @Test
    public void testNextSecondStepMode() throws Exception {
        FixityServiceOptions.Mode mode = FixityServiceOptions.Mode.ALL_IN_ONE_LIST;

        doSetUp(mode);
        doTest(mode);
    }

    private void doTest(FixityServiceOptions.Mode mode) {
        int i = 0;
        ContentLocation locA;
        ContentLocation locB;
        while (workload.hasNext()) {
            ContentLocationPair locationPair = workload.next();
            Assert.assertNotNull(locationPair);

            locA = locationPair.getContentLocationA();
            locB = locationPair.getContentLocationB();
            Assert.assertNotNull(locA);
            Assert.assertNotNull(locB);

            Assert.assertEquals(serviceOptions.getProvidedListingSpaceIdA(),
                                locA.getSpaceId());
            Assert.assertEquals(serviceOptions.getProvidedListingContentIdA(),
                                locA.getContentId());

            if (mode.equals(FixityServiceOptions.Mode.COMPARE)) {
                Assert.assertEquals(serviceOptions.getProvidedListingSpaceIdB(),
                                    locB.getSpaceId());
                Assert.assertEquals(serviceOptions.getProvidedListingContentIdB(),
                                    locB.getContentId());
            } else {
                Assert.assertEquals(serviceOptions.getOutputSpaceId(),
                                    locB.getSpaceId());
                Assert.assertEquals(serviceOptions.getOutputContentId(),
                                    locB.getContentId());
            }

            i++;
        }
        Assert.assertEquals(NUM_ITEMS, i);
    }

    @Test
    public void testRegisterCountListener() throws Exception {
        doSetUp(FixityServiceOptions.Mode.ALL_IN_ONE_LIST);

        CountListener countListener = new CountListener() {
            public void setCount(long count) {
            }
        };

        boolean thrown = false;
        try {
            workload.registerCountListener(countListener);
        } catch (Exception e) {
            thrown = true;
        }
        Assert.assertEquals(false, thrown);
    }

    @Override
    protected ContentStore createContentStore() throws ContentStoreException {
        return null;
    }
    
}
