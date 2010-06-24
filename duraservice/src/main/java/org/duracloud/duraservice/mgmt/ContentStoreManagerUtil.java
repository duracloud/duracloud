/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraservice.mgmt;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.duraservice.domain.ServiceStore;
import org.duracloud.duraservice.domain.Store;
import org.duracloud.security.context.SecurityContextUtil;
import org.duracloud.security.error.NoUserLoggedInException;

/**
 * This class helps to create ContentStoreManagers that are logged-in with the
 * current session's user credentials.
 *
 * @author Andrew Woods
 *         Date: Mar 30, 2010
 */
public class ContentStoreManagerUtil {
    private SecurityContextUtil securityContextUtil;

    public ContentStoreManagerUtil(SecurityContextUtil securityContextUtil) {
        this.securityContextUtil = securityContextUtil;
    }

    /**
     * This method creates a ContentStoreManager for the arg store logged-in
     * with the credentials of the session's current user.
     *
     * @param store for which manager is requested
     * @return ContentStoreManager for arg store
     */
    public ContentStoreManager getContentStoreManager(Store store) {
        return getContentStoreManager(store, getCurrentUser());
    }

    public ContentStoreManager getContentStoreManager(Store store,
                                                      Credential credential) {
        ContentStoreManager storeManager;
        storeManager = new ContentStoreManagerImpl(store.getHost(),
                                                   store.getPort(),
                                                   store.getContext());
        storeManager.login(credential);
        return storeManager;
    }

    /**
     * @return credential of current session's user.
     */
    public Credential getCurrentUser() {
        try {
            return securityContextUtil.getCurrentUser();

        } catch (NoUserLoggedInException e) {
            return new Credential("unknown", "unknown");
        }
    }
}
