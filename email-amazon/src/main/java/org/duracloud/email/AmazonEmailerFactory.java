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
public class AmazonEmailerFactory implements EmailerFactory {

    @Override
    public Emailer createEmailer(String username,
                                 String password,
                                 String fromAddress) {
        // Default method body
        return null;
    }
}
