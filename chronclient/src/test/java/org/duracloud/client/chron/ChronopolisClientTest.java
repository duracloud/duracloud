/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chron;

import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.duracloud.client.chron.error.ChronopolisException;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.httpclient.HttpStatus.SC_ACCEPTED;
import static org.apache.commons.httpclient.HttpStatus.SC_OK;
import static org.duracloud.client.chron.ResponseKey.ACCT_ID;
import static org.duracloud.client.chron.ResponseKey.HTTP_STATUS;
import static org.duracloud.client.chron.ResponseKey.IDENTIFIER;
import static org.duracloud.client.chron.ResponseKey.ITEM_ID;
import static org.duracloud.client.chron.ResponseKey.REQUEST_TYPE;
import static org.duracloud.client.chron.ResponseKey.RETRY_AFTER;
import static org.duracloud.client.chron.ResponseKey.SPACE_ID;
import static org.duracloud.client.chron.ResponseKey.STATUS;
import static org.duracloud.client.chron.ResponseKey.STATUS_MSG;

/**
 * @author Andrew Woods
 *         Date: 10/25/12
 */
public class ChronopolisClientTest {

    private ChronopolisClient client;
    private RestHttpHelper restHelper;
    private RestHttpHelper.HttpResponse mockResponse;

    private static final String host = "host";
    private static final String port = "443";
    private static final String acctId = "acct-id";

    @Before
    public void setUp() throws Exception {
        restHelper = EasyMock.createMock("RestHttpHelper",
                                         RestHttpHelper.class);
        mockResponse = EasyMock.createMock("HttpResponse",
                                           RestHttpHelper.HttpResponse.class);
        client = new ChronopolisClient(acctId, host, port, restHelper);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(restHelper, mockResponse);
    }

    private void replayMocks() {
        EasyMock.replay(restHelper, mockResponse);
    }

    @Test
    public void testPutContentSpace() throws Exception {
        String spaceId = "space-id";
        String text = "some-text";
        String manifestMd5 = getMd5(text);
        InputStream manifest =
            new AutoCloseInputStream(new ByteArrayInputStream(text.getBytes()));

        Map<String, String> headers = new HashMap<String, String>();
        headers.put(RequestKey.CONTENT_MD5.toString(), manifestMd5);

        EasyMock.expect(restHelper.put(EasyMock.eq(
            "https://" + host + "/chron-notify/resources/notify/" + acctId +
                "/" + spaceId),
                                       EasyMock.anyObject(InputStream.class),
                                       EasyMock.eq(Long.toString(text.length())),
                                       EasyMock.eq("text/plain;charset=UTF-8"),
                                       headerMatches(headers))).andReturn(
            mockResponse);

        EasyMock.expect(mockResponse.getStatusCode())
                .andReturn(SC_ACCEPTED)
                .times(2);
        EasyMock.expect(mockResponse.getResponseHeader(RETRY_AFTER.toString()))
                .andReturn(new Header(RETRY_AFTER.toString(), "120"));

        Map<String, String> responseMap = createResponseMap();
        responseMap.put(SPACE_ID.toString(), spaceId);

        Map<String, String> expectedMap = createExpectedMap();
        expectedMap.put(HTTP_STATUS.toString(), Integer.toString(SC_ACCEPTED));

        InputStream body = createBodyStream(responseMap);
        EasyMock.expect(mockResponse.getResponseStream()).andReturn(body);

        replayMocks();

        // Do the actual test.
        Map<String, String> response = client.putContentSpace(spaceId,
                                                              manifest);

        verifyMaps(expectedMap, response);
    }

