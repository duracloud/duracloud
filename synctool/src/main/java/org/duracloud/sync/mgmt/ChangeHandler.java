/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

/**
  * @author: Bill Branan
 * Date: Mar 17, 2010
 */
public interface ChangeHandler {

    /**
     * Tells the handler that a file has changed
     *
     * @param changedFile a file which has changed
     * @returns true if handling was successful, false otherwise
     */
    public boolean handleChangedFile(ChangedFile changedFile);

}
