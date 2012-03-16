/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.support.converter.MessageConversionException;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class IngestMessageConverterTest {
    private IngestMessageConverter ingestMessageConverter;

    @Before
    public void setUp() throws Exception {
        ingestMessageConverter = new IngestMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try{
            Message msg = EasyMock.createMock("Message",
                                              Message.class);

            ingestMessageConverter.fromMessage(msg);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try{
            ingestMessageConverter.toMessage((Object) "", null);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromMessage() throws JMSException {
        String storeId = "storeId";
        String spaceId = "spaceId";
        String contentId = "contentId";
        String mimeType = "mimeType";
        String username = "username";

        MapMessage msg = EasyMock.createMock("MapMessage",
                                             MapMessage.class);

        msg.getStringProperty(IngestMessageConverter.STORE_ID);
        EasyMock.expectLastCall().andReturn(storeId);

        msg.getString(IngestMessageConverter.SPACE_ID);
        EasyMock.expectLastCall().andReturn(spaceId);

        msg.getString(IngestMessageConverter.CONTENT_ID);
        EasyMock.expectLastCall().andReturn(contentId);

        msg.getString(IngestMessageConverter.MIMETYPE);
        EasyMock.expectLastCall().andReturn(mimeType);

        msg.getString(IngestMessageConverter.USERNAME);
        EasyMock.expectLastCall().andReturn(username);

        EasyMock.replay(msg);
        Object obj = ingestMessageConverter.fromMessage(msg);
        EasyMock.verify(msg);

        assertNotNull(obj);
        assertTrue(obj instanceof IngestMessage);

        IngestMessage ingestMessage = (IngestMessage) obj;
        assertEquals(storeId, ingestMessage.getStoreId());
        assertEquals(spaceId, ingestMessage.getSpaceId());
        assertEquals(contentId, ingestMessage.getContentId());
        assertEquals(mimeType, ingestMessage.getContentMimeType());
        assertEquals(username, ingestMessage.getUsername());
    }

    @Test
    public void testToMessage() throws JMSException {
        String storeId = "storeId";
        String spaceId = "spaceId";
        String contentId = "contentId";
        String mimeType = "mimeType";
        String username = "username";

        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setStringProperty(IngestMessageConverter.STORE_ID, storeId);
        EasyMock.expectLastCall().once();

        mapMsg.setStringProperty(IngestMessageConverter.SPACE_ID, spaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(IngestMessageConverter.SPACE_ID, spaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(IngestMessageConverter.CONTENT_ID, contentId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(IngestMessageConverter.MIMETYPE, mimeType);
        EasyMock.expectLastCall().once();

        mapMsg.setString(IngestMessageConverter.USERNAME, username);
        EasyMock.expectLastCall().once();

        IngestMessage ingestMessage = new IngestMessage();
        ingestMessage.setStoreId(storeId);
        ingestMessage.setSpaceId(spaceId);
        ingestMessage.setContentId(contentId);
        ingestMessage.setContentMimeType(mimeType);
        ingestMessage.setUsername(username);

        EasyMock.replay(mapMsg);
        EasyMock.replay(session);
        Message msg = ingestMessageConverter.toMessage((Object)ingestMessage,
                                                      session);
        EasyMock.verify(mapMsg);
        EasyMock.verify(session);

        assertNotNull(msg);
        assertTrue(msg instanceof MapMessage);
    }
}
