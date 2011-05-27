/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import junit.framework.Assert;
import org.duracloud.common.model.Credential;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.Before;
import org.junit.Test;

/**
 * This class is used to test the Rackspace GET API calls.
 *
 * @author: Bill Branan
 * Date: May 27, 2011
 */
public class RackspaceGetTestNotRun {

    private static FilesClient filesClient = null;
    private int attempts = 10;

    @Before
    public void setUp() throws Exception {
        if(null == filesClient) {
            Credential rackspaceCredential = getCredential();
            Assert.assertNotNull(rackspaceCredential);

            String username = rackspaceCredential.getUsername();
            String password = rackspaceCredential.getPassword();
            Assert.assertNotNull(username);
            Assert.assertNotNull(password);

            filesClient = new FilesClient(username, password);
            if (!filesClient.login()) {
                throw new Exception("Login to Rackspace failed");
            }
        }
    }

    private Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        return dbUtil.findCredentialForResource(
            ResourceType.fromStorageProviderType(
            StorageProviderType.RACKSPACE));
    }

    @Test
    public void testListContainers() {
        System.out.println("--- TEST LIST CONTAINERS ---");
        int failures = 0;
        for(int i=0; i < attempts; i++) {
            try {
                filesClient.listContainers();
            } catch (Exception e) {
                System.out.println("Failure listing containers: " +
                                       e.getMessage());
                failures++;
            }
        }
        System.out.println("TEST LIST CONTAINERS RESULT: " + failures +
                           " failures " + " out of " + attempts + " attempts.");
    }

    @Test
    public void testListObjects() {
        System.out.println("--- TEST LIST OBJECTS ---");
        String containerName = getContainerName();

        int failures = 0;
        for(int i=0; i < attempts; i++) {
            try {
                filesClient.listObjects(containerName, null, 1000, null);
            } catch (Exception e) {
                System.out.println("Failure listing objects: " +
                                   e.getMessage());
                failures++;
            }
        }
        System.out.println("TEST LIST OBJECTS RESULT: " + failures +
                           " failures " + " out of " + attempts + " attempts.");
    }

    private String getContainerName() {
        for(int i=0; i<10; i++) {
            try {
                return filesClient.listContainers().get(0).getName();
            } catch(Exception e) {
            }
        }
        throw new RuntimeException("Unable to get container name");
    }

    @Test
    public void testGetObjectMetadata() {
        System.out.println("--- TEST GET OBJECT METADATA ---");
        String containerName = getContainerName();
        String objectName = getObjectName(containerName);

        int failures = 0;
        for(int i=0; i < attempts; i++) {
            try {
                filesClient.getObjectMetaData(containerName, objectName);
            } catch (Exception e) {
                System.out.println("Failure getting object metadata: " +
                                   e.getMessage());
                failures++;
            }
        }
        System.out.println("TEST GET OBJECT METADATA RESULT: " + failures +
                           " failures " + " out of " + attempts + " attempts.");
    }

    private String getObjectName(String containerName) {
        for(int i=0; i<10; i++) {
            try {
                return filesClient.listObjects(containerName, null, 10, null)
                                   .get(0).getName();
            } catch (Exception e) {
            }
        }
        throw new RuntimeException("Unable to get object name");
    }

}
