/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import junit.framework.Assert;
import org.duracloud.common.model.Credential;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * This class is used to test Rackspace API calls which are causing
 * problems in order to better understand how the call is functioning
 * prior to writing up a question to Rackspace support.
 *
 * @author: Bill Branan
 * Date: Jan 8, 2010
 */
public class RackspaceApiTestNotRun {

    private FilesClient filesClient = null;
    private String CONTAINER_NAME;
    private String CONTENT_NAME = "test-content";

    @Before
    public void setUp() throws Exception {
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

        String random = String.valueOf(new Random().nextInt(99999));
        CONTAINER_NAME = "api-test-container-" + random;        
    }

    @After
    public void tearDown() throws Exception {
        try {
            filesClient.deleteObject(CONTAINER_NAME, CONTENT_NAME);
        } catch (Exception e) {
            // Ignore, object was likely not created or already deleted
        }
        filesClient.deleteContainer(CONTAINER_NAME);
    }

    private Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        return dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                                                StorageProviderType.RACKSPACE));
    }

    /*
     * Goal for this method is to determine how to discover whether a
     * Rackspace container has been CDN enabled, which translates to
     * being OPEN in DuraCloud lingo. The current issue is that calling
     * getCDNContainerInfo() on a container which has not been enabled
     * throws an exception.
     */
    @Test
    public void testGetCDNContainerInfo() throws Exception {
        filesClient.createContainer(CONTAINER_NAME);

        // This currently fails
//        FilesCDNContainer cdnContainer =
//            filesClient.getCDNContainerInfo(CONTAINER_NAME);
//        assertNotNull(cdnContainer);
//        assertFalse(cdnContainer.isEnabled());

        filesClient.cdnEnableContainer(CONTAINER_NAME);

        FilesCDNContainer cdnContainer =
            filesClient.getCDNContainerInfo(CONTAINER_NAME);
        assertNotNull(cdnContainer);
        assertTrue(cdnContainer.isEnabled());
    }

    /*
     * It would appear (from exception traces) that calling
     * listObjectsStaringWith() has been failing. Attempting
     * to determine the failure rate here. 
     */
    @Test
    public void testListObjectsStartingWith() throws Exception {
        filesClient.createContainer(CONTAINER_NAME);

        Map<String, String> properties = new HashMap<String, String>();
        filesClient.storeObject(CONTAINER_NAME,
                                "test".getBytes(),
                                "text/plain",
                                CONTENT_NAME,
                                properties);

        int attempts = 100;
        int failures = 0;
        for(int i=0; i<attempts; i++) {
            try {
                filesClient.listObjectsStartingWith(CONTAINER_NAME,
                                                    "test",
                                                    null,
                                                    10,
                                                    null);
            } catch(Exception e) {
                if(e.getMessage().contains("Error parsing server resposne")) {
                    failures++;
                }
                else throw e;
            }
        }
        System.out.println("FAILURES OUT OF " + attempts +
                           " ATTEMPTS: " + failures);
    }

    /*
     * When setting properties on a Rackspace object the name of the
     * property is being changed to camel case (i.e. test-meta becomes
     * Test-Meta). This method shows that behavior.
     *
     * Note that according to Rackspace support (ticket #13594) this
     * is expected behavior. All property names are converted to camel
     * case for transfer and so are returned that way when retrieved.
     */
    @Test
    public void testSetPropertiesLowercase() throws Exception {
        filesClient.createContainer(CONTAINER_NAME);

        String[] propertyNames = {"Test-Property-1", "test-property-2",
                                  "testProperty-3", "tEsT-pRoPeRtY-4"};
        String propertyValue = "test-property";

        Map<String, String> properties = new HashMap<String, String>();
        for(String name : propertyNames) {
            properties.put(name, propertyValue);
        }
        filesClient.storeObject(CONTAINER_NAME,
                                "test".getBytes(),
                                "text/plain",
                                CONTENT_NAME,
                                properties);

        Map<String, String> retrievedMeta =
            filesClient.getObjectMetaData(CONTAINER_NAME,
                                          CONTENT_NAME).getMetaData();

        System.out.println("Property Keys");
        for(String key : retrievedMeta.keySet()) {
            System.out.println("["+key+"]");
        }

        // These currently fail, the casing of the keys are changed to camel case
//        for(String name : propertyNames) {
//            assertTrue(retrievedMeta.containsKey(name));
//        }
    }

    /*
     * This method was added in an effort to determine how to properly set
     * the MIME type of a Rackspace object. It's clear that the method
     * attempted here does not work.
     */
    @Test
    public void testUpdateMimeType() throws Exception {
        filesClient.createContainer(CONTAINER_NAME);

        String mimetype = "text/plain";
        Map<String, String> properties = new HashMap<String, String>();
        filesClient.storeObject(CONTAINER_NAME,
                                "test".getBytes(),
                                mimetype,
                                CONTENT_NAME,
                                properties);

        // Make sure original mimetype is correct
        String retrievedMime =
            filesClient.getObjectMetaData(CONTAINER_NAME,
                                          CONTENT_NAME).getMimeType();
        assertEquals(mimetype, retrievedMime);

        // Update mimetype
        mimetype = "image/jpeg";
        properties.put("Content-Type", mimetype);
        filesClient.updateObjectMetadata(CONTAINER_NAME,
                                         CONTENT_NAME,
                                         properties);

        // See if mimetype was actually updated
        retrievedMime =
            filesClient.getObjectMetaData(CONTAINER_NAME,
                                          CONTENT_NAME).getMimeType();
        System.out.println("Updated MIME type: " + retrievedMime);
//        assertEquals(mimetype, retrievedMime);
    }
}
