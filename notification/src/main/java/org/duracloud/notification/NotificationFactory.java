/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.notification;

/**
 * @author Andrew Woods
 *         Date: 3/11/11
 */
public interface NotificationFactory {

    /**
     * This method creates an Emailer which connects to an underlying notification
     * provider
     *
     * @param fromAddress of all emails sent with resultant Emailer
     * @return Emailer
     */
    public Emailer getEmailer(String fromAddress);

}
