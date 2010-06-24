/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class TestAnonymousAccess extends ClientTestBase {

    private static RestHttpHelper restHelper = getAuthorizedRestHelper();

    private final static String spacePrefix = "test-store-anon-";
    private final static String contentPrefix = "test-content-";
    private static String spaceId;
    private static String contentId;

    private static ContentStore store;


    @BeforeClass
    public static void beforeClass() throws Exception {
        StorageAccountTestUtil acctUtil = new StorageAccountTestUtil();
        acctUtil.initializeDurastore(getHost(), getPort(), getContext());

        ContentStoreManager storeManager = new ContentStoreManagerImpl(getHost(),
                                                                       getPort(),
                                                                       getContext());
        store = storeManager.getPrimaryContentStoreAsAnonymous();

        createSpace();
        createContent();
    }

    private static void createSpace() throws Exception {
        ClientTestBase.HttpCaller caller = new ClientTestBase.HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                String url = getSpaceUrl();
                String content = null;
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("x-dura-meta-" + ContentStore.SPACE_ACCESS,
                            ContentStore.AccessType.OPEN.name());
                return restHelper.put(url, content, headers);
            }
        };
        caller.makeCall(201);
    }

    private static void createContent() throws Exception {
        HttpCaller caller = new HttpCaller() {
            protected RestHttpHelper.HttpResponse call() throws Exception {
                String url = getContentUrl();
                Map<String, String> headers = null;
                return restHelper.put(url, "hello", headers);
            }
        };
        caller.makeCall(201);
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

    @Test
    public void testGetSpaces() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getSpaces();
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertTrue(allowed);
    }

    @Test
    public void testGetSpace() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getSpace(getSpaceId(), null, 0, null);
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertTrue(allowed);
    }

    @Test
    public void testGetSpaceMetadata() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getSpaceMetadata(getSpaceId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertTrue(allowed);
    }

    @Test
    public void testCreateSpace() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.createSpace("should-not-work", null);
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    @Test
    public void testSetSpaceMetadata() throws ContentStoreException {
        boolean allowed = true;
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("name-x", "value-x");

        try {
            store.setSpaceMetadata(getSpaceId(), metadata);
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    @Test
    public void testDeleteSpace() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.deleteSpace(getSpaceId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    @Test
    public void testGetContent() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getContent(getSpaceId(), getContentId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertTrue(allowed);
    }

    @Test
    public void testGetContentMetadata() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getContentMetadata(getSpaceId(), getContentId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertTrue(allowed);
    }

    @Test
    public void testStoreContent() throws ContentStoreException {
        boolean allowed = true;
        String data = "hello";
        InputStream content = new ByteArrayInputStream(data.getBytes());
        try {
            store.addContent(getSpaceId(),
                             "should-not-work",
                             content,
                             data.length(),
                             null,
                             null,
                             null);
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    @Test
    public void testSetContentMetadata() throws ContentStoreException {
        boolean allowed = true;
        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put("name-x", "value-x");
        try {
            store.setContentMetadata(getSpaceId(), getContentId(), metadata);
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    @Test
    public void testDeleteContent() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.deleteContent(getSpaceId(), getContentId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    private static String getSpaceId() {
        if (null == spaceId) {
            Random r = new Random();
            spaceId = spacePrefix + r.nextInt(10000);
        }
        return spaceId;
    }

    private static String getContentId() {
        if (null == contentId) {
            Random r = new Random();
            contentId = contentPrefix + r.nextInt(10000);
        }
        return contentId;
    }

    private static String getSpaceUrl() throws Exception {
        return getBaseUrl() + "/" + getSpaceId();
    }

    private static String getContentUrl() throws Exception {
        return getSpaceUrl() + "/" + getContentId();
    }

}
