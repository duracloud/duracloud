/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.rest;

import static junit.framework.Assert.assertNotNull;
import org.apache.http.HttpStatus;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.List;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class TestTaskRest extends BaseRestTester {

    @Test
    public void getSupportedTasks() throws Exception {
        String url = BaseRestTester.baseUrl + "/task";
        RestHttpHelper.HttpResponse response = BaseRestTester.restHelper.get(url);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);
        List<String> supportedTasks =
            SerializationUtil.deserializeList(responseText);
        assertNotNull(supportedTasks);
        assertTrue(supportedTasks.contains("noop"));
    }

    @Test
    public void testPerformTask() throws Exception {
        // Noop Task
        String taskId = "noop";
        String url = BaseRestTester.baseUrl + "/task/" + taskId;
        RestHttpHelper.HttpResponse response = BaseRestTester.restHelper.post(url, null, null);
        String responseText = checkResponse(response, HttpStatus.SC_OK);
        assertNotNull(responseText);        

        // Unsupported Task
        taskId = "unsupported-task";
        url = BaseRestTester.baseUrl + "/task/" + taskId;
        response = BaseRestTester.restHelper.post(url, null, null);
        responseText = checkResponse(response, HttpStatus.SC_BAD_REQUEST);
        assertTrue(responseText.contains(taskId));
    }

}
