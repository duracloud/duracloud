/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.changenotifier;

/**
 * Defines interface for notifying listeners of account change events.
 *
 * @author Daniel Bernstein
 */
public interface AccountChangeNotifier {

    /**
     * Notifies listeners that an account had changed.
     *
     * @param account
     */
    public void accountChanged(String account);

    /**
     * Notifies listeners that one or more storage providers associated with an account has changed.
     *
     * @param account
     */
    public void storageProvidersChanged(String account);

    /**
     * Notifies listeners that the set of users associated with an account have changed
     *
     * @param account
     */
    public void userStoreChanged(String account);

    /**
     * Notifies listeners that a root user's status has changed:  a root user was added, removed, or changed.
     */
    public void rootUsersChanged();

    /**
     * Notifies listeners that a node's cached storage provider information has changed.
     *
     * @param account
     */
    void storageProviderCacheOnNodeChanged(String account);

}
