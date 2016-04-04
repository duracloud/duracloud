/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.rackspacestorage;

import junit.framework.Assert;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.openstackstorage.OpenStackStorageProvider;
import org.junit.Before;
import org.junit.Test;

/**
 * This class is used to test the RackspaceStorageProvider GET calls.
 *
 * @author: Bill Branan
 * Date: May 27, 2011
 */
public class RackspaceStorageProviderGetTestNotRun {

    private OpenStackStorageProvider rackspaceProvider = null;
    private int attempts = 10;

    private String username;
    private String password;

    @Before
    public void setup() throws Exception {
        TestConfigUtil configUtil = new TestConfigUtil();
        SimpleCredential credential = configUtil.getCredential(
            StorageProviderCredential.ProviderType.RACKSPACE);
        Assert.assertNotNull(credential);

        this.username = credential.getUsername();
        this.password = credential.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);

        rackspaceProvider = getFreshStorageProvider();
    }

    private RackspaceStorageProvider getFreshStorageProvider() {
        return new RackspaceStorageProvider(username, password);
    }

    @Test
    public void testGetSpaces() {
        System.out.println("--- TEST GET SPACES ---");
        int failures = 0;
        for(int i=0; i < attempts; i++) {
            try {
                rackspaceProvider.getSpaces();
            } catch (Exception e) {
                System.out.println("Failure getting spaces: " +
                                   e.getMessage());
                failures++;
            }
        }
        System.out.println("TEST GET SPACES RESULT: " + failures +
                           " failures out of " + attempts + " attempts.");
    }

    @Test
    public void testGetSpaceContents() {
        System.out.println("--- TEST GET SPACE CONTENTS ---");
        String spaceId = getSpaceId();

        int failures = 0;
        for(int i=0; i < attempts; i++) {
            try {
                rackspaceProvider.getSpaceContents(spaceId, null);
            } catch (Exception e) {
                System.out.println("Failure getting space contents: " +
                                   e.getMessage());
                failures++;
            }
        }
        System.out.println("TEST GET SPACE CONTENTS RESULT: " + failures +
                           " failures " + " out of " + attempts + " attempts.");
    }

    private String getSpaceId() {
        for(int i=0; i<10; i++) {
            try {
                return rackspaceProvider.getSpaces().next();
            } catch(Exception e) {
            }
        }
        throw new RuntimeException("Unable to get a space Id");
    }

    @Test
    public void testGetContentProperties() {
        System.out.println("--- TEST GET CONTENT PROPERTIES ---");
        String spaceId = getSpaceId();
        String contentId = getContentId(spaceId);

        int failures = 0;
        for(int i=0; i < attempts; i++) {
            try {
                rackspaceProvider.getContentProperties(spaceId, contentId);
            } catch (Exception e) {
                System.out.println("Failure getting object properties: " +
                                   e.getMessage());
                failures++;
            }
        }
        System.out.println("TEST GET CONTENT PROPERTIES RESULT: " + failures +
                           " failures " + " out of " + attempts + " attempts.");
    }

    private String getContentId(String spaceId) {
        for(int i=0; i<10; i++) {
            try {
                return rackspaceProvider.getSpaceContents(spaceId, null).next();
            } catch (Exception e) {
            }
        }
        throw new RuntimeException("Unable to get content Id from space: " +
                                   spaceId);
    }

}
