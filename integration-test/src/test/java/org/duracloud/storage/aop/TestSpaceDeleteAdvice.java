/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.durastore.aop.MessagingTestSupport;
import org.duracloud.durastore.rest.RestTestHelper;
import org.duracloud.storage.aop.ContentMessageConverter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.Random;

/**
 * <pre>
 *
 * This test exercises three elements of the delete flow:
 * 1. actual space deletion
 * 2. aop publishing delete event to topic
 * 3. topic consumer asynchronously receiving the event
 *
 * </pre>
 */
public class TestSpaceDeleteAdvice
        extends MessagingTestSupport
        implements MessageListener {

    private Connection conn;

    private Session session;

    private Destination destination;

    private boolean received;

    private final long MAX_WAIT = 5000;


    private static String spaceId;

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Initialize the Instance
        HttpResponse response = RestTestHelper.initialize();
        int statusCode = response.getStatusCode();
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);
    }

    @Before
    public void setUp() throws Exception {
        conn = createConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = createDestination(spaceDeleteTopicName);
        received = false;

        // Add space
        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "delete-advice-test-space-" + random;
        HttpResponse response = RestTestHelper.addSpace(spaceId);
        int statusCode = response.getStatusCode();
        String responseText = response.getResponseBody();
        Assert.assertEquals(responseText, HttpStatus.SC_CREATED, statusCode);
    }

    @After
    public void tearDown() throws Exception {
        RestTestHelper.deleteSpace(spaceId);

        if (conn != null) {
            conn.close();
            conn = null;
        }
        if (session != null) {
            session.close();
            session = null;
        }
        destination = null;
    }

    @Test
    public void testDeleteEventFail() throws Exception {
        boolean successful = false;
        doTestDeleteEvent(successful);
    }

    @Test
    public void testDeleteEventPass() throws Exception {
        boolean successful = true;
        doTestDeleteEvent(successful);
    }

    private void doTestDeleteEvent(boolean successful) throws Exception {
        createEventListener(null);
        publishDeleteEvent(successful);
        verifyEventHeard(successful);
    }

    @Test
    public void testDeleteEventSelectorFail() throws Exception {
        boolean successful = false;
        String selector = ContentMessageConverter.STORE_ID + " = 'invalidStoreId'";
        doTestDeleteSelectorEvent(selector, successful);
    }

    @Test
    public void testDeleteEventSelectorPass() throws Exception {
        boolean successful = true;
        // Store ID 1 is the ID for the default storage provider (Amazon S3)
        String selector = ContentMessageConverter.STORE_ID + " = '1'";
        doTestDeleteSelectorEvent(selector, successful);
    }

    private void doTestDeleteSelectorEvent(String selector,
                                           boolean successful) throws Exception {
        createEventListener(selector);
        publishDeleteEvent(true);
        verifyEventHeard(successful);
    }

    /**
     * This method implements the MessageListener.
     */
    public void onMessage(Message msg) {
        received = true;
    }

    private void createEventListener(String selector) throws Exception {
        javax.jms.MessageConsumer consumer =
                session.createConsumer(destination, selector);
        consumer.setMessageListener(this);
        conn.start();
    }

    private void publishDeleteEvent(boolean successful) throws Exception {
        if (successful) {
            // Delete space
            HttpResponse response = RestTestHelper.deleteSpace(spaceId);
            Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
            String responseText = response.getResponseBody();
            assertNotNull(responseText);
            assertTrue(responseText.contains(spaceId));
        }
    }

    private void verifyEventHeard(boolean successful) throws Exception {
        boolean expired = false;
        long startTime = System.currentTimeMillis();
        while (!received && !expired) {
            Thread.sleep(1000);
            expired = MAX_WAIT < (System.currentTimeMillis() - startTime);
        }
        Assert.assertEquals(received, successful);
    }

}
