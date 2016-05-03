/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.durastore.storage;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfig;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.integration.durastore.storage.probe.ProbedRackspaceStorageProvider;
import org.duracloud.integration.durastore.storage.probe.ProbedS3StorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class runs a suite of tests on the StorageProvider interface across all
 * support provider implementations.
 *
 * @author Andrew Woods
 */
public class TestStorageProviders {

    protected final static Logger log =
            LoggerFactory.getLogger(TestStorageProviders.class);

    private final static StorageProvidersTestInterface tester =
            new StorageProvidersTestProxyPipe();

    private final static List<StorageProvider> storageProviders =
            new ArrayList<StorageProvider>();

    private final static List<String> spaceIds = new ArrayList<String>();

    private static String spaceId0;
    private static String spaceId1;

    @BeforeClass
    public static void beforeClass() throws StorageException {

        for (StorageProviderType providerType : StorageProviderType.values()) {
            Credential credential = getCredential(providerType);
            if (credential != null) {
                String user = credential.getUsername();
                String pass = credential.getPassword();

                StorageProvider provider = null;
                if (StorageProviderType.AMAZON_S3.equals(providerType)) {
                    provider = new ProbedS3StorageProvider(user, pass);
                } else if (StorageProviderType.RACKSPACE.equals(providerType)) {
                    provider = new ProbedRackspaceStorageProvider(user, pass);
                } else {
                    StringBuffer sb = new StringBuffer("NOT TESTING ");
                    sb.append("storage-provider: '" + providerType + "'");
                    log.info(sb.toString());
                }

                if (provider != null) {
                    storageProviders.add(provider);
                }
            }
        }

        Assert.assertTrue(storageProviders.size() > 0);
    }

    private static Credential getCredential(StorageProviderType type) {
        log.debug("Getting credential for: '" + type + "'");
        TestConfig config;
        try {
            config = new TestConfigUtil().getTestConfig();
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
        SimpleCredential credential = null;
        for(StorageProviderCredential spc : config.getProviderCredentials()){
            if(spc.getType().name().equals(type.name())){
                credential = spc.getCredential();
                break;
            }
        }
        
        try {
            assertNotNull(credential);

            String username = credential.getUsername();
            String password = credential.getPassword();
            assertNotNull(username);
            assertNotNull(password);
            return new Credential(username, password);
        } catch (Throwable e) {
            log.warn("Error getting credential for: '" + type + "'");
        }

        return null;
    }

    @Before
    public void setUp() {
        checkSpacesCreated();
        try {
            cleanSpaces();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    private void checkSpacesCreated() {
        if(spaceIds.size() <= 0) {
            spaceId0 = getNewSpaceId();
            spaceId1 = getNewSpaceId();
            spaceIds.add(spaceId0);
            spaceIds.add(spaceId1);

            for (StorageProvider provider : storageProviders) {
                log.info("Creating spaces for provider " + provider.getClass());
                for (String spaceId : spaceIds) {
                    log.info("Creating space: " + spaceId);
                    tester.testCreateSpace(provider, spaceId);
                }
                log.info("Creating spaces complete for " + provider.getClass());
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        try {
            cleanSpaces();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }

    @AfterClass
    public static void afterClass() {
        try {
            removeSpaces();
        } catch (Exception e) {
            log.info(e.getMessage());
        }

        tester.close();
    }

    private static void cleanSpaces() {
        for (StorageProvider provider : storageProviders) {
            assertNotNull(provider);
            for(String spaceId : spaceIds) {
                Iterator<String> contentIds =
                    provider.getSpaceContents(spaceId, null);
                while(contentIds.hasNext()) {
                    String contentId = contentIds.next();
                    provider.deleteContent(spaceId, contentId);
                }
            }
        }
    }

    private static void removeSpaces() {
        for (StorageProvider provider : storageProviders) {
            assertNotNull(provider);
            log.info("Removing spaces for provider " + provider.getClass());
            for(String spaceId : spaceIds) {
                log.info("Removing space: " + spaceId);
                tester.testDeleteSpace(provider, spaceId);
            }
            log.info("Removing spaces complete for " + provider.getClass());
        }
    }

    private static String getNewSpaceId() {
        String random = String.valueOf(new Random().nextInt(99999));
        return "storage-providers-test-space-" + random;
    }

    private String getNewContentId() {
        String random = String.valueOf(new Random().nextInt(99999));
        return "storage-providers-test-content-" + random;
    }

    @Test
    public void testGetSpaces() throws StorageException {
        log.info("testGetSpaces()");
        for (StorageProvider provider : storageProviders) {
            tester.testGetSpaces(provider, spaceId0, spaceId1);
        }
    }

    @Test
    public void testGetSpaceContents() throws StorageException {
        log.info("testGetSpaceContents()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testGetSpaceContents(provider,
                                        spaceId0,
                                        contentId0,
                                        contentId1);
        }
    }

    @Test
    public void testGetSpaceProperties() throws StorageException {
        log.info("testGetSpaceProperties()");

        for (StorageProvider provider : storageProviders) {
            tester.testGetSpaceProperties(provider, spaceId0);
        }
    }

    @Test
    public void testAddAndGetContent() throws Exception {
        log.info("testAddAndGetContent()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();
        String contentId2 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testAddAndGetContent(provider,
                                        spaceId0,
                                        contentId0,
                                        contentId1,
                                        contentId2);
        }
    }

    @Test
    public void testAddAndGetContentOverwrite() throws Exception {
        log.info("testAddAndGetContentOverwrite()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testAddAndGetContentOverwrite(provider,
                                                 spaceId0,
                                                 contentId0,
                                                 contentId1);
        }
    }

    @Test
    public void testAddContentLarge() throws Exception {
        log.info("testAddContentLarge()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testAddContentLarge(provider,
                                       spaceId0,
                                       contentId0,
                                       contentId1);
        }
    }

    @Test
    public void testDeleteContent() throws StorageException {
        log.info("testDeleteContent()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testDeleteContent(provider,
                                     spaceId0,
                                     contentId0,
                                     contentId1);
        }
    }

    @Test
    public void testSetContentProperties() throws StorageException {
        log.info("testSetContentProperties()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testSetContentProperties(provider,
                                            spaceId0,
                                            spaceId1,
                                            contentId0,
                                            contentId1);
        }
    }

    @Test
    public void testGetContentProperties() throws StorageException {
        log.info("testGetContentProperties()");
        String contentId0 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testGetContentProperties(provider, spaceId0, contentId0);
        }
    }

    // TODO: need to implement
    @Test
    public void testSpaceAccess() throws Exception {

    }

    // TODO: need to implement
    @Test
    public void testContentAccess() throws Exception {

    }

}