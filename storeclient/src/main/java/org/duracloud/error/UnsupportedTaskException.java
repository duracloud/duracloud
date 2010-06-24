/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.error;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class UnsupportedTaskException extends ContentStoreException {

    private static final String messageKey = "duracloud.error.durastore.task";

    public UnsupportedTaskException(String taskName, Throwable t) {
        super("Task " + taskName + "is not supported", t, messageKey);
        setArgs(taskName, t.getMessage());
    }

}