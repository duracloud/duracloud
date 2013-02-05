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
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.InvalidIdException;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 8/23/11
 */
public class ContentStoreImplTest {

    private static final String HEADER_PREFIX = "x-dura-meta-";
    private static final String ACL_PREFIX = StorageProvider.PROPERTIES_SPACE_ACL;

    private ContentStore contentStore;

    private final String baseURL = "url";
    private final StorageProviderType type = StorageProviderType.AMAZON_S3;
    private final String storeId = "1";
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
        doTestCopyContent(storeId);
    }

    @Test
    public void testCopyContentWithAlternateStoreId() throws Exception {
        doTestCopyContent("1");
    }

    private void doTestCopyContent(String destStoreId) throws Exception {
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
        doTestCopyContentError(storeId);
    }

    @Test
    public void testCopyContentErrorWithAlternateStore() throws Exception {
        doTestCopyContentError("1");
    }

    private void doTestCopyContentError(String destStoreId) throws Exception {
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
        doTestMoveContent(storeId);
    }

    @Test
    public void testMoveContentWithAlternateStore() throws Exception {
        doTestMoveContent("1");
    }

    private void doTestMoveContent(String destStoreId) throws Exception {
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

    @Test
    public void testSetSpaceACLs() throws Exception {
        String name = "name1";
        Map<String, AclType> acls = createACLsForTest(name);

        String spaceId = "space-id";
        Capture<Map<String, String>> capturedHeaders = createSetSpaceACLsMocks(
            spaceId,
            storeId,
            200);

        // call being tested
        contentStore.setSpaceACLs(spaceId, acls);

        // Note; All headers are included, even if they are not ACL headers.
        Assert.assertNotNull(capturedHeaders);
        Map<String, String> headers = capturedHeaders.getValue();
        Assert.assertNotNull(headers);

        Assert.assertEquals(acls.size(), headers.size());

        String prefix = HEADER_PREFIX + ACL_PREFIX;

        Set<String> headerKeys = headers.keySet();
        for (String acl : acls.keySet()) {
            Assert.assertTrue(headerKeys.contains(prefix + acl));

            String aclProp = headers.get(prefix + acl);
            Assert.assertEquals(acls.get(acl), AclType.valueOf(aclProp));
        }
    }

    private Map<String, AclType> createACLsForTest(String name) {
        String prefix = HEADER_PREFIX + ACL_PREFIX;
        String name0 = prefix + "name0";
        String name1 = name;
        String name2 = prefix + "name2";
        String name3 = prefix + "name3";

        AclType value0 = AclType.READ;
        AclType value1 = AclType.READ;
        AclType value2 = AclType.WRITE;
        AclType value3 = AclType.WRITE;

        Map<String, AclType> acls = new HashMap<String, AclType>();
        acls.put(name0, value0);
        acls.put(name1, value1);
        acls.put(name2, value2);
        acls.put(name3, value3);
        return acls;
    }

    private Capture<Map<String, String>> createSetSpaceACLsMocks(String spaceId,
                                                                 String storeId,
                                                                 int status)
        throws Exception {
        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            "HttpResponse",
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(status);

        Header header = EasyMock.createMock(Header.class);
        EasyMock.replay(response, header);

        String fullURL = baseURL + "/acl/" + spaceId + "?storeID=" + storeId;
        Capture<Map<String, String>> capturedHeaders =
            new Capture<Map<String, String>>();
        EasyMock.expect(restHelper.post(EasyMock.eq(fullURL),
                                        EasyMock.<String>isNull(),
                                        EasyMock.capture(capturedHeaders)))
                .andReturn(response);

        replayMocks();

        return capturedHeaders;
    }

    @Test
    public void testGetSpaceACLs() throws Exception {
        String name = "name1";
        Map<String, AclType> acls = createACLsForTest(name);

        String spaceId = "space-id";
        createGetSpaceACLsMocks(acls, spaceId, storeId, 200);

        // call being tested
        Map<String, AclType> spaceACLs = contentStore.getSpaceACLs(spaceId);
        Assert.assertNotNull(spaceACLs);

        // header without the proper x-dura-meta- prefix is omitted.
        Assert.assertEquals(acls.size() - 1, spaceACLs.size());
        Assert.assertTrue(!spaceACLs.containsKey(name));
        Assert.assertNotNull(acls.remove(name));

        Set<String> spaceACLKeys = spaceACLs.keySet();
        String aclHeaderPrefix = HEADER_PREFIX + ACL_PREFIX;
        for (String acl : acls.keySet()) {
            String aclNoPrefix = acl.substring(aclHeaderPrefix.length());
            Assert.assertTrue(spaceACLKeys.contains(aclNoPrefix));
            Assert.assertEquals(acls.get(acl), spaceACLs.get(aclNoPrefix));
        }
    }

    private void createGetSpaceACLsMocks(Map<String, AclType> acls,
                                         String spaceId,
                                         String storeId,
                                         int status) throws Exception {
        Header header = EasyMock.createMock(Header.class);
        Header[] headers = new Header[acls.size()];
        int i = 0;
        for (String acl : acls.keySet()) {
            headers[i++] = new Header(acl, acls.get(acl).name());
        }

        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            "HttpResponse",
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(status);
        EasyMock.expect(response.getResponseHeaders()).andReturn(headers);

        EasyMock.replay(response, header);

        String fullURL = baseURL + "/acl/" + spaceId + "?storeID=" + storeId;
        EasyMock.expect(restHelper.head(EasyMock.eq(fullURL))).andReturn(
            response);

        replayMocks();
    }

    @Test
    public void testGetSupportedTasks() throws Exception {
        List<String> supportedtaskList = new ArrayList<>();
        supportedtaskList.add("task1");
        String xml = SerializationUtil.serializeList(supportedtaskList);

        String fullURL = baseURL + "/task" + "?storeID=" + storeId;
        RestHttpHelper.HttpResponse response = EasyMock.createMock(
            "HttpResponse",
            RestHttpHelper.HttpResponse.class);
        EasyMock.expect(response.getStatusCode()).andReturn(200);
        EasyMock.expect(response.getResponseBody()).andReturn(xml);
        EasyMock.expect(restHelper.get(fullURL)).andReturn(response);

        EasyMock.replay(response);
        replayMocks();

        List<String> taskList = contentStore.getSupportedTasks();
        Assert.assertEquals(supportedtaskList, taskList);
    }

}
