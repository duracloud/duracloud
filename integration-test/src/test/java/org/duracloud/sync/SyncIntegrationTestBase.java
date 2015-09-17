/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.error.ContentStoreException;
import org.duracloud.sync.mgmt.ChangedList;
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.util.Random;

import static junit.framework.Assert.assertNull;

/**
 * @author: Bill Branan
 * Date: Apr 16, 2010
 */
public class SyncIntegrationTestBase {

    protected static String host;
    protected static String context;
    protected static String port;
    protected static ContentStore store;
    protected static String spaceId;
    protected static String username;
    protected File tempDir;
    protected ChangedList changedList;

    private final static StorageAccountTestUtil acctUtil = new StorageAccountTestUtil();

    protected File createTempDir(String dirName) {
        File tempDir = new File("target", dirName);
        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return tempDir;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        host = "localhost";
        context = "durastore";
        username = "username";
        port = getPort();

        acctUtil.initializeDurastore(host, port, context);

        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(host, port, context);
        storeManager.login(getRootCredential());

        store = storeManager.getPrimaryContentStore();

        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "synctool-test-space-" + random;
    }

    protected static Credential getRootCredential() throws Exception {
        return acctUtil.getRootCredential();
    }

    private static String getPort() throws Exception {
        String port = new SyncToolTestConfig().getPort();
        try { // Ensure the port is a valid port value
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            port = "8080";
        }
        return port;
    }

    @AfterClass
    public static void afterClass() {
        try {
            store.deleteSpace(spaceId);
        } catch(ContentStoreException e) {
            System.err.println("Failed to delete space " + spaceId +
                               " after tests");
        }
    }

    @Before
    public void setUp() throws Exception {
        changedList = ChangedList.getInstance();
        assertNull(changedList.reserve());

        tempDir = createTempDir("sync-test-dir");
    }

    @After
    public void tearDown() throws Exception {
        while(changedList.reserve() != null) {}
        assertNull(changedList.reserve());

        FileUtils.deleteDirectory(tempDir);
    }    

}
