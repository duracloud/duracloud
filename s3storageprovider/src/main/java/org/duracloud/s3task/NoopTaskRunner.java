/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3task;

import org.duracloud.storage.provider.TaskRunner;

/**
 * This task does not actually do anything, but it does allow for tests to
 * ensure that task execution is operational.
 *
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public class NoopTaskRunner implements TaskRunner {

    private static final String TASK_NAME = "noop";

    public String getName() {
        return TASK_NAME;
    }

    public String performTask(String taskParameters) {
        return "Success";
    }

}