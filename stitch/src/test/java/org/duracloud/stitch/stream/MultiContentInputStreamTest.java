/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.stitch.stream;

import org.apache.commons.io.IOUtils;
import org.duracloud.common.model.ContentItem;
import org.duracloud.domain.Content;
import org.duracloud.stitch.datasource.DataSource;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: 9/9/11
 */
public class MultiContentInputStreamTest {

    private MultiContentInputStream multiStream;

    private DataSource dataSource;
    private List<ContentItem> contentItems;

    private List<InputStream> streams;
    
    private MultiContentInputStreamListener listener;

    @Before
    public void setUp() throws Exception {
        dataSource = EasyMock.createMock("DataSource", DataSource.class);
        listener = EasyMock.createMock("MultiContentInputStreamListener", MultiContentInputStreamListener.class);
        contentItems = new ArrayList<ContentItem>();
        
        streams = new ArrayList<InputStream>();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(dataSource, listener);

        for (InputStream stream : streams) {
            stream.close();
        }
    }

    private void replayMocks() {
        EasyMock.replay(dataSource,listener);
    }

    @Test
    public void testReadWithoutListener() throws Exception {
        String text = createReadMocks();
        replayMocks();

        OutputStream out = new ByteArrayOutputStream();

        multiStream = new MultiContentInputStream(dataSource, contentItems);
        IOUtils.copy(multiStream, out);
        Assert.assertEquals(text, out.toString());
        out.close();
    }
    
    @Test
    public void testReadWithListener() throws Exception {
        String text = createReadMocks();
        contentItems.stream().forEach(x -> {
            listener.contentIdRead(x.getContentId());
            EasyMock.expectLastCall().once();
        });
        replayMocks();

        OutputStream out = new ByteArrayOutputStream();
        multiStream = new MultiContentInputStream(dataSource, contentItems, listener);
        IOUtils.copy(multiStream, out);
        Assert.assertEquals(text, out.toString());
        out.close();
    }

    private String createReadMocks() {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            text.append(createContentExpectation(i));
        }

        return text.toString();
    }

    private String createContentExpectation(int i) {
        String spaceId = "spaceId-" + i;
        String contentId = "contentId-" + i;
        contentItems.add(new ContentItem(spaceId, contentId));

        Content content = new Content();
        String text = "text-" + i;
        content.setStream(getStream(text));

        EasyMock.expect(dataSource.getContent(spaceId, contentId)).andReturn(
            content);

        return text;
    }

    private InputStream getStream(String text) {
        InputStream stream = new ByteArrayInputStream(text.getBytes());
        streams.add(stream);
        return stream;
    }
}
