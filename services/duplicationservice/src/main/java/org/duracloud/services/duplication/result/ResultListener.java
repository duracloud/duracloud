/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

/**
 * This interface defines the contract for listeners of duplication events.
 *
 * @author Andrew Woods
 *         Date: 9/14/11
 */
public interface ResultListener {

    /**
     * This method processes the arg duplication event.
     *
     * @param event to be processed
     */
    public void processResult(DuplicationEvent event);

}
