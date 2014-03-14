/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.duracloud.storage.aop.ContentMessage;
import org.duracloud.storage.aop.ContentMessageConverter;
import org.duracloud.storage.aop.IngestMessageConverter;
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

public class ContentMessageConverterTest {
    private ContentMessageConverter contentMessageConverter;

    @Before
    public void setUp() throws Exception {
        contentMessageConverter = new ContentMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try{
            Message msg = EasyMock.createMock("Message",
                                              Message.class);

            contentMessageConverter.fromMessage(msg);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try{
            contentMessageConverter.toMessage((Object) "", null);
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
        String username = "username";
        String action = ContentMessage.ACTION.DELETE.name();

        MapMessage msg = EasyMock.createMock("MapMessage",
                                             MapMessage.class);

        msg.getStringProperty(ContentMessageConverter.STORE_ID);
        EasyMock.expectLastCall().andReturn(storeId);

        msg.getString(ContentMessageConverter.SPACE_ID);
        EasyMock.expectLastCall().andReturn(spaceId);

        msg.getString(ContentMessageConverter.CONTENT_ID);
        EasyMock.expectLastCall().andReturn(contentId);

        msg.getString(ContentMessageConverter.USERNAME);
        EasyMock.expectLastCall().andReturn(username);

        msg.getString(ContentMessageConverter.ACTION);
        EasyMock.expectLastCall().andReturn(action);

        EasyMock.replay(msg);
        Object obj = contentMessageConverter.fromMessage(msg);
        EasyMock.verify(msg);

        assertNotNull(obj);
        assertTrue(obj instanceof ContentMessage);

        ContentMessage contentMessage = (ContentMessage) obj;
        assertEquals(storeId, contentMessage.getStoreId());
        assertEquals(spaceId, contentMessage.getSpaceId());
        assertEquals(contentId, contentMessage.getContentId());
        assertEquals(username, contentMessage.getUsername());
        assertEquals(action, contentMessage.getAction());
    }

    @Test
    public void testToMessage() throws JMSException {
        String storeId = "storeId";
        String spaceId = "spaceId";
        String contentId = "contentId";
        String username = "username";
        String action = ContentMessage.ACTION.DELETE.name();

        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setStringProperty(ContentMessageConverter.STORE_ID, storeId);
        EasyMock.expectLastCall().once();

        mapMsg.setStringProperty(IngestMessageConverter.SPACE_ID, spaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentMessageConverter.SPACE_ID, spaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentMessageConverter.CONTENT_ID, contentId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentMessageConverter.USERNAME, username);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentMessageConverter.ACTION, action);
        EasyMock.expectLastCall().once();

        ContentMessage contentMessage = new ContentMessage();
        contentMessage.setStoreId(storeId);
        contentMessage.setSpaceId(spaceId);
        contentMessage.setContentId(contentId);
        contentMessage.setUsername(username);
        contentMessage.setAction(action);

        EasyMock.replay(mapMsg);
        EasyMock.replay(session);
        Message msg = contentMessageConverter.toMessage((Object)contentMessage,
                                                      session);
        EasyMock.verify(mapMsg);
        EasyMock.verify(session);

        assertNotNull(msg);
        assertTrue(msg instanceof MapMessage);
    }
}
