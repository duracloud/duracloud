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
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Dec 1, 2010
 */
public class CachingContentStoreImplTest {

    private CachingContentStoreImpl contentStore;
    private RestHttpHelper restHttpHelper;

    @Before
    public void setUp() throws Exception {
        contentStore = new CachingContentStoreImpl("http://example.org",
                                                   StorageProviderType.AMAZON_S3,
                                                   "store-id",
                                                   createMockRestHelper());
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
    public void testGetSpaces() throws Exception {
        contentStore.getSpaces();
        contentStore.getSpaces();
        EasyMock.verify(restHttpHelper);
    }

    private RestHttpHelper.HttpResponse createHttpResponse() throws Exception {
        String xml = getAccountXml();
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        return RestHttpHelper.HttpResponse.buildMock(HttpStatus.SC_OK, null, stream);
    }

    private String getAccountXml() throws Exception {
        StringBuilder xml = new StringBuilder();
        xml.append("<spaces>");
        xml.append("  <space id='space-0'/>");
        xml.append("  <space id='space-1'/>");
        xml.append("  <space id='space-2'/>");
        xml.append("</spaces>");
        return xml.toString();
    }
}
