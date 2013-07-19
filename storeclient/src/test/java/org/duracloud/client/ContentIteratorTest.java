/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: Dec 23, 2009
 */
public class ContentIteratorTest {

    @Test
    public void testIterator() throws Exception {
        for (int i = 0; i < 30; i++) {
            ContentStore testStore = new MockStore(i);
            long maxResults = 10;
            ContentIterator iterator =
                new ContentIterator(testStore, "spaceId", "prefix", maxResults);

            int count = 0;
            while (iterator.hasNext()) {
                assertNotNull(iterator.next());
                Assert.assertTrue("count=" + count + ", i=" + i, count <= i);
                count++;
            }
            Assert.assertEquals(i, count);
        }
    }

    private class MockStore extends ContentStoreImpl {

        private List<String> contentItems;

        public MockStore(long numItems) {
            super(null, null, null, null);
            this.contentItems = new ArrayList<String>();
            for (int i = 0; i < numItems; ++i) {
                this.contentItems.add("test" + i);
            }
        }

        @Override
        public Space getSpace(String spaceId,
                              String prefix,
                              long maxResults,
                              String marker) throws ContentStoreException {
            // Throw in the occasional exception to test retry capability
            if(System.currentTimeMillis() % 9 == 0) {
                throw new ContentStoreException("Expected randomized error");
            }

            List<String> items = new ArrayList<>();

            int index = marker == null ? 0 : contentItems.indexOf(marker) + 1;
            int limit = (int) Math.min(index + maxResults, contentItems.size());

            for (int i = index; i < limit; ++i) {
                items.add(contentItems.get(i));
            }

            Space space = new Space();
            space.setId(spaceId);
            space.setContentIds(items);
            return space;
        }
    }


}