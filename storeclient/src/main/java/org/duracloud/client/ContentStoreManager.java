/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import java.util.Map;

import org.duracloud.common.model.Securable;
import org.duracloud.error.ContentStoreException;

/**
 * Provides facilities for connecting to a set of content stores
 *
 * @author Bill Branan
 */
public interface ContentStoreManager extends Securable {

    /**
     * Gets all available content stores.
     * Each content store uses the default number of retries (3) on call failure.
     *
     * @return a map of content stores to content store IDs
     * @throws ContentStoreException if the content store list cannot be retrieved
     */
    public Map<String, ContentStore> getContentStores()
        throws ContentStoreException;

    /**
     * Gets all available content stores.
     *
     * @param maxRetries number of retries to perform if a content store call fails
     * @return a map of content stores to content store IDs
     * @throws ContentStoreException if the content store list cannot be retrieved
     */
    public Map<String, ContentStore> getContentStores(int maxRetries)
        throws ContentStoreException;

    /**
     * Gets a specific content store based on ID.
     * Content store uses the default number of retries (3) on call failure.
     *
     * @param storeID the ID of a particular content store
     * @return the ContentStore mapped to storeID
     * @throws ContentStoreException if the content store cannot be retrieved
     */
    public ContentStore getContentStore(String storeID)
        throws ContentStoreException;

    /**
     * Gets a specific content store based on ID.
     *
     * @param storeID    the ID of a particular content store
     * @param maxRetries number of retries to perform if a content store call fails
     * @return the ContentStore mapped to storeID
     * @throws ContentStoreException if the content store cannot be retrieved
     */
    public ContentStore getContentStore(String storeID, int maxRetries)
        throws ContentStoreException;

    /**
     * Gets the primary content store.
     * Content store uses the default number of retries (3) on call failure.
     *
     * @return the primary ContentStore
     * @throws if the content store cannot be retrieved
     */
    public ContentStore getPrimaryContentStore()
        throws ContentStoreException;

    /**
     * Gets the primary content store.
     *
     * @param maxRetries number of retries to perform if a content store call fails
     * @return the primary ContentStore
     * @throws if the content store cannot be retrieved
     */
    public ContentStore getPrimaryContentStore(int maxRetries)
        throws ContentStoreException;

    /**
     * Provides access to the primary content store without requiring login().
     * Only certain ContentStore activities are permitted to anonymous users,
     * primarily getting content from open spaces.
     *
     * Content store uses the default number of retries (3) on call failure.
     *
     * @return the primary ContentStore
     * @throws if the content store cannot be retrieved
     */
    public ContentStore getPrimaryContentStoreAsAnonymous()
        throws ContentStoreException;

    /**
     * Provides access to the primary content store without requiring login().
     * Only certain ContentStore activities are permitted to anonymous users,
     * primarily getting content from open spaces.
     *
     * @param maxRetries number of retries to perform if a content store call fails
     * @return the primary ContentStore
     * @throws if the content store cannot be retrieved
     */
    public ContentStore getPrimaryContentStoreAsAnonymous(int maxRetries)
        throws ContentStoreException;

    /**
     * <p>reconfigure</p>
     *
     * @param host    of durastore
     * @param port    of durastore
     * @param context of durastore
     * @throws ContentStoreException
     */
    public void reinitialize(String host, String port, String context)
        throws ContentStoreException;

}
