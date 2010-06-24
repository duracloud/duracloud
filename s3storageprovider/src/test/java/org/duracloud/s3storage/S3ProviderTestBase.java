/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import static junit.framework.Assert.assertNotNull;
import org.duracloud.common.model.Credential;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;

/**
 * @author: Bill Branan
 * Date: May 20, 2010
 */
public class S3ProviderTestBase {

    protected Credential getCredential() throws Exception {
        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        Credential s3Credential =
            dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                                             StorageProviderType.AMAZON_S3));
        assertNotNull(s3Credential);
        assertNotNull(s3Credential.getUsername());
        assertNotNull(s3Credential.getPassword());        

        return s3Credential;
    }    

}
