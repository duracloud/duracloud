/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.util;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;

/**
 * @author: Bill Branan
 * Date: Nov 5, 2010
 */
public class StoreClientUtil {

    public ContentStore createContentStore(String host,
                                           int port,
                                           String context,
                                           String username,
                                           String password,
                                           String storeId) {
        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(host, String.valueOf(port), context);
        storeManager.login(new Credential(username, password));

        ContentStore contentStore;
        try {
            if(storeId != null) {
                contentStore = storeManager.getContentStore(storeId);
            } else {
                contentStore = storeManager.getPrimaryContentStore();
            }
        } catch(ContentStoreException e) {
            String prefix = "Could not create connection to DuraCloud (" + host + ":" + port + "/"+context + "). Cause: ";
            if(e.getMessage().contains("Response code was 401")) {
                throw new RuntimeException(prefix + "invalid credentials. " +
                    "Check your username and password and try again.");
            } else {
                throw new RuntimeException(prefix + e.getMessage(), e);
            }
        }

        return contentStore;
    }
}
