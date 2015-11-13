/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.rest;

import javax.ws.rs.core.Response;

import org.duracloud.common.rest.RestUtil;
import org.duracloud.storage.provider.TaskProviderFactory;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests top-level error handling of TaskRest.
 *
 * @author Andrew Woods
 *         Date: Aug 31, 2010
 */
public class TaskRestExceptionsTest {

    private TaskRest taskRest;
    private TaskProviderFactory taskProviderFactory;
    private RestUtil restUtil;

    private RestExceptionsTestSupport support = new RestExceptionsTestSupport();

    @Before
    public void setUp() throws Exception {
        taskProviderFactory = support.createTaskProviderFactory();
        restUtil = support.createRestUtil();
        taskRest = new TaskRest(taskProviderFactory, restUtil);
    }

    @Test
    public void testGetSupportedTasks() throws Exception {
        Response response = taskRest.getSupportedTasks(null);
        support.verifyErrorResponse(response);
    }

    @Test
    public void testPerformTask() throws Exception {
        Response response = taskRest.performTask(null, null);
        support.verifyErrorResponse(response);
    }

}
