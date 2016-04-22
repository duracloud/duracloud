/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.client;

import java.util.List;
import java.util.Random;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.domain.Space;
import org.duracloud.error.ContentStoreException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Aug 30, 2010
 */
public class TestContentIterator extends ClientTestBase {

    private final static String spacePrefix = "test-content-itr-";
    private final static String contentPrefix = "test-content-";

    private final static int NUM_ITEMS = 10;

    private static ContentStore store;
    private static String spaceId;

    static {
        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "test-content-itr-space-" + random;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        ContentStoreManager storeManager = new ContentStoreManagerImpl(getHost(),
                                                                       getPort(),
                                                                       getContext());
        storeManager.login(getRootCredential());
        store = storeManager.getPrimaryContentStore();

        createSpace(getSpaceUrl());
        for (int i = 0; i < NUM_ITEMS; ++i) {
            createContent(getContentUrl(i));
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
        // delete test space
        HttpCaller caller = new HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                return restHelper.delete(getSpaceUrl());
            }
        };
        caller.makeCall(200);
    }

    private static String getSpaceId() {
        if (null == spaceId) {
            Random r = new Random();
            spaceId = spacePrefix + r.nextInt(10000);
        }
        return spaceId;
    }

    private static String getContentId(int i) {
        return contentPrefix + i;
    }

    private static String getSpaceUrl() throws Exception {
        return getBaseUrl() + "/" + getSpaceId();
    }

    private static String getContentUrl(int i) throws Exception {
        return getSpaceUrl() + "/" + getContentId(i);
    }

    @Test
    public void testIterator() throws ContentStoreException {
        String marker = null;
        long maxItems = NUM_ITEMS / 3;
        long count = 0;

        List<String> contentIds = getContentIds(maxItems, marker);
        while (contentIds != null && contentIds.size() > 0) {
            count += contentIds.size();

            int index = contentIds.size() - 1;
            marker = contentIds.get(index);

            contentIds = getContentIds(maxItems, marker);
        }

        Assert.assertEquals(NUM_ITEMS, count);
    }

    private List<String> getContentIds(long maxItems, String marker)
        throws ContentStoreException {
        Space space = store.getSpace(getSpaceId(), null, maxItems, marker);
        Assert.assertNotNull(space);

        List<String> contentIds = space.getContentIds();
        Assert.assertNotNull(contentIds);
        
        int size = contentIds.size();
        Assert.assertTrue("size: " + size, size <= maxItems);
        return contentIds;
    }

}
