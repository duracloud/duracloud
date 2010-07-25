/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.duradmin.mock.contentstore.MockContentStoreManagerFactoryImpl;
import org.junit.After;
import org.junit.Before;

public class ContentStoreProviderTestBase {

    protected ContentStoreProvider contentStoreProvider;

    @Before
    public void setUp() throws Exception {
        ContentStoreManager contentStoreManager = new MockContentStoreManagerFactoryImpl()
            .create();
        ContentStoreSelector contentStoreSelector = new ContentStoreSelector();
        this.contentStoreProvider = new ContentStoreProvider(contentStoreManager,
                                                             contentStoreSelector);
    }

    @After
    public void tearDown() throws Exception {
    }
}
