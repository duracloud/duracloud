/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.common.model.Securable;
import org.duracloud.error.ContentStoreException;

import java.util.Map;

/**
 * Provides facilities for connecting to a set of content stores
 *
 * @author Bill Branan
 */
public interface ContentStoreManager extends Securable {

    /**
     * <p>getContentStores</p>
     *
     * @return a map of content stores to content store IDs
     * @throws ContentStoreException if the content store list cannot be retrieved
     */
    public Map<String, ContentStore> getContentStores()
        throws ContentStoreException;
    
    /**
     * <p>getContentStore</p>
     *
     * @param storeID the ID of a particular content store
     * @return the ContentStore mapped to storeID
     * @throws ContentStoreException if the content store cannot be retrieved
     */
    public ContentStore getContentStore(String storeID)
        throws ContentStoreException;
    
    /**
     * <p>getPrimaryContentStore</p>
     *
     * @return the primary ContentStore
     * @throws if the content store cannot be retrieved
     */
    public ContentStore getPrimaryContentStore()
        throws ContentStoreException;

    /**
     * Provides access to the primary content store without requiring login().
     * Only certain ContentStore activities are permitted to anonymous users,
     * primarily getting content from open spaces.
     *
     * <p>getPrimaryContentStoreAsAnonymous</p>
     *
     * @return the primary ContentStore
     * @throws if the content store cannot be retrieved
     */
    public ContentStore getPrimaryContentStoreAsAnonymous()
        throws ContentStoreException;

    /**
     * <p>reconfigure</p>
     * @param host of durastore
     * @param port of durastore
     * @param context of durastore
     * @throws ContentStoreException
     */
    public void reinitialize(String host, String port, String context)
        throws ContentStoreException;
}
