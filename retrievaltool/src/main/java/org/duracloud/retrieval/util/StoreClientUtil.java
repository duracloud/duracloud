/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.util;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;

/**
 * @author: Bill Branan
 * Date: Oct 15, 2010
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
            throw new RuntimeException("Could not create connection to " +
                "DuraStore due to " + e.getMessage(), e);
        }

        return contentStore;
    }

}
