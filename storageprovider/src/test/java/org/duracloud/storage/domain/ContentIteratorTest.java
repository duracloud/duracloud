/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.storage.provider.mock.MockStorageProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Bill Branan
 * Date: Dec 22, 2009
 */
public class ContentIteratorTest {

    @Test
    public void testIterator() throws Exception {
        for(int i=0; i<31; i++) {
            StorageProvider testProvider = new MockProvider(i);
            ContentIterator iterator =
                new ContentIterator(testProvider, "spaceId", "prefix", 10);
            int count = 0;
            while(iterator.hasNext()) {
                assertNotNull(iterator.next());
                count++;            
            }
            assertEquals(i, count);
        }
    }

    private class MockProvider extends MockStorageProvider {

        private long contentItems;

        public MockProvider(long contentItems) {
            this.contentItems = contentItems;
        }

        @Override
        public List<String> getSpaceContentsChunked(String spaceId,
                                                    String prefix,
                                                    long maxResults,
                                                    String marker)
            throws StorageException {

            long listSize;
            if(contentItems > maxResults) {
                listSize = maxResults;
                contentItems -= maxResults;
            } else {
                listSize = contentItems;
                contentItems = 0;
            }

            List<String> contentList = new ArrayList<String>();
            for(long i=0; i < listSize; i++) {
                contentList.add("test" + i);
            }
            return contentList;
        }
    }


}
