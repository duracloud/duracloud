/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.exec;

import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 4/4/12
 */
public class ExecutorImplTest {

    protected ExecutorImpl exec;
    
    protected RestHttpHelper mockRestHelper;
    protected String host = "host";
    protected String port = "8080";
    protected String context = "context";

    protected String baseUrl = "http://"+host+":"+port+"/"+context;
    protected RestHttpHelper.HttpResponse successResponse;

    @Before
    public void setup() {
        mockRestHelper = EasyMock.createMock(RestHttpHelper.class);

        exec = new ExecutorImpl(host, port, context, mockRestHelper);
        setResponse("result");
    }

    private void setResponse(String value) {
        InputStream stream = new ByteArrayInputStream(value.getBytes());
        successResponse =
            new RestHttpHelper.HttpResponse(200, null, null, stream);
    }

    private void replayMocks() {
        EasyMock.replay(mockRestHelper);
    }

    @After
    public void teardown() {
        EasyMock.verify(mockRestHelper);
    }

    private String getBaseUrl() {
        return baseUrl + "/exec";
    }

    @Test
    public void testGetSupportedActions() throws Exception {
        String url = getBaseUrl() + "/action";

        Set<String> actions = new HashSet<String>();
        actions.add("action-1");
        actions.add("action-2");
        setResponse(SerializationUtil.serializeSet(actions));

        EasyMock.expect(mockRestHelper.get(url))
                .andReturn(successResponse);

        replayMocks();

        Set<String> resultActions = exec.getSupportedActions();
        assertNotNull(resultActions);
        assertEquals(actions, resultActions);
    }

    @Test
    public void testPerformAction() throws Exception {
        String actionName = "actionName";
        String actionParams = "actionParams";
        String url = getBaseUrl() + "/" + actionName;

        EasyMock.expect(mockRestHelper.post(url, actionParams, null))
                .andReturn(successResponse);

        replayMocks();

        exec.performAction(actionName, actionParams);
    }

    @Test
    public void testGetStatus() throws Exception {
        Map<String, String> status = new HashMap<String, String>();
        status.put("status-1", "value-1");
        status.put("status-2", "value-2");
        setResponse(SerializationUtil.serializeMap(status));

        EasyMock.expect(mockRestHelper.get(getBaseUrl()))
                .andReturn(successResponse);

        replayMocks();

        Map<String, String> resultStatus = exec.getStatus();
        assertNotNull(resultStatus);
        assertEquals(status, resultStatus);
    }

    @Test
    public void testStop() throws Exception {
        EasyMock.expect(mockRestHelper.delete(getBaseUrl()))
                .andReturn(successResponse);

        replayMocks();

        exec.stop();
    }

}
