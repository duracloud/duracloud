/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.http.HttpStatus;
import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Dec 1, 2010
 */
public class CachingContentStoreManagerImplTest {

    private CachingContentStoreManagerImpl contentStoreMgr;
    private RestHttpHelper restHttpHelper;

    @Before
    public void setUp() throws Exception {
        contentStoreMgr = new CachingContentStoreManagerImpl("test.org",
                                                             "9876",
                                                             "durastore");
        contentStoreMgr.setRestHelper(createMockRestHelper());
    }

    private RestHttpHelper createMockRestHelper() throws Exception {
        restHttpHelper = EasyMock.createMock("RestHttpHelper",
                                             RestHttpHelper.class);
        EasyMock.expect(restHttpHelper.get(EasyMock.isA(String.class)))
            .andReturn(createHttpResponse())
            .times(1);

        EasyMock.replay(restHttpHelper);
        return restHttpHelper;
    }

    @Test
    public void testGetPrimaryContentStore() throws Exception {
        contentStoreMgr.getPrimaryContentStore();
        contentStoreMgr.getPrimaryContentStore();
        EasyMock.verify(restHttpHelper);
    }

    @Test
    public void testGetContentStores() throws Exception {
        contentStoreMgr.getContentStores();
        contentStoreMgr.getContentStores();
        EasyMock.verify(restHttpHelper);
    }

    private RestHttpHelper.HttpResponse createHttpResponse() throws Exception {
        String xml = getAccountXml();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        return RestHttpHelper.HttpResponse.buildMock(HttpStatus.SC_OK, null, stream);
    }

    private String getAccountXml() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<storageProviderAccounts>");
        xml.append("  <storageAcct ownerId='0' isPrimary='1'>");
        xml.append("    <id>0</id>");
        xml.append("    <storageProviderType>AMAZON_S3</storageProviderType>");
        xml.append("    <storageProviderCredential>");
        EncryptionUtil encryptUtil = new EncryptionUtil();
        String username = encryptUtil.encrypt("username");
        xml.append("      <username>" + username + "</username>");
        String password = encryptUtil.encrypt("password");
        xml.append("      <password>" + password + "</password>");
        xml.append("    </storageProviderCredential>");
        xml.append("  </storageAcct>");
        xml.append("</storageProviderAccounts>");
        return xml.toString();
    }

}
