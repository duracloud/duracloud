/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb;

import org.duracloud.common.model.Credential;
import org.duracloud.common.util.DatabaseUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestPasswordRepositoryDBImpl {

    private final DatabaseUtil dbUtil;

    private final String baseDir = "testUnitDB";

    private final Credential dbCred = new Credential("duracloud", "duracloud");

    private final String bootPassword = "bxxtPassword";

    private PasswordRepositoryDBImpl repo;

    private final String tablename =
            PasswordRepositoryDBImpl.getTableSpec().getTableName();

    private ResourceType resource;

    private final String username = "username";

    private final String password = "paxxword";

    public TestPasswordRepositoryDBImpl()
            throws Exception {
        dbUtil = new UnitTestDatabaseUtil(dbCred, baseDir, bootPassword);
        dbUtil.initializeDB();
    }

    @Before
    public void setUp() throws Exception {
        resource = getResource(StorageProviderType.EMC);

        repo = new PasswordRepositoryDBImpl();
        repo.setDataSource(dbUtil.getDataSource());
    }

    @After
    public void tearDown() throws Exception {
        repo = null;
        dbUtil.clearDB();
    }

    @Test
    public void testFindPassword() throws Exception {
        verifyTableSize(0);
        insertTestData(3);
        verifyTableSize(3);

        repo.insertPassword(resource, username, password);

        verifyTableSize(4);

        String pword =
                repo.findPasswordByResourceTypeAndUsername(resource, username);
        assertNotNull(pword);
        assertEquals(pword, password);
    }

    @Test
    public void testFindCredential() throws Exception {
        verifyTableSize(0);
        repo.insertPassword(getResource(StorageProviderType.AMAZON_S3),
                            username + 0,
                            bootPassword + 0);
        repo.insertPassword(getResource(StorageProviderType.MICROSOFT_AZURE),
                            username + 1,
                            bootPassword + 1);
        repo.insertPassword(getResource(StorageProviderType.RACKSPACE),
                            username + 2,
                            bootPassword + 2);
        verifyTableSize(3);

        repo.insertPassword(resource, username, password);
        verifyTableSize(4);

        Credential cred = repo.findCredentialByResourceType(resource);
        assertNotNull(cred);
        Assert.assertEquals(cred.getUsername(), username);
        Assert.assertEquals(cred.getPassword(), password);
    }

    @Test
    public void testDuplicateFindCredential() throws Exception {
        verifyTableSize(0);
        repo.insertPassword(resource,
                            username + 0,
                            bootPassword + 0);
        repo.insertPassword(resource,
                            username + 1,
                            bootPassword + 1);
        verifyTableSize(2);

        Credential cred = null;
        try {
            cred = repo.findCredentialByResourceType(resource);
            Assert.fail("Should have thrown exception.");
        } catch (Exception e) {
        }
        Assert.assertEquals(cred, null);
    }

    @Test
    public void testMissingFindCredential() throws Exception {
        verifyTableSize(0);
        repo.insertPassword(getResource(StorageProviderType.AMAZON_S3),
                            username + 0,
                            bootPassword + 0);
        repo.insertPassword(getResource(StorageProviderType.MICROSOFT_AZURE),
                            username + 1,
                            bootPassword + 1);
        repo.insertPassword(getResource(StorageProviderType.RACKSPACE),
                            username + 2,
                            bootPassword + 2);
        verifyTableSize(3);

        Credential cred = null;
        try {
            cred = repo.findCredentialByResourceType(resource);
            Assert.fail("Should have thrown exception.");
        } catch (Exception e) {
        }
        Assert.assertEquals(cred, null);
    }

    @Test
    public void testBadRetrieval() throws Exception {
        verifyTableSize(0);
        insertTestData(2);
        verifyTableSize(2);

        String pword = null;
        try {
            pword =
                    repo.findPasswordByResourceTypeAndUsername(resource,
                                                               "bad-password");
            Assert.fail("Should throw exception.");
        } catch (Exception e) {
        }
        assertTrue(pword == null);
    }

    @SuppressWarnings("unchecked")
    private void verifyTableSize(int size) {
        List results =
                dbUtil.getOps().queryForList("SELECT * FROM " + tablename);
        Assert.assertNotNull(results);
        Assert.assertTrue(results.size() == size);
    }

    private void insertTestData(int size) {
        for (int i = 0; i < size; ++i) {
            dbUtil.getOps().update("INSERT INTO " + tablename
                    + " (providerType,username,password) VALUES (" + "'"
                    + resource + "','" + username + i + "','" + password + i
                    + "')");
        }
    }

    private ResourceType getResource(StorageProviderType type) {
        return ResourceType.fromStorageProviderType(type);
    }

}
