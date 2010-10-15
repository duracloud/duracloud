/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.duracloud.retrieval.RetrievalTestBase;
import org.duracloud.retrieval.source.ContentItem;
import org.duracloud.retrieval.source.ContentStream;
import org.duracloud.retrieval.source.RetrievalSource;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * @author: Bill Branan
 * Date: Oct 14, 2010
 */
public class RetrievalManagerTest extends RetrievalTestBase {

    private String spaceId = "spaceId";

    @Test
    public void testRetrievalManager() throws Exception {
        int numTestFiles = 5;
        OutputWriter outWriter = createMockOutputWriter();
        RetrievalSource source = new MockRetrievalSource(numTestFiles);

        RetrievalManager retManager =
            new RetrievalManager(source, tempDir, tempDir, false, 1, outWriter);

        retManager.run();

        File spaceDir = new File(tempDir, spaceId);
        assertTrue(spaceDir.exists());
        assertEquals(numTestFiles, spaceDir.listFiles().length);

        EasyMock.verify(outWriter);
    }

    private class MockRetrievalSource implements RetrievalSource {

        private int items;

        public MockRetrievalSource(int items) {
            this.items = items;
        }

        @Override
        public ContentItem getNextContentItem() {
            if(items > 0) {
                ContentItem item =
                    new ContentItem(spaceId, "content-" + items);
                items--;
                return item;
            } else {
                return null;
            }
        }

        @Override
        public String getSourceChecksum(ContentItem contentItem) {
            return String.valueOf(items);
        }

        @Override
        public ContentStream getSourceContent(ContentItem contentItem) {
            String value = String.valueOf(items);
            InputStream stream = new ByteArrayInputStream(value.getBytes());
            return new ContentStream(stream, value);
        }
    }
    
}
