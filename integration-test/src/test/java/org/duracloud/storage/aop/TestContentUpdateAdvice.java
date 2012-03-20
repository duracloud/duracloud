/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.aop;

import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.rest.HttpHeaders;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.durastore.aop.MessagingTestSupport;
import org.duracloud.durastore.rest.BaseRest;
import org.duracloud.durastore.rest.RestTestHelper;
import org.duracloud.storage.aop.SpaceMessageConverter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * <pre>
 *
 * This test exercises three elements of the update flow:
 * 1. actual content update
 * 2. aop publishing update event to topic
 * 3. topic consumer asynchronously receiving the event
 *
 * </pre>
 */
public class TestContentUpdateAdvice
        extends MessagingTestSupport
        implements MessageListener {

    private Connection conn;

    private Session session;

    private Destination destination;

    private boolean received;

    private final long MAX_WAIT = 5000;

    private static RestHttpHelper restHelper = RestTestHelper.getAuthorizedRestHelper();

    private static final String CONTENT = "<junk/>";

    private static String spaceId;

    static {
        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "update-advice-test-space-" + random;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        // Initialize the Instance
        HttpResponse response = RestTestHelper.initialize();
        int statusCode = response.getStatusCode();
        Assert.assertEquals(HttpStatus.SC_OK, statusCode);

        // Add space
        response = RestTestHelper.addSpace(spaceId);
        statusCode = response.getStatusCode();
        String responseText = response.getResponseBody();
        Assert.assertEquals(responseText, HttpStatus.SC_CREATED, statusCode);
    }

    @Before
    public void setUp() throws Exception {
        conn = createConnection();
        session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        destination = createDestination(contentUpdateTopicName);
        received = false;
    }

    @After
    public void tearDown() throws Exception {
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

    @AfterClass
    public static void afterClass() throws Exception {
        // Delete space
        HttpResponse response = RestTestHelper.deleteSpace(spaceId);
        Assert.assertEquals(HttpStatus.SC_OK, response.getStatusCode());
        String responseText = response.getResponseBody();
        assertNotNull(responseText);
        assertTrue(responseText.contains(spaceId));
    }

    @Test
    public void testUpdateEventSelectorFail() throws Exception {
        boolean successful = false;
        String selector = SpaceMessageConverter.STORE_ID + " = 'invalidStoreId'";
        doTestUpdateSelectorEvent(selector, successful);
    }

    @Test
    public void testUpdateEventSelectorPass() throws Exception {
        boolean successful = true;
        // Store ID 1 is the ID for the default storage provider (Amazon S3)
        String selector = SpaceMessageConverter.STORE_ID + " = '1'";
        doTestUpdateSelectorEvent(selector, successful);
    }

    private void doTestUpdateSelectorEvent(String selector,
                                           boolean successful) throws Exception {
        createEventListener(selector);
        publishUpdateEvent(true);
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

    private void publishUpdateEvent(boolean successful) throws Exception {
        // Add content
        String suffix = "/" + spaceId + "/contentGOOD";
        String url = RestTestHelper.getBaseUrl() + suffix;
        HttpResponse response = restHelper.put(url, CONTENT, null);
        int statusCode = response.getStatusCode();
        String responseText = response.getResponseBody();
        Assert.assertEquals(responseText, HttpStatus.SC_CREATED, statusCode);

        if (!successful) {
            suffix = "/" + spaceId + "-invalid-space-id/contentBAD";
            url = RestTestHelper.getBaseUrl() + suffix;
        }

        // Update properties
        Map<String, String> headers = new HashMap<String, String>();
        String newContentMime = "text/plain";
        headers.put(HttpHeaders.CONTENT_TYPE, newContentMime);
        String newMetaName = BaseRest.HEADER_PREFIX + "new-properties";
        String newMetaValue = "New Properties Value";
        headers.put(newMetaName, newMetaValue);
        
        response = restHelper.post(url, null, headers);
        statusCode = response.getStatusCode();
        responseText = response.getResponseBody();
        Assert.assertEquals(responseText, HttpStatus.SC_OK, statusCode);
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
