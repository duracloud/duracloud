/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.duracloud.durastore.aop.MessagingTestSupport;
import org.duracloud.storage.aop.SpaceMessage;
import org.duracloud.storage.aop.SpaceMessageConverter;
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

public class TestSpaceMessageConverter
        extends MessagingTestSupport {

    private Connection conn;

    private Session session;

    private MessageConverter converter;

    private MapMessage mapMsg;

    private SpaceMessage spaceMsg;

    private final String STORE_ID = "testStoreId";

    private final String SPACE_ID = "testSpaceId";

    @Before
    public void setUp() throws Exception {
        conn = createConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        converter = new SpaceMessageConverter();
        mapMsg = session.createMapMessage();
        mapMsg.setStringProperty(SpaceMessageConverter.STORE_ID, STORE_ID);
        mapMsg.setString(SpaceMessageConverter.SPACE_ID, SPACE_ID);

        spaceMsg = new SpaceMessage();
        spaceMsg.setStoreId(STORE_ID);
        spaceMsg.setSpaceId(SPACE_ID);
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
        spaceMsg = null;
    }

    @Test
    public void testFromMessage() throws Exception {
        SpaceMessage msg = (SpaceMessage) converter.fromMessage(mapMsg);
        assertNotNull(msg);
        Assert.assertEquals(STORE_ID, msg.getStoreId());
        Assert.assertEquals(SPACE_ID, msg.getSpaceId());
    }

    @Test
    public void testToMessage() throws Exception {
        MapMessage msg = (MapMessage) converter.toMessage(spaceMsg, session);
        assertNotNull(msg);
        Assert.assertEquals(STORE_ID, msg.getStringProperty(
            SpaceMessageConverter.STORE_ID));
        Assert.assertEquals(SPACE_ID, msg.getString(SpaceMessageConverter.SPACE_ID));
    }

}
