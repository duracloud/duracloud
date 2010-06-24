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
import org.duracloud.unittestdb.util.StorageAccountTestUtil;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.util.Random;

/**
 * @author: Bill Branan
 * Date: Apr 16, 2010
 */
public class SyncIntegrationTestBase extends SyncTestBase {

    protected static String host;
    protected static String context;
    protected static String port;
    protected static ContentStore store;
    protected static String spaceId;
    protected File tempDir;

    private final static StorageAccountTestUtil acctUtil = new StorageAccountTestUtil();

    @BeforeClass
    public static void beforeClass() throws Exception {
        host = "localhost";
        context = "durastore";
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

    @Override
    @Before
    public void setUp() throws Exception {
        tempDir = createTempDir("sync-test-dir");
    }

    @Override
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(tempDir);
    }    

}
