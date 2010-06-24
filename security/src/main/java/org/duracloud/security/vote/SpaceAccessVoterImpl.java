/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.vote;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.error.ContentStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andrew Woods
 *         Date: Mar 12, 2010
 */
public class SpaceAccessVoterImpl extends SpaceAccessVoter {
    private final Logger log = LoggerFactory.getLogger(SpaceAccessVoterImpl.class);

    protected ContentStore getContentStore(String host,
                                           String port,
                                           String storeId) {
        // FIXME: temporary handling of https requests
        if (port != null && port.equals("443")) {
            port = "80";
        }

        ContentStoreManager storeMgr = new ContentStoreManagerImpl(host, port);
        storeMgr.login(new SystemUserCredential());
        ContentStore store = null;
        try {
            if (null == storeId) {
                store = storeMgr.getPrimaryContentStore();
            } else {
                store = storeMgr.getContentStore(storeId);
            }
        } catch (ContentStoreException e) {
            log.warn("Unable to get content-store: " + e.getMessage());
        } finally {
            storeMgr.logout();
        }
        return store;
    }
}
