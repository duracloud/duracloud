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
public class AmazonEmailer implements Emailer {

    public AmazonEmailer(String username, String password, String fromAddress) {

    }

    @Override
    public void send(String subject, String body, String... recipients) {
        // Default method body

    }

    @Override
    public void sendAsHtml(String subject, String body, String... recipients) {
        // Default method body

    }
}
