/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin;

import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;

/**
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class RestTestHelper {

    private static Credential rootCredential;

    public static RestHttpHelper getAuthorizedRestHelper() {
        return new RestHttpHelper(getRootCredential());
    }

    private static Credential getRootCredential() {
        if (null == rootCredential) {
            UnitTestDatabaseUtil dbUtil = null;
            try {
                dbUtil = new UnitTestDatabaseUtil();
            } catch (Exception e) {
                System.err.println("ERROR from unitTestDB: " + e.getMessage());
                throw new RuntimeException(e);
            }

            try {
                ResourceType rootUser = ResourceType.fromDuraCloudUserType(
                    DuraCloudUserType.ROOT);
                rootCredential = dbUtil.findCredentialForResource(rootUser);
            } catch (Exception e) {
                System.err.print("ERROR getting credential: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return rootCredential;
    }
}
