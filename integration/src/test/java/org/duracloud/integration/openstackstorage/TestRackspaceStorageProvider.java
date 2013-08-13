/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.openstackstorage;

import junit.framework.Assert;
import org.duracloud.common.model.SimpleCredential;
import org.duracloud.common.test.StorageProviderCredential;
import org.duracloud.common.test.TestConfigUtil;
import org.duracloud.rackspacestorage.RackspaceStorageProvider;

import java.io.IOException;

/**
 * @author Erik Paulsson
 *         Date: 8/5/13
 */
public class TestRackspaceStorageProvider extends TestOpenStackStorageProvider {

    @Override
    public void setUp() {
        TestConfigUtil testConfigUtil = new TestConfigUtil();
        try {
            SimpleCredential cred = testConfigUtil.getCredential(StorageProviderCredential.ProviderType.RACKSPACE);
            storageProvider = new RackspaceStorageProvider(cred.getUsername(), cred.getPassword());
            super.setUp();
        } catch (IOException ioe) {
            Assert.fail(ioe.getMessage());
        }
    }
}
