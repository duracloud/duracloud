/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.provider;

/**
 * @author: Bill Branan
 * Date: May 21, 2010
 */
public interface TaskRunner {

    public String getName();

    public String performTask(String taskParameters);
    
}
