/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.aop;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jms.support.converter.MessageConverter;

import javax.jms.Connection;
import javax.jms.MapMessage;
import javax.jms.Session;

public class TestContentMessageConverter
        extends MessagingTestSupport {

    private Connection conn;

    private Session session;

    private MessageConverter converter;

    private MapMessage mapMsg;

    private ContentMessage contentMsg;

    private final String STORE_ID = "testStoreId";

    private final String SPACE_ID = "testSpaceId";

    private final String CONTENT_ID = "testContentId";

    @Before
    public void setUp() throws Exception {
        conn = createConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        converter = new ContentMessageConverter();
        mapMsg = session.createMapMessage();
        mapMsg.setStringProperty(ContentMessageConverter.STORE_ID, STORE_ID);
        mapMsg.setString(ContentMessageConverter.CONTENT_ID, CONTENT_ID);
        mapMsg.setString(ContentMessageConverter.SPACE_ID, SPACE_ID);

        contentMsg = new ContentMessage();
        contentMsg.setStoreId(STORE_ID);
        contentMsg.setContentId(CONTENT_ID);
        contentMsg.setSpaceId(SPACE_ID);
    }

    @After
    public void tearDown() throws Exception {
        if (session != null) {
            session.close();
            session = null;
        }
        if (conn != null) {
            conn.close();
            conn = null;
        }
        converter = null;
        mapMsg = null;
        contentMsg = null;
    }

    @Test
    public void testFromMessage() throws Exception {
        ContentMessage msg = (ContentMessage) converter.fromMessage(mapMsg);
        assertNotNull(msg);
        Assert.assertEquals(STORE_ID, msg.getStoreId());
        Assert.assertEquals(CONTENT_ID, msg.getContentId());
        Assert.assertEquals(SPACE_ID, msg.getSpaceId());
    }

    @Test
    public void testToMessage() throws Exception {
        MapMessage msg = (MapMessage) converter.toMessage(contentMsg, session);
        assertNotNull(msg);
        Assert.assertEquals(STORE_ID, msg.getStringProperty(
            ContentMessageConverter.STORE_ID));
        Assert.assertEquals(CONTENT_ID, msg.getString(ContentMessageConverter.CONTENT_ID));
        Assert.assertEquals(SPACE_ID, msg.getString(ContentMessageConverter.SPACE_ID));
    }

}
