/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb.domain;

import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.error.UnknownResourceTypeException;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Mar 15, 2010
 */
public class ResourceTypeTest {

    @Test
    public void testFromString() {
        ResourceType resourceType;
        for (StorageProviderType providerType : StorageProviderType.values()) {
            resourceType = ResourceType.fromString(providerType.toString());
            Assert.assertNotNull(resourceType);
            Assert.assertEquals(resourceType.toString(),
                                providerType.toString());
        }

        for (DuraCloudUserType userType : DuraCloudUserType.values()) {
            resourceType = ResourceType.fromString(userType.toString());
            Assert.assertNotNull(resourceType);
            Assert.assertEquals(resourceType.toString(), userType.toString());
        }

        verifyFail("junk");
        verifyFail("");
        verifyFail(null);
    }

    private void verifyFail(String id) {
        boolean thrown = false;
        try {
            ResourceType.fromString(id);
            Assert.fail("exception expected");
        } catch (UnknownResourceTypeException e) {
            thrown = true;
            Assert.assertEquals(id, e.getMessage());
        }
        Assert.assertTrue(thrown);
    }

}
