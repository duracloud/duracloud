/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.source;

import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.easymock.IAnswer;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: Oct 15, 2010
 */
public class DuraStoreRetrievalSourceTest {

    private ContentStore store;
    private String checksum;
    private String value;

    @Before
    public void setUp() throws Exception {
        checksum = "a1b2";
        value = "value";
        store = createMockContentStore();        
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(store);
    }

    @Test
    public void testGetNextItem() throws Exception {
        List<String> spaces = new ArrayList<String>();
        spaces.add("space1");
        spaces.add("space2");

        // Spaces list
        RetrievalSource source =
            new DuraStoreRetrievalSource(store, spaces, false);
        int count;
        for(count = 0; source.getNextContentItem() != null; count++) {}
        assertEquals(6, count);

        // All Spaces
        source = new DuraStoreRetrievalSource(store, null, true);
        for(count = 0; source.getNextContentItem() != null; count++) {}
        assertEquals(9, count);
    }

    @Test
    public void testGetSourceChecksum() throws Exception {
        RetrievalSource source =
            new DuraStoreRetrievalSource(store, null, true);

        ContentItem contentItem = new ContentItem("spaceId", "contentId");
        String sourceChecksum = source.getSourceChecksum(contentItem);
        assertNotNull(sourceChecksum);
        assertEquals(checksum, sourceChecksum);
    }

    @Test
    public void testGetSourceContent() throws Exception {
        RetrievalSource source =
            new DuraStoreRetrievalSource(store, null, true);

        ContentItem contentItem = new ContentItem("spaceId", "contentId");
        ContentStream contentStream = source.getSourceContent(contentItem);
        assertNotNull(contentStream);
        assertEquals(checksum, contentStream.getChecksum());

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        IOUtils.copy(contentStream.getStream(), outStream);
        assertEquals(value, outStream.toString("UTF-8"));
    }

    private ContentStore createMockContentStore() throws Exception {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        EasyMock
            .expect(contentStore.getSpaces())
            .andAnswer(new GetSpacesAnswer())
            .anyTimes();

        EasyMock
            .expect(contentStore.getSpaceContents(EasyMock.isA(String.class)))
            .andAnswer(new GetSpaceContentsAnswer())
            .anyTimes();

        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ContentStore.CONTENT_CHECKSUM, checksum);

        EasyMock
            .expect(
                contentStore.getContentProperties(EasyMock.isA(String.class),
                                                  EasyMock.isA(String.class)))
            .andReturn(properties)
            .anyTimes();

        Content content = new Content();
        content.setId("1");
        content.setProperties(properties);
        content.setStream(new ByteArrayInputStream(value.getBytes("UTF-8")));

        EasyMock
            .expect(contentStore.getContent(EasyMock.isA(String.class),
                                            EasyMock.isA(String.class)))
            .andReturn(content)
            .anyTimes();

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private class GetSpacesAnswer implements IAnswer {
        @Override
        public List<String> answer() throws Exception {
            List<String> spaces = new ArrayList<String>();
            spaces.add("space1");
            spaces.add("space2");
            spaces.add("space3");

            // Default method body
            return spaces;
        }
    }

    private class GetSpaceContentsAnswer implements IAnswer {
        @Override
        public Iterator<String> answer() throws Exception {
            List<String> contents = new ArrayList<String>();
            contents.add("content1");
            contents.add("content2");
            contents.add("content3");

            // Default method body
            return contents.iterator();
        }
    }
}
