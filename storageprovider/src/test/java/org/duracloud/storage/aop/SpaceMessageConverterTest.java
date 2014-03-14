/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.duracloud.storage.aop.SpaceMessage;
import org.duracloud.storage.aop.SpaceMessageConverter;
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

public class SpaceMessageConverterTest {
    private SpaceMessageConverter spaceMessageConverter;

    @Before
    public void setUp() throws Exception {
        spaceMessageConverter = new SpaceMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try{
            Message msg = EasyMock.createMock("Message",
                                              Message.class);

            spaceMessageConverter.fromMessage(msg);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try{
            spaceMessageConverter.toMessage((Object) "", null);
            assertTrue(false);

        } catch(MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromMessage() throws JMSException {
        String storeId = "storeId";
        String spaceId = "spaceId";
        String username = "username";
        
        MapMessage msg = EasyMock.createMock("MapMessage",
                                             MapMessage.class);

        msg.getStringProperty(SpaceMessageConverter.STORE_ID);
        EasyMock.expectLastCall().andReturn(storeId);

        msg.getString(SpaceMessageConverter.SPACE_ID);
        EasyMock.expectLastCall().andReturn(spaceId);

        msg.getString(SpaceMessageConverter.USERNAME);
        EasyMock.expectLastCall().andReturn(username);

        EasyMock.replay(msg);
        Object obj = spaceMessageConverter.fromMessage(msg);
        EasyMock.verify(msg);

        assertNotNull(obj);
        assertTrue(obj instanceof SpaceMessage);

        SpaceMessage spaceMessage = (SpaceMessage) obj;
        assertEquals(storeId, spaceMessage.getStoreId());
        assertEquals(spaceId, spaceMessage.getSpaceId());
        assertEquals(username, spaceMessage.getUsername());
    }

    @Test
    public void testToMessage() throws JMSException {
        String storeId = "storeId";
        String spaceId = "spaceId";
        String username = "username";

        MapMessage mapMsg = EasyMock.createMock("MapMessage",
                                                MapMessage.class);
        Session session = EasyMock.createMock("Session",
                                              Session.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setStringProperty(SpaceMessageConverter.STORE_ID, storeId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(SpaceMessageConverter.SPACE_ID, spaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(SpaceMessageConverter.USERNAME, username);
        EasyMock.expectLastCall().once();

        SpaceMessage spaceMessage = new SpaceMessage();
        spaceMessage.setStoreId(storeId);
        spaceMessage.setSpaceId(spaceId);
        spaceMessage.setUsername(username);

        EasyMock.replay(mapMsg);
        EasyMock.replay(session);
        Message msg = spaceMessageConverter.toMessage((Object)spaceMessage,
                                                      session);
        EasyMock.verify(mapMsg);        
        EasyMock.verify(session);

        assertNotNull(msg);
        assertTrue(msg instanceof MapMessage);
    }
}
