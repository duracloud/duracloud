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

public class ContentCopyMessageConverterTest {
    private ContentCopyMessageConverter contentCopyMessageConverter;

    private String storeId = "storeId";
    private String srcSpaceId = "srcSpaceId";
    private String srcContentId = "srcContentId";
    private String destSpaceId = "destSpaceId";
    private String destContentId = "destContentId";
    private String username = "username";

    @Before
    public void setUp() throws Exception {
        contentCopyMessageConverter = new ContentCopyMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try{
            Message msg = EasyMock.createMock("Message",
                                              Message.class);

            contentCopyMessageConverter.fromMessage(msg);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try{
            contentCopyMessageConverter.toMessage((Object) "", null);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromMessage() throws JMSException {
        MapMessage msg = EasyMock.createMock("MapMessage",
                                             MapMessage.class);

        msg.getStringProperty(ContentCopyMessageConverter.STORE_ID);
        EasyMock.expectLastCall().andReturn(storeId);

        msg.getString(ContentCopyMessageConverter.SRC_SPACE_ID);
        EasyMock.expectLastCall().andReturn(srcSpaceId);

        msg.getString(ContentCopyMessageConverter.SRC_CONTENT_ID);
        EasyMock.expectLastCall().andReturn(srcContentId);

        msg.getString(ContentCopyMessageConverter.DEST_SPACE_ID);
        EasyMock.expectLastCall().andReturn(destSpaceId);

        msg.getString(ContentCopyMessageConverter.DEST_CONTENT_ID);
        EasyMock.expectLastCall().andReturn(destContentId);

        msg.getString(ContentCopyMessageConverter.USERNAME);
        EasyMock.expectLastCall().andReturn(username);

        EasyMock.replay(msg);

        Object obj = contentCopyMessageConverter.fromMessage(msg);
        assertNotNull(obj);
        assertTrue(obj instanceof ContentCopyMessage);

        ContentCopyMessage contentCopyMessage = (ContentCopyMessage) obj;
        assertEquals(storeId, contentCopyMessage.getStoreId());
        assertEquals(srcSpaceId, contentCopyMessage.getSourceSpaceId());
        assertEquals(srcContentId, contentCopyMessage.getSourceContentId());
        assertEquals(destSpaceId, contentCopyMessage.getDestSpaceId());
        assertEquals(destContentId, contentCopyMessage.getDestContentId());
        assertEquals(username, contentCopyMessage.getUsername());

        EasyMock.verify(msg);
    }

    @Test
    public void testToMessage() throws JMSException {
        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setStringProperty(ContentCopyMessageConverter.STORE_ID,
                                 storeId);
        EasyMock.expectLastCall().once();

        mapMsg.setStringProperty(ContentCopyMessageConverter.DEST_SPACE_ID,
                         destSpaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentCopyMessageConverter.SRC_SPACE_ID,
                         srcSpaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentCopyMessageConverter.SRC_CONTENT_ID,
                         srcContentId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentCopyMessageConverter.DEST_SPACE_ID,
                         destSpaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentCopyMessageConverter.DEST_CONTENT_ID,
                         destContentId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ContentCopyMessageConverter.USERNAME, username);
        EasyMock.expectLastCall().once();

        ContentCopyMessage contentCopyMessage = new ContentCopyMessage();
        contentCopyMessage.setStoreId(storeId);
        contentCopyMessage.setSourceSpaceId(srcSpaceId);
        contentCopyMessage.setSourceContentId(srcContentId);
        contentCopyMessage.setDestSpaceId(destSpaceId);
        contentCopyMessage.setDestContentId(destContentId);
        contentCopyMessage.setUsername(username);

        EasyMock.replay(mapMsg, session);

        Message msg =
            contentCopyMessageConverter.toMessage((Object)contentCopyMessage,
                                                  session);
        assertNotNull(msg);
        assertTrue(msg instanceof MapMessage);

        EasyMock.verify(mapMsg, session);
    }
}
