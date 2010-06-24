/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

/**
 * @author Andrew Woods
 *         Date: Mar 25, 2010
 */
public interface Securable {

    /**
     * This method supplies user credentials to the application.
     *
     * @param credential of user
     */
    public void login(Credential credential);

    /**
     * This method clears any previously logged-in credentials.
     */
    public void logout();
}
