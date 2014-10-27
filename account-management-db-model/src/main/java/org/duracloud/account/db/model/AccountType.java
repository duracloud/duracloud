/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.account.db.model;

/**
 * Defines the types of DuraCloud accounts that are available for subscription.
 *
 * @author: Bill Branan
 * Date: 2/9/12
 */
public enum AccountType {

    FULL("Full DuraCloud Account"),
    COMMUNITY("DuraCloud Community Account");

    private final String text;

    private AccountType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return name();
    }

    @Override
    public String toString() {
        return name();
    }

}