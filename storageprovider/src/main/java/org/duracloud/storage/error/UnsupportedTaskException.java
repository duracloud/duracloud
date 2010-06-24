/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.error;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class UnsupportedTaskException extends TaskException {

    public UnsupportedTaskException(String task) {
        super(task + " is not a supported task");
    }

}