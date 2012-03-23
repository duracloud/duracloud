/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit;

import org.duracloud.storage.aop.ContentMessage;

/**
 * This class defines the contract for the listener of system storage events.
 *
 * @author Andrew Woods
 *         Date: 3/19/12
 */
public interface AuditListener {

    /**
     * This method initializes the listener.
     */
    public void initialize();

    /**
     * This method is called by upon receipt of new events.
     *
     * @param message of event
     */
    public void onContentEvent(ContentMessage message);

    /**
     * This method notifies the listener to either stop or start writing events
     * for the arg spaceId.
     *
     * @param spaceId over which logging of audit events will be stopped/started
     * @param flag    indicating start or stop logging (true=start)
     */
    public void waitToWrite(String spaceId, boolean flag);

    /**
     * This method notifies the listener to stop logging audit events.
     */
    public void stop();

}
