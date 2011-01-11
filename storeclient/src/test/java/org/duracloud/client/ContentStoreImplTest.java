/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.apache.commons.httpclient.Header;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.storage.domain.StorageProviderType;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Jan 11, 2011
 */
public class ContentStoreImplTest {

    private ContentStoreImpl store;
    private RestHttpHelper restHelper;
    private RestHttpHelper.HttpResponse response;

    @Before
    public void setUp() throws Exception {
        restHelper = createMockRestHttpHelper();

        String baseURL = "http://example.org";
        StorageProviderType type = StorageProviderType.RACKSPACE;
        String storeId = "1";
        store = new ContentStoreImpl(baseURL, type, storeId, restHelper);
    }

    private RestHttpHelper createMockRestHttpHelper() throws Exception {
        response = EasyMock.createMock("HttpResponse",
                                       RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(200);

        Header[] headers = {};
        EasyMock.expect(response.getResponseHeaders())
            .andReturn(headers)
            .anyTimes();

        response.close();
        EasyMock.expectLastCall();
        EasyMock.replay(response);

        RestHttpHelper restHttpHelper = EasyMock.createMock("RestHttpHelper",
                                                            RestHttpHelper.class);

        EasyMock.expect(restHttpHelper.head(EasyMock.isA(String.class)))
            .andReturn(response);

        EasyMock.replay(restHttpHelper);
        return restHttpHelper;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(restHelper);
        EasyMock.verify(response);
    }

    @Test
    public void testGetContentMetadata() throws Exception {
        String spaceId = "space-id";
        String contentId = "content-id";
        store.getContentMetadata(spaceId, contentId);
        // test is verified if mock objects verify.
    }
}
