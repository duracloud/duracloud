/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

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
import static org.duracloud.storage.aop.BaseContentMessageConverter.ACTION;
import static org.duracloud.storage.aop.BaseContentMessageConverter.CONTENT_ID;
import static org.duracloud.storage.aop.BaseContentMessageConverter.SPACE_ID;
import static org.duracloud.storage.aop.BaseContentMessageConverter.STORE_ID;
import static org.duracloud.storage.aop.BaseContentMessageConverter.USERNAME;

/**
 * @author Andrew Woods
 *         Date: 3/20/12
 */
public class AllContentEventsMessageConverterTest {

    private AllContentEventsMessageConverter converter;

    private String storeId = "storeId";
    private String spaceId = "spaceId";
    private String contentId = "contentId";
    private String username = "username";

    @Before
    public void setUp() throws Exception {
        converter = new AllContentEventsMessageConverter();
    }

    @Test
    public void testFromConversionException() throws JMSException {
        try {
            Message msg = EasyMock.createMock("Message", Message.class);
            converter.fromMessage(msg);
            assertTrue(false);

        } catch (MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testToConversionException() throws JMSException {
        try {
            converter.toMessage("", null);
            assertTrue(false);

        } catch (MessageConversionException mce) {
            assertTrue(true);
        }
    }

    @Test
    public void testFromMessageCopy() throws JMSException {
        doTestFromMessage(ContentMessage.ACTION.COPY);
    }

    @Test
    public void testFromMessageIngest() throws JMSException {
        doTestFromMessage(ContentMessage.ACTION.INGEST);
    }

    @Test
    public void testFromMessageUpdate() throws JMSException {
        doTestFromMessage(ContentMessage.ACTION.UPDATE);
    }

    @Test
    public void testFromMessageDelete() throws JMSException {
        doTestFromMessage(ContentMessage.ACTION.DELETE);
    }

    private void doTestFromMessage(ContentMessage.ACTION action)
        throws JMSException {
        MapMessage msg = createMockMapMessageTo(action.name());

        Object result = converter.fromMessage(msg);
        assertNotNull(result);

        switch (action) {
            case COPY:
                assertTrue(result instanceof ContentCopyMessage);
                break;
            case INGEST:
                assertTrue(result instanceof IngestMessage);
                break;
            case UPDATE:
            case DELETE:
                assertTrue(result instanceof ContentMessage);
                break;
            default:
                assertTrue("unexpected action: " + action, false);
        }

        verifyMessage(action.name(), (ContentMessage) result);
        EasyMock.verify(msg);
    }

    private MapMessage createMockMapMessageTo(String action)
        throws JMSException {
        MapMessage msg = EasyMock.createNiceMock("MapMessage",
                                                 MapMessage.class);

        EasyMock.expect(msg.getStringProperty(STORE_ID)).andReturn(storeId);
        EasyMock.expect(msg.getString(SPACE_ID)).andReturn(spaceId);
        EasyMock.expect(msg.getString(CONTENT_ID)).andReturn(contentId);
        EasyMock.expect(msg.getString(USERNAME)).andReturn(username);
        EasyMock.expect(msg.getString(ACTION)).andReturn(action).times(2);

        EasyMock.replay(msg);
        return msg;
    }

    private void verifyMessage(String action, ContentMessage contentMessage) {
        assertEquals(storeId, contentMessage.getStoreId());
        assertEquals(spaceId, contentMessage.getSpaceId());
        assertEquals(contentId, contentMessage.getContentId());
        assertEquals(username, contentMessage.getUsername());
        assertEquals(action, contentMessage.getAction());
    }

    @Test
    public void testToMessageCopy() throws JMSException {
        doTestToMessage(ContentMessage.ACTION.COPY);
    }

    @Test
    public void testToMessageIngest() throws JMSException {
        doTestToMessage(ContentMessage.ACTION.INGEST);
    }

    @Test
    public void testToMessageUpdate() throws JMSException {
        doTestToMessage(ContentMessage.ACTION.UPDATE);
    }

    @Test
    public void testToMessageDelete() throws JMSException {
        doTestToMessage(ContentMessage.ACTION.DELETE);
    }

    private void doTestToMessage(ContentMessage.ACTION action)
        throws JMSException {
        Session session = EasyMock.createMock("Session", Session.class);
        MapMessage mapMsg = createMapMessageFrom(action, session);

        ContentMessage msg = createContentMessage(action);

        EasyMock.replay(session, mapMsg);

        Message result = converter.toMessage(msg, session);
        assertNotNull(result);
        assertTrue(result instanceof MapMessage);

        EasyMock.verify(session, mapMsg);
    }

    private MapMessage createMapMessageFrom(ContentMessage.ACTION action,
                                            Session session)
        throws JMSException {
        MapMessage mapMsg = EasyMock.createNiceMock("MapMessage",
                                                    MapMessage.class);

        session.createMapMessage();
        EasyMock.expectLastCall().andReturn(mapMsg);

        mapMsg.setStringProperty(STORE_ID, storeId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(SPACE_ID, spaceId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(CONTENT_ID, contentId);
        EasyMock.expectLastCall().once();

        mapMsg.setString(USERNAME, username);
        EasyMock.expectLastCall().once();

        mapMsg.setString(ACTION, action.name());
        EasyMock.expectLastCall().once();
        return mapMsg;
    }

    private ContentMessage createContentMessage(ContentMessage.ACTION action) {
        ContentMessage msg = null;
        switch (action) {
            case COPY:
                msg = new ContentCopyMessage();
                break;
            case INGEST:
                msg = new IngestMessage();
                break;
            case UPDATE:
            case DELETE:
                msg = new ContentMessage();
        }

        msg.setStoreId(storeId);
        msg.setSpaceId(spaceId);
        msg.setContentId(contentId);
        msg.setUsername(username);
        msg.setAction(action.toString());
        return msg;
    }

}