    private InputStream createBodyStream(Map<String, String> map)
        throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        byte[] body = mapper.writeValueAsBytes(map);
        return new AutoCloseInputStream(new ByteArrayInputStream(body));
    }

    private String getMd5(String text) {
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        return util.generateChecksum(text);
    }

    private Map<String, String> createExpectedMap() {
        Map<String, String> map = createResponseMap();
        map.put(HTTP_STATUS.toString(), Integer.toString(HttpStatus.SC_OK));
        map.put(RETRY_AFTER.toString(), "120");

        return map;
    }

    private Map<String, String> createResponseMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(ACCT_ID.toString(), acctId);
        map.put(IDENTIFIER.toString(), "776655001");
        map.put(REQUEST_TYPE.toString(), "3");
        map.put(SPACE_ID.toString(), "space-id");
        map.put(STATUS.toString(), "processing-status");
        map.put(STATUS_MSG.toString(), "status-msg");
        return map;
    }

    private void verifyMaps(Map<String, String> expectedMap,
                            Map<String, String> map) {
        Assert.assertNotNull(map);
        Assert.assertEquals(map + " != " + expectedMap,
                            expectedMap.size(),
                            map.size());

        for (String key : expectedMap.keySet()) {
            Assert.assertTrue(map + " != " + expectedMap, map.containsKey(key));

            String value = expectedMap.get(key);
            String found = map.get(key);
            Assert.assertEquals(value, found);
        }
    }

    @Test
    public void testGetProcessingStatus() throws Exception {
        String identifier = "98237640";

        EasyMock.expect(restHelper.get(EasyMock.eq(
            "https://" + host + "/chron-notify/resources/status/" +
                identifier))).andReturn(mockResponse);

        EasyMock.expect(mockResponse.getStatusCode())
                .andReturn(HttpStatus.SC_OK)
                .times(2);
        EasyMock.expect(mockResponse.getResponseHeader(RETRY_AFTER.toString()))
                .andReturn(new Header(RETRY_AFTER.toString(), "120"));

        Map<String, String> responseMap = createResponseMap();
        responseMap.put(IDENTIFIER.toString(), identifier);

        Map<String, String> expectedMap = createExpectedMap();
        expectedMap.put(IDENTIFIER.toString(), identifier);
        expectedMap.put(HTTP_STATUS.toString(), Integer.toString(SC_OK));

        InputStream body = createBodyStream(responseMap);
        EasyMock.expect(mockResponse.getResponseStream()).andReturn(body);

        replayMocks();

        // Do the test.
        Map<String, String> response = client.getProcessingStatus(identifier);

        verifyMaps(expectedMap, response);
    }

    @Test
    public void testGetProcessingStatusError() throws Exception {
        String identifier = "98237640";

        EasyMock.expect(restHelper.get(EasyMock.eq(
            "https://" + host + "/chron-notify/resources/status/" +
                identifier))).andReturn(mockResponse);

        // Error status code!
        EasyMock.expect(mockResponse.getStatusCode())
                .andReturn(HttpStatus.SC_FORBIDDEN);

        Map<String, String> expectedMap = createResponseMap();
        expectedMap.put(IDENTIFIER.toString(), identifier);

        EasyMock.expect(mockResponse.getResponseBody()).andReturn("error");

        replayMocks();

        // Do the test.
        try {
            client.getProcessingStatus(identifier);
            Assert.fail("Exception expected.");
        } catch (ChronopolisException e) {
            Assert.assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testGetReceiptManifest() throws Exception {
        String identifier = "98237640";

        EasyMock.expect(restHelper.get(EasyMock.eq(
            "https://" + host + "/chron-notify/resources/status/" +
                identifier + "/receipt"))).andReturn(mockResponse);

        EasyMock.expect(mockResponse.getStatusCode())
                .andReturn(HttpStatus.SC_OK);

        String text = "a test response body";
        InputStream body = new AutoCloseInputStream(new ByteArrayInputStream(
            text.getBytes()));
        EasyMock.expect(mockResponse.getResponseStream()).andReturn(body);

        replayMocks();

        // Do the test.
        InputStream response = client.getReceiptManifest(identifier);

        Assert.assertNotNull(response);

        StringWriter result = new StringWriter();
        IOUtils.copy(response, result);

        Assert.assertEquals(text, result.toString());
    }

    @Test
    public void testGetContentItem() throws Exception {
        String spaceId = "space-id";
        String contentId = "content-id";

        EasyMock.expect(restHelper.get(EasyMock.eq(
            "https://" + host + "/chron-notify/resources/notify/" + acctId +
                "/" + spaceId + "/" + contentId))).andReturn(mockResponse);

        EasyMock.expect(mockResponse.getStatusCode())
                .andReturn(SC_ACCEPTED)
                .times(2);
        EasyMock.expect(mockResponse.getResponseHeader(RETRY_AFTER.toString()))
                .andReturn(new Header(RETRY_AFTER.toString(), "120"));

        Map<String, String> responseMap = createResponseMap();
        responseMap.put(SPACE_ID.toString(), spaceId);
        responseMap.put(ITEM_ID.toString(), contentId);

        Map<String, String> expectedMap = createExpectedMap();
        expectedMap.put(SPACE_ID.toString(), spaceId);
        expectedMap.put(ITEM_ID.toString(), contentId);
        expectedMap.put(HTTP_STATUS.toString(), Integer.toString(SC_ACCEPTED));
        expectedMap.put(RETRY_AFTER.toString(), "120");

        InputStream body = createBodyStream(responseMap);
        EasyMock.expect(mockResponse.getResponseStream()).andReturn(body);

        replayMocks();

        // Do the actual test.
        Map<String, String> response = client.getContentItem(spaceId,
                                                             contentId);

        verifyMaps(expectedMap, response);
    }

    @Test
    public void testGetContentSpace() throws Exception {
        String spaceId = "space-id";

        EasyMock.expect(restHelper.get(EasyMock.eq(
            "https://" + host + "/chron-notify/resources/notify/" + acctId +
                "/" + spaceId))).andReturn(mockResponse);

        EasyMock.expect(mockResponse.getStatusCode())
                .andReturn(SC_ACCEPTED)
                .times(2);
        EasyMock.expect(mockResponse.getResponseHeader(RETRY_AFTER.toString()))
                .andReturn(new Header(RETRY_AFTER.toString(), "120"));

        Map<String, String> responseMap = createResponseMap();
        responseMap.put(SPACE_ID.toString(), spaceId);

        Map<String, String> expectedMap = createExpectedMap();
        expectedMap.put(SPACE_ID.toString(), spaceId);
        expectedMap.put(HTTP_STATUS.toString(), Integer.toString(SC_ACCEPTED));
        expectedMap.put(RETRY_AFTER.toString(), "120");

        InputStream body = createBodyStream(expectedMap);
        EasyMock.expect(mockResponse.getResponseStream()).andReturn(body);

        replayMocks();

        // Do the actual test.
        Map<String, String> response = client.getContentSpace(spaceId);

        verifyMaps(expectedMap, response);
    }


    private static class HeaderMatcher implements IArgumentMatcher {
        private Map<String, String> expectedMap;

        public HeaderMatcher(Map<String, String> map) {
            this.expectedMap = map;
        }

        @Override
        public boolean matches(Object argument) {
            if (null == argument || !(argument instanceof Map)) {
                return false;
            }

            for (String key : expectedMap.keySet()) {
                if (!((Map) argument).containsKey(key)) {
                    return false;
                }

                String value = expectedMap.get(key);
                String found = (String) ((Map) argument).get(key);
                if (!value.equals(found)) {
                    return false;
                }
            }

            return expectedMap.size() == ((Map) argument).size();
        }

        @Override
        public void appendTo(StringBuffer buffer) {
            buffer.append("headerMatches(");
            buffer.append(expectedMap.getClass().getName());
            buffer.append(" with values: ");
            buffer.append(expectedMap.toString());
            buffer.append(")");
        }
    }

    public static <T extends Map<String, String>> T headerMatches(T expected) {
        EasyMock.reportMatcher(new HeaderMatcher(expected));
        return null;
    }

}
