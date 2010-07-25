/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.contentstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class ContentItemListTest
        extends ContentStoreProviderTestBase {

    private ContentItemList list = null;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String spaceId =
                this.contentStoreProvider.getContentStore().getSpaces().get(0);
        this.list = new ContentItemList(spaceId, this.contentStoreProvider.getContentStore());
    }

    @Test
    public void testGetSpace() {
        assertNotNull(this.list.getSpace());
    }

    public void testSmokeTest() {
        this.list.setMaxResultsPerPage(5);
        assertTrue(this.list.isNextAvailable());
        assertFalse(this.list.isPreviousAvailable());
        assertEquals(5, this.list.getResultList().size());
        this.list.next();
        assertTrue(this.list.isPreviousAvailable());
        this.list.previous();
        assertTrue(this.list.isNextAvailable());
        assertFalse(this.list.isPreviousAvailable());
    }

}
