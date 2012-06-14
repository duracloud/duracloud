/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sdscstorage;

import com.rackspacecloud.client.cloudfiles.FilesClient;
import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 6/6/12
 */
public class SDSCStorageProviderTest {

    private SDSCStorageProvider provider;

    @Before
    public void setup() {
        FilesClient filesClient = EasyMock.createMock(FilesClient.class);
        provider = new SDSCStorageProvider(filesClient);
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
