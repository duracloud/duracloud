/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.email;

/**
 * @author Andrew Woods
 *         Date: 3/11/11
 */
public interface EmailerFactory {

    /**
     * This method creates an Emailer which connects to an underlying email
     * provider
     *
     * @param username    for underlying email provider
     * @param password    for underlying email provider
     * @param fromAddress of all emails sent with resultant Emailer
     * @return Emailer
     */
    public Emailer createEmailer(String username,
                                 String password,
                                 String fromAddress);

}
