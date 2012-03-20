/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.duracloud.durastore.aop.MessagingTestSupport;
import org.duracloud.storage.aop.IngestMessage;
import org.duracloud.storage.aop.IngestMessageConverter;
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

public class TestIngestMessageConverter
        extends MessagingTestSupport {

    private Connection conn;

    private Session session;

    private MessageConverter converter;

    private MapMessage mapMsg;

    private IngestMessage ingestMsg;

    private final String STORE_ID = "testStoreId";

    private final String SPACE_ID = "testSpaceId";

    private final String MIMETYPE = "testMimeType";

    private final String CONTENT_ID = "testContentId";

    @Before
    public void setUp() throws Exception {
        conn = createConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);

        converter = new IngestMessageConverter();
        mapMsg = session.createMapMessage();
        mapMsg.setStringProperty(IngestMessageConverter.STORE_ID, STORE_ID);
        mapMsg.setString(IngestMessageConverter.CONTENT_ID, CONTENT_ID);
        mapMsg.setString(IngestMessageConverter.MIMETYPE, MIMETYPE);
        mapMsg.setString(IngestMessageConverter.SPACE_ID, SPACE_ID);

        ingestMsg = new IngestMessage();
        ingestMsg.setStoreId(STORE_ID);
        ingestMsg.setContentId(CONTENT_ID);
        ingestMsg.setContentMimeType(MIMETYPE);
        ingestMsg.setSpaceId(SPACE_ID);
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
        ingestMsg = null;
    }

    @Test
    public void testFromMessage() throws Exception {
        IngestMessage msg = (IngestMessage) converter.fromMessage(mapMsg);
        assertNotNull(msg);
        Assert.assertEquals(STORE_ID, msg.getStoreId());
        Assert.assertEquals(CONTENT_ID, msg.getContentId());
        Assert.assertEquals(MIMETYPE, msg.getContentMimeType());
        Assert.assertEquals(SPACE_ID, msg.getSpaceId());
    }

    @Test
    public void testToMessage() throws Exception {
        MapMessage msg = (MapMessage) converter.toMessage(ingestMsg, session);
        assertNotNull(msg);
        Assert.assertEquals(STORE_ID, msg.getStringProperty(IngestMessageConverter.STORE_ID));
        Assert.assertEquals(CONTENT_ID, msg.getString(IngestMessageConverter.CONTENT_ID));
        Assert.assertEquals(MIMETYPE, msg.getString(IngestMessageConverter.MIMETYPE));
        Assert.assertEquals(SPACE_ID, msg.getString(IngestMessageConverter.SPACE_ID));
    }

}
