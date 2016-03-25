/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.integration.duradmin;

import org.duracloud.common.model.Credential;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.integration.util.StorageAccountTestUtil;

/**
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class RestTestHelper {

    private static Credential rootCredential;

    public static RestHttpHelper getAuthorizedRestHelper() {
        return new RestHttpHelper(new StorageAccountTestUtil().getRootCredential());
    }


}
