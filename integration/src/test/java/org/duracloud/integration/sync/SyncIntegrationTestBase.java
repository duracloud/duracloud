/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.sync;

import static junit.framework.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManager;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.common.test.TestEndPoint;
import org.duracloud.error.ContentStoreException;
import org.duracloud.sync.mgmt.ChangedList;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author: Bill Branan
 * Date: Apr 16, 2010
 */
public class SyncIntegrationTestBase {

    protected static ContentStore store;
    protected static String spaceId;
    protected File tempDir;
    protected ChangedList changedList;


    protected File createTempDir(String dirName) {
        File tempDir = new File("target", dirName);
        if(!tempDir.exists()) {
            tempDir.mkdir();
        }
        return tempDir;
    }

    @BeforeClass
    public static void beforeClass() throws Exception {

        ContentStoreManager storeManager =
            new ContentStoreManagerImpl(getHost(), getPort(), getContext());
        
        storeManager.login(getRootCredential());

        store = storeManager.getPrimaryContentStore();

        String random = String.valueOf(new Random().nextInt(99999));
        spaceId = "synctool-test-space-" + random;
    }

    protected static String getHost(){
        return getTestEndPoint().getHost();
    }

    protected static String getPort(){
        return getTestEndPoint().getPort()+"";
    }

    private static TestEndPoint getTestEndPoint() {
        try {
            return new TestConfigUtil().getTestConfig().getTestEndPoint();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static String getContext(){
        return "durastore";
    }
    
    protected static Credential getRootCredential() throws Exception {
        SimpleCredential cred = new TestConfigUtil().getTestConfig().getRootCredential();
        return new Credential(cred.getUsername(), cred.getPassword());
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
