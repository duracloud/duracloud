/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.client;

import static junit.framework.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.AclType;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.error.UnauthorizedException;
import org.duracloud.storage.provider.StorageProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class TestAnonymousAccess extends ClientTestBase {


    private final static String spacePrefix = "test-store-anon-";
    private final static String contentPrefix = "test-content-";
    private static String spaceId;
    private static String contentId;

    private static ContentStore store;


    @BeforeClass
    public static void beforeClass() throws Exception {
       
        ContentStoreManager storeManager = new ContentStoreManagerImpl(getHost(),
                                                                       getPort(),
                                                                       getContext());
        store = storeManager.getPrimaryContentStoreAsAnonymous();

        createSpace(getSpaceUrl());
        createContent(getContentUrl());
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
        assertFalse(allowed);
    }

    @Test
    public void testGetSpaceProperties() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getSpaceProperties(getSpaceId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
    }

    @Test
    public void testCreateSpace() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.createSpace("should-not-work");
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
        assertFalse(allowed);
    }

    @Test
    public void testGetContentProperties() throws ContentStoreException {
        boolean allowed = true;
        try {
            store.getContentProperties(getSpaceId(), getContentId());
        } catch (UnauthorizedException e) {
            allowed = false;
        }
        assertFalse(allowed);
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
    public void testSetContentProperties() throws ContentStoreException {
        boolean allowed = true;
        Map<String, String> properties = new HashMap<String, String>();
        properties.put("name-x", "value-x");
        try {
            store.setContentProperties(getSpaceId(), getContentId(), properties);
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
