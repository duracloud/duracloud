/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sdscstorage;

import junit.framework.Assert;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMock;
import org.jclouds.openstack.swift.SwiftClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Andrew Woods
 *         Date: 6/6/12
 */
public class SDSCStorageProviderTest {

    private SDSCStorageProvider provider;
    private SwiftClient swiftClient;

    @Before
    public void setup() {
        swiftClient = EasyMock.createMock("SwiftClient", SwiftClient.class);
        provider = new SDSCStorageProvider(swiftClient);

        replayMocks();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(swiftClient);
    }

    private void replayMocks() {
        EasyMock.replay(swiftClient);
    }

    @Test
    public void testGetStorageProviderType() {
        SDSCStorageProvider provider = new SDSCStorageProvider("accessKey", "secretKey");
        assertEquals(StorageProviderType.SDSC, provider.getStorageProviderType());
    }

    @Test
    public void testGetAuthUrl() throws Exception {
        String authUrl = provider.getAuthUrl();
        Assert.assertNotNull(authUrl);

        String expected = "https://duracloud.auth.cloud.sdsc.edu/auth/v1.0";
        Assert.assertEquals(expected, authUrl);
    }

    @Test
    public void testGetProviderName() throws Exception {
        String providerName = provider.getProviderName();
        Assert.assertNotNull(providerName);

        String expected = "SDSC";
        Assert.assertEquals(expected, providerName);
    }
}
