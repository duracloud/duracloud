/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sdscstorage;

import junit.framework.Assert;
import org.duracloud.common.model.Credential;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is used to test the SDSCStorageProvider GET calls.
 *
 * @author Andrew Woods
 *         Date: Oct 07, 2011
 */
public class SDSCStorageProviderGetTestNotRun {

    private final Logger log = LoggerFactory.getLogger(
        SDSCStorageProviderGetTestNotRun.class);

    private SDSCStorageProvider sdscProvider = null;

    private String spaceId = "integration-test-space-id";
    private String contentId = "content-id";
    private int attempts = 10;

    @Before
    public void setUp() throws Exception {
        if (null == sdscProvider) {
            Credential credential = getCredential();
            Assert.assertNotNull(credential);

            String username = credential.getUsername();
            String password = credential.getPassword();
            Assert.assertNotNull(username);
            Assert.assertNotNull(password);

            sdscProvider = new SDSCStorageProvider(username, password);
        }
    }

    private Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        return dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
            StorageProviderType.SDSC));
    }

    @Test
    public void testCreateSpace() {
        String spaceId = getSpaceId();
        boolean exists = false;

        Iterator<String> spaces = sdscProvider.getSpaces();
        while (spaces.hasNext()) {
            if (spaceId.equals(spaces.next())) {
                exists = true;
            }
        }

        try {
            sdscProvider.createSpace(spaceId);
            Assert.assertFalse(exists);

        } catch (StorageException e) {
            log.info("space already exists: {}", spaceId);
            Assert.assertTrue(exists);
        }
    }

    @Test
    public void testGetSpaces() {
        log.info("--- TEST GET SPACES ---");

        int failures = 0;
        Iterator<String> spaces = null;
        for (int i = 0; i < attempts; i++) {
            try {
                spaces = sdscProvider.getSpaces();

            } catch (Exception e) {
                log.info("Failure getting spaces: {}", e.getMessage());
                failures++;
            }
        }

        log.info("TEST GET SPACES RESULT: {} failures out of {} attempts.",
                 failures,
                 attempts);

        Assert.assertNotNull(spaces);
        while (spaces.hasNext()) {
            log.info("space: {}", spaces.next());
        }
    }

   @Test
    public void testGetSpaceProperties() {
        String spaceId = getSpaceId();

        Map<String, String> props = sdscProvider.getSpaceProperties(spaceId);
        Assert.assertNotNull(props);
        log.info("space props: {}", props);
    }

    @Test
    public void testAddContent() throws IOException {
        String text = "hello";

        String spaceId = getSpaceId();
        String contentId = getContentId();
        String contentMimeType = "text/plain";
        long contentSize = text.length();
        String contentChecksum = null;
        InputStream content = getStream(text);

        String md5 = null;
        try {
            md5 = sdscProvider.addContent(spaceId,
                                          contentId,
                                          contentMimeType,
                                          null,
                                          contentSize,
                                          contentChecksum,
                                          content);
        } catch (Exception e) {
            log.error("Unexpected exception: {}, ", e);

        } finally {
            content.close();
        }

        log.info("content md5 = {}", md5);
    }

    private InputStream getStream(String text) {
        return new ByteArrayInputStream(text.getBytes());
    }

    @Test
    public void testGetSpaceContents() {
        log.info("--- TEST GET SPACE CONTENTS ---");
        String spaceId = getSpaceId();

        int failures = 0;
        Iterator<String> contents = null;
        for (int i = 0; i < attempts; i++) {
            try {
                contents = sdscProvider.getSpaceContents(spaceId, null);

            } catch (Exception e) {
                log.info("Failure getting space contents: {}", e.getMessage());
                failures++;
            }
        }
        
        log.info(
            "TEST GET SPACE CONTENTS RESULT: {} failures  out of {} attempts.",
            failures,
            attempts);

        Assert.assertNotNull(contents);
        while (contents.hasNext()) {
            log.info("content: {}", contents.next());
        }
    }

    @Test
    public void testSetContentProperties() {
        String spaceId = getSpaceId();
        String contentId = getContentId();
        Map<String, String> props = new HashMap<String, String>();
        props.put("color", "green");
        props.put("state", "va");

        sdscProvider.setContentProperties(spaceId, contentId, props);
    }

    @Test
    public void testGetContentProperties() {
        log.info("--- TEST GET CONTENT PROPERTIES ---");
        String spaceId = getSpaceId();
        String contentId = getContentId();

        int failures = 0;
        Map<String, String> props = null;
        for (int i = 0; i < attempts; i++) {
            try {
                props = sdscProvider.getContentProperties(spaceId, contentId);

            } catch (Exception e) {
                log.info("Failure getting object properties: {}", e);
                failures++;
            }
        }

        log.info(
            "TEST GET CONTENT PROPERTIES RESULT: {} failures  out of {} attempts.",
            failures,
            attempts);

        Assert.assertNotNull(props);
        log.info("props: {}", props);
    }

    @Test
    public void testDeleteContent() {
        String spaceId = getSpaceId();
        String contentId = getContentId();

        sdscProvider.deleteContent(spaceId, contentId);
    }

    //@Test
    public void testDeleteSpace() {
        String spaceId = getSpaceId();
        sdscProvider.deleteSpace(spaceId);
    }

    private String getSpaceId() {
        return spaceId;
    }

    private String getContentId() {
        return contentId;
    }

}
