/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.computeprovider.mgmt;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.duracloud.common.model.Credential;
import org.duracloud.computeprovider.domain.ComputeProviderType;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ComputeProviderFactoryImplTest {

    private final String instanceId = "testInstanceId";

    private Map<String, String> map;

    private final ComputeProviderType AMAZON = ComputeProviderType.AMAZON_EC2;

    private final ComputeProviderType BAD = ComputeProviderType.UNKNOWN;

    private Credential credential;

    private final String username = "username";

    private final String password = "password";

    private final String xmlProps = null;

    @Before
    public void setUp() throws Exception {

        map = new HashMap<String, String>();
        map
                .put(AMAZON.toString(),
                     "org.duracloud.computeprovider.mgmt.mock.LocalComputeProviderImpl");
        map.put(BAD.toString(),
                "org.duracloud.computeprovider.mgmt.Mockxxxxxxxxx");

        ComputeProviderFactory.setIdToClassMap(map);

        credential = new Credential();
        credential.setUsername(username);
        credential.setPassword(password);
    }

    @After
    public void tearDown() throws Exception {
        map = null;
        credential = null;
    }

    @Test
    public void testGetComputeProvider() throws Exception {
        ComputeProvider provider =
                ComputeProviderFactory.getComputeProvider(AMAZON);
        assertNotNull(provider);

        assertFalse(provider.isInstanceRunning(credential, instanceId, xmlProps));
    }

    @Test
    public void testGetInvalidComputeProvider() {
        ComputeProvider provider = null;
        try {
            provider = ComputeProviderFactory.getComputeProvider(BAD);
            fail("Should throw and exception!");
        } catch (Exception e) {
        }

        assertTrue(provider == null);
    }

}
