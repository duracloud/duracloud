/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import static junit.framework.Assert.assertNotNull;
import org.duracloud.domain.Space;
import org.duracloud.storage.error.StorageException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Dec 23, 2009
 */
public class ContentIteratorTest {

    @Test
    public void testIterator() throws Exception {
        for(int i=0; i<30; i++) {
            ContentStore testStore = new MockStore(i);
            long maxResults = 10;
            ContentIterator iterator =
                new ContentIterator(testStore, "spaceId", "prefix", maxResults);
            while(iterator.hasNext()) {
                assertNotNull(iterator.next());
            }
        }
    }

    private class MockStore extends ContentStoreImpl {

        private long contentItems;

        public MockStore(long contentItems) {
            super(null, null, null, null);
            this.contentItems = contentItems;
        }

        @Override
        public Space getSpace(String spaceId,
                              String prefix,
                              long maxResults,
                              String marker)
            throws StorageException {

            long listSize;
            if(contentItems > maxResults) {
                listSize = maxResults;
                contentItems -= maxResults;
            } else if(contentItems == maxResults) {
                listSize = 0;
            } else {
                listSize = contentItems;
            }

            List<String> contentList = new ArrayList<String>();
            for(long i=0; i < listSize; i++) {
                contentList.add("test" + i);
            }

            Space space = new Space();
            space.setId(spaceId);
            space.setContentIds(contentList);
            return space;
        }
    }


}