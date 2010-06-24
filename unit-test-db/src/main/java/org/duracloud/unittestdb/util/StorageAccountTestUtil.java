/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.unittestdb.util;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.DuraCloudUserType;
import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.common.web.RestHttpHelper.HttpResponse;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.unittestdb.UnitTestDatabaseUtil;
import org.duracloud.unittestdb.domain.ResourceType;

/**
 * Provides utilities for testing with storage accounts.
 *
 * @author Bill Branan
 */
public class StorageAccountTestUtil {

    private Credential rootCredential;

    public HttpResponse initializeDurastore(String host, String port, String context)
        throws Exception {
        String baseURL = "http://" + host + ":" + port + "/" + context;
        String storesURL =  baseURL + "/stores";

        String xml = buildTestAccountXml();
        RestHttpHelper restHelper = new RestHttpHelper(getRootCredential());
        return restHelper.post(storesURL, xml, null);
    }

    private String buildTestAccountXml() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<storageProviderAccounts>");

        UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
        int acctId = 0;
        for(StorageProviderType type : StorageProviderType.values()) {
            Credential cred = null;
            try {
                cred = dbUtil.findCredentialForResource(ResourceType.fromStorageProviderType(
                    type));
            } catch (Exception e) {
                if(type.equals(StorageProviderType.TEST_RETRY) ||
                   type.equals(StorageProviderType.TEST_VERIFY_CREATE) ||
                   type.equals(StorageProviderType.TEST_VERIFY_DELETE)) {
                    cred = new Credential("", "");
                } else {
                    // No credentials available for provider type - skip
                    continue;
                }
            }
            if(cred != null) {
                ++acctId;
                EncryptionUtil encryptUtil = new EncryptionUtil();
                String encUsername = encryptUtil.encrypt(cred.getUsername());
                String encPassword = encryptUtil.encrypt(cred.getPassword());

                xml.append("<storageAcct ownerId='0'");
                if(type.equals(StorageProviderType.AMAZON_S3)) {
                    xml.append(" isPrimary='1'");
                }
                xml.append(">");
                xml.append("<id>"+acctId+"</id>");
                xml.append("<storageProviderType>");
                xml.append(type.name());
                xml.append("</storageProviderType>");
                xml.append("<storageProviderCredential>");
                xml.append("<username>"+encUsername+"</username>");
                xml.append("<password>"+encPassword+"</password>");
                xml.append("</storageProviderCredential>");
                xml.append("</storageAcct>");
            }
        }

        xml.append("</storageProviderAccounts>");
        return xml.toString();
    }

    public Credential getRootCredential() throws Exception {
        if (null == rootCredential) {
            UnitTestDatabaseUtil dbUtil = new UnitTestDatabaseUtil();
            ResourceType rootUser = ResourceType.fromDuraCloudUserType(
                DuraCloudUserType.ROOT);
            rootCredential = dbUtil.findCredentialForResource(rootUser);
        }
        return rootCredential;
    }

}
