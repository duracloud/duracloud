/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.storage;

import org.duracloud.common.model.Credential;
import org.duracloud.emcstorage.ProbedEMCStorageProvider;
import org.duracloud.rackspacestorage.ProbedRackspaceStorageProvider;
import org.duracloud.s3storage.ProbedS3StorageProvider;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * This class runs a suite of tests on the StorageProvider interface across all
 * support provider implementations.
 *
 * @author Andrew Woods
 */
public class StorageProvidersTest {

    protected final static Logger log =
            LoggerFactory.getLogger(StorageProvidersTest.class);

    private final static StorageProvidersTestInterface tester =
            new StorageProvidersTestProxyPipe();

    private final static List<StorageProvider> storageProviders =
            new ArrayList<StorageProvider>();

    private final static List<String> spaceIds = new ArrayList<String>();

    private static String spaceId0;
    private static String spaceId1;

    @BeforeClass
    public static void beforeClass() throws StorageException {

        final int NUM_PROVIDERS = 3;
        for (StorageProviderType providerType : StorageProviderType.values()) {
            Credential credential = getCredential(providerType);
            if (credential != null) {
                String user = credential.getUsername();
                String pass = credential.getPassword();

                StorageProvider provider = null;
                if (StorageProviderType.AMAZON_S3.equals(providerType)) {
                    provider = new ProbedS3StorageProvider(user, pass);
                } else if (StorageProviderType.EMC.equals(providerType)) {
                    provider = new ProbedEMCStorageProvider(user, pass);
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

        Assert.assertEquals(NUM_PROVIDERS, storageProviders.size());
    }

    private static Credential getCredential(StorageProviderType type) {
        log.debug("Getting credential for: '" + type + "'");

        Credential credential = null;
        try {
            UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
            credential = dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                type));
            assertNotNull(credential);

            String username = credential.getUsername();
            String password = credential.getPassword();
            assertNotNull(username);
            assertNotNull(password);
        } catch (Exception e) {
            log.warn("Error getting credential for: '" + type + "'");
        }

        return credential;
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
                provider.setSpaceMetadata(spaceId,
                                          new HashMap<String, String>());
                provider.setSpaceAccess(spaceId,
                                        StorageProvider.AccessType.CLOSED);
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
    public void testGetSpaceMetadata() throws StorageException {
        log.info("testGetSpaceMetadata()");

        for (StorageProvider provider : storageProviders) {
            tester.testGetSpaceMetadata(provider, spaceId0);
        }
    }

    @Test
    public void testSetSpaceMetadata() throws StorageException {
        log.info("testSetSpaceMetadata()");

        for (StorageProvider provider : storageProviders) {
            tester.testSetSpaceMetadata(provider, spaceId0);
        }
    }

    @Test
    public void testGetSpaceAccess() throws StorageException {
        log.info("testGetSpaceAccess()");

        for (StorageProvider provider : storageProviders) {
            tester.testGetSpaceAccess(provider, spaceId0);
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
    public void testSetContentMetadata() throws StorageException {
        log.info("testSetContentMetadata()");
        String contentId0 = getNewContentId();
        String contentId1 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testSetContentMetadata(provider,
                                          spaceId0,
                                          spaceId1,
                                          contentId0,
                                          contentId1);
        }
    }

    @Test
    public void testGetContentMetadata() throws StorageException {
        log.info("testGetContentMetadata()");
        String contentId0 = getNewContentId();

        for (StorageProvider provider : storageProviders) {
            tester.testGetContentMetadata(provider, spaceId0, contentId0);
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