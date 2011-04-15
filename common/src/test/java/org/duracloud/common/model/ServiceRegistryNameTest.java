/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.common.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: 4/13/11
 */
public class ServiceRegistryNameTest {

    private ServiceRegistryName registryName;

    @Test
    public void testGetName() throws Exception {
        String version = "0.9.0-SNAPSHOT";
        String expectedInfix = "0-9-0-snapshot";
        verifyName(version, expectedInfix);

        version = "1.0.0";
        expectedInfix = "1-0-0";
        verifyName(version, expectedInfix);
    }

    private void verifyName(String version, String expectedInfix) {
        registryName = new ServiceRegistryName(version);

        String name = registryName.getName();
        Assert.assertNotNull(name);

        String expectedName = "duracloud-" + expectedInfix + "-service-repo";
        Assert.assertEquals(expectedName, name);
    }
}
