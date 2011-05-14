/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.domain.impl;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;

/**
 * @author Andrew Woods
 *         Date: 5/11/11
 */
public class StorageAccountS3Impl extends StorageAccountImpl {

    public StorageAccountS3Impl(String id,
                                String username,
                                String password,
                                String storageClass) {
        super(id, username, password, StorageProviderType.AMAZON_S3);
        if (!StringUtils.isBlank(storageClass)) {
            String storageClassLower = storageClass.toLowerCase();
            if (storageClassLower.equals("reduced_redundancy")
                || storageClassLower.equals("reducedredundancy")
                || storageClassLower.equals("reduced")
                || storageClassLower.equals("rrs")) {
                setStorageClassReducedRedundancy();

            } else if (storageClassLower.equals("standard")) {
                setStorageClassStandard();

            } else {
                throw new DuraCloudRuntimeException(
                    "Invalid storageClass: '" + storageClass + "'");
            }
        }
    }

    public String getStorageClass() {
        return super.getProperty(StorageAccount.PROPS.STORAGE_CLASS.name());
    }

    public void setStorageClassStandard() {
        super.setProperty(StorageAccount.PROPS.STORAGE_CLASS.name(),
                          "STANDARD");
    }

    public void setStorageClassReducedRedundancy() {
        super.setProperty(StorageAccount.PROPS.STORAGE_CLASS.name(),
                          "REDUCED_REDUNDANCY");
    }

}
