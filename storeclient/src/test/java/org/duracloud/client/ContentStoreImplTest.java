/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.InvalidIdException;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: 8/23/11
 */
public class ContentStoreImplTest {

    private static final String HEADER_PREFIX = "x-dura-meta-";

    private ContentStore contentStore;

    private final String baseURL = "url";
    private final StorageProviderType type = StorageProviderType.AMAZON_S3;
    private final String storeId = "0";
    private RestHttpHelper restHelper;

    @Before
    public void setUp() throws Exception {
        restHelper = EasyMock.createMock("RestHttpHelper",
                                         RestHttpHelper.class);

        contentStore = new ContentStoreImpl(baseURL, type, storeId, restHelper);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(restHelper);
    }

    private void replayMocks() {
        EasyMock.replay(restHelper);
    }

    @Test
    public void testCopyContentWithDefaultStoreId() throws Exception {
        testCopyContent(storeId);
    }

    @Test
    public void testCopyContentWithAlternateStoreId() throws Exception {
        testCopyContent("1");
    }

    public void testCopyContent(String destStoreId) throws Exception {
        String srcSpaceId = "src-space-id";
        String srcContentId = "src-content-id";
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";

        String expectedMd5 = "md5";
        int expectedStatus = 201;
        Capture<Map<String, String>> capturedHeaders = createCopyContentMocks(
            destStoreId,
            destSpaceId,
            destContentId,
            expectedMd5,
            expectedStatus);
        replayMocks();

        
        String md5;
        if(storeId.equals(destStoreId)){
            md5 = contentStore.copyContent(
                srcSpaceId,
                srcContentId,
                destSpaceId,
                destContentId);
            
        }else{
            md5 = contentStore.copyContent(
                srcSpaceId,
                srcContentId,
                destStoreId,
                destSpaceId,
                destContentId);
        }
        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);

        Assert.assertNotNull(capturedHeaders);
        Map<String, String> headers = capturedHeaders.getValue();
        Assert.assertNotNull(headers);
        Assert.assertEquals(2, headers.size());
        Assert.assertEquals(srcSpaceId + "/" + srcContentId,
                            headers.get(HEADER_PREFIX
                                + StorageProvider.PROPERTIES_COPY_SOURCE));

        if(!destStoreId.equals(this.storeId)){
            Assert.assertEquals(destStoreId,
                                headers.get(HEADER_PREFIX
                                    + StorageProvider.PROPERTIES_COPY_SOURCE_STORE));
        }
    }

    @Test
    public void testCopyContentErrorWithDefaultStore() throws Exception {
        testCopyContentError(storeId);
    }

    @Test
    public void testCopyContentErrorWithAlternateStore() throws Exception {
        testCopyContentError("1");
    }

    public void testCopyContentError(String destStoreId) throws Exception {
        String srcSpaceId = "src-space-id";
        String srcContentId = "src-content-id";
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";

        String expectedMd5 = "md5";
        int status = 400;
        createCopyContentMocksError(destStoreId,
                                    destSpaceId,
                                    destContentId,
                                    expectedMd5,
                                    status);
        replayMocks();

        try {
            
            if(storeId.equals(destStoreId)){
                contentStore.copyContent(srcSpaceId,
                                         srcContentId,
                                         destSpaceId,
                                         destContentId);
            }else{
                contentStore.copyContent(srcSpaceId,
                                         srcContentId,
                                         destStoreId,
                                         destSpaceId,
                                         destContentId);
            }
            Assert.fail("exception expected");

        } catch (Exception e) {
            Assert.assertTrue(e instanceof InvalidIdException);
        }

    }

    private void createCopyContentMocksError(String destStoreId,
                                             String destSpaceId,
                                             String destContentId,
                                             String md5,
                                             int status) throws Exception {

        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            "HttpResponse",
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(status);

        Header header = EasyMock.createMock(Header.class);
        EasyMock.expect(header.getValue()).andReturn(md5);
        EasyMock.expect(response.getResponseHeader(HttpHeaders.CONTENT_MD5))
            .andReturn(header);
        EasyMock.expect(response.getResponseBody()).andReturn("body");
        EasyMock.replay(response, header);

        String fullURL =
            baseURL + "/" + destSpaceId + "/" + destContentId + "?storeID=" +
                destStoreId;
        Capture<Map<String, String>> capturedHeaders =
            new Capture<Map<String, String>>();
        EasyMock.expect(restHelper.put(EasyMock.eq(fullURL),
                                       EasyMock.<String>isNull(),
                                       EasyMock.capture(capturedHeaders)))
            .andReturn(response);

    }

    private Capture<Map<String, String>> createCopyContentMocks(String destStoreId,
                                                                String destSpaceId,
                                                                String destContentId,
                                                                String md5,
                                                                int status)
        throws Exception {
        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            "HttpResponse",
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(status);

        Header header = EasyMock.createMock(Header.class);
        EasyMock.expect(header.getValue()).andReturn(md5);
        EasyMock.expect(response.getResponseHeader(HttpHeaders.CONTENT_MD5))
            .andReturn(header);
        EasyMock.replay(response, header);

        String fullURL =
            baseURL + "/" + destSpaceId + "/" + destContentId + "?storeID=" +
                destStoreId;
        Capture<Map<String, String>> capturedHeaders =
            new Capture<Map<String, String>>();
        EasyMock.expect(restHelper.put(EasyMock.eq(fullURL),
                                       EasyMock.<String>isNull(),
                                       EasyMock.capture(capturedHeaders)))
            .andReturn(response);

        return capturedHeaders;
    }

    @Test
    public void testMoveContentWithDefaultStore() throws Exception {
        testMoveContent(storeId);
    }

    @Test
    public void testMoveContentWithAlternateStore() throws Exception {
        testMoveContent("1");
    }

    public void testMoveContent(String destStoreId) throws Exception {
        String srcSpaceId = "src-space-id";
        String srcContentId = "src-content-id";
        String destSpaceId = "dest-space-id";
        String destContentId = "dest-content-id";

        String expectedMd5 = "md5";
        int expectedStatus = 201;
        Capture<Map<String, String>> capturedHeaders = createCopyContentMocks(
            destStoreId,
            destSpaceId,
            destContentId,
            expectedMd5,
            expectedStatus);

        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            "HttpResponse",
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(HttpStatus.SC_OK);
        EasyMock.expect(restHelper.delete(EasyMock.<String>notNull()))
            .andReturn(response);
        EasyMock.replay(response);        
        replayMocks();

        String md5;
        if(destStoreId.equals(storeId)){
            md5 = contentStore.moveContent(srcSpaceId,
                                           srcContentId,
                                           destSpaceId,
                                           destContentId);
        }else{
            md5 = contentStore.moveContent(srcSpaceId,
                                           srcContentId,
                                           destStoreId,
                                           destSpaceId,
                                           destContentId);
        }
        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);

        Assert.assertNotNull(capturedHeaders);
        Map<String, String> headers = capturedHeaders.getValue();
        Assert.assertNotNull(headers);
        Assert.assertEquals(2, headers.size());
        Assert.assertEquals(srcSpaceId + "/" + srcContentId, headers.get(
            HEADER_PREFIX + StorageProvider.PROPERTIES_COPY_SOURCE));
        if (!destStoreId.equals(this.storeId)) {
            Assert.assertEquals(destStoreId,
                                headers.get(HEADER_PREFIX
                                    + StorageProvider.PROPERTIES_COPY_SOURCE_STORE));

        }
    }
}
