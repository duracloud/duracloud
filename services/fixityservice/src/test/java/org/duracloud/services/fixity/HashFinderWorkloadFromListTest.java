/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fixity;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.fixity.domain.ContentLocation;
import org.duracloud.services.fixity.domain.FixityServiceOptions;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.duracloud.services.fixity.domain.FixityServiceOptions.HashApproach.GENERATED;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode.ALL_IN_ONE_LIST;
import static org.duracloud.services.fixity.domain.FixityServiceOptions.Mode.GENERATE_SPACE;

/**
 * @author Andrew Woods
 *         Date: Aug 6, 2010
 */
public class HashFinderWorkloadFromListTest extends HashFinderWorkloadTestBase {

    private HashFinderWorkload workload;
    private FixityServiceOptions.Mode mode = ALL_IN_ONE_LIST;
    private FixityServiceOptions.HashApproach hashApproach = GENERATED;

    private String contentListing;

    private List<String> contentItems;
    private final String contentItemPrefix = "content-item-";
    private final String spacePrefix = "space-prefix-";
    private final int numContentItems = 5;

    @Before
    public void setUp() throws ContentStoreException {
        String sep = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder("header-space,header-content");
        sb.append(sep);
        for (int i = 0; i < numContentItems; ++i) {
            sb.append(spacePrefix + i);
            sb.append(",");
            sb.append(contentItemPrefix + i);
            sb.append(sep);
        }
        contentListing = sb.toString();

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

            Assert.assertEquals(spacePrefix + i, location.getSpaceId());
            Assert.assertEquals(contentItemPrefix + i, location.getContentId());
            i++;
        }
        Assert.assertEquals(numContentItems, i);
    }

    @Override
    protected ContentStore createContentStore() throws ContentStoreException {
        Content content = EasyMock.createMock(Content.class);
        EasyMock.expect(content.getStream()).andReturn(getContentStream(
            contentListing)).anyTimes();
        EasyMock.replay(content);

        ContentStore store = EasyMock.createMock("ContentStore",
                                                 ContentStore.class);
        EasyMock.expect(store.getContent(providedListingSpaceIdA,
                                         providedListingContentIdA)).andReturn(
            content).times(2);
        EasyMock.replay(store);

        return store;
    }

    private InputStream getContentStream(String text) {
        return new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));
    }
}
