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
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach.GENERATED;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode.GENERATE_SPACE;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashFinderWorkloadFromSpaceTest extends HashFinderWorkloadTestBase {

    private HashFinderWorkload workload;
    private FixityServiceOptions.Mode mode = GENERATE_SPACE;
    private FixityServiceOptions.HashApproach hashApproach = GENERATED;


    private List<String> contentItems;
    private final String contentItemPrefix = "content-item-";
    private final int numContentItems = 5;

    @Before
    public void setUp() throws ContentStoreException {
        contentItems = new ArrayList<String>();
        for (int i = 0; i < numContentItems; ++i) {
            contentItems.add(contentItemPrefix + i);
        }

        super.initialize(mode, hashApproach);
        workload = new HashFinderWorkload(serviceOptions, contentStore);
    }


    @Test
    public void testHasNext() throws Exception {
        for (int i = 0; i < numContentItems; ++i) {
            Assert.assertTrue(i + " of " + numContentItems, workload.hasNext());
            workload.next();
        }
        Assert.assertFalse(workload.hasNext());
    }

    @Test
    public void testNext() throws Exception {
        int i = 0;
        while (workload.hasNext()) {
            ContentLocation location = workload.next();
            Assert.assertNotNull(location);

            Assert.assertEquals(targetSpaceId, location.getSpaceId());
            Assert.assertEquals(contentItemPrefix + i, location.getContentId());
            i++;
        }
        Assert.assertEquals(numContentItems, i);
    }

    @Override
    protected ContentStore createContentStore() throws ContentStoreException {
        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getSpaceContents(targetSpaceId)).andReturn(
            contentItems.iterator());
        EasyMock.replay(store);

        return store;
    }
}
