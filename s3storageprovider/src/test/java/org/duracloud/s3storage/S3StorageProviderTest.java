/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketLifecycleConfiguration;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.TagSet;
import org.duracloud.common.util.IOUtil;
import org.duracloud.storage.domain.RetrievedContent;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.error.ChecksumMismatchException;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.IExpectationSetters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Aug 3, 2010
 */
public class S3StorageProviderTest {

    private AmazonS3 s3Client;
    private InputStream contentStream;

    // Must be 20 char alphanum (and lowercase, to match bucket naming pattern)
    private static final String accessKey = "abcdefghijklmnopqrst";
    private static final String secretKey = "secretKey";
    private static final String spaceId = "space-id";
    private static final String contentId = "content-id";

    private static final String content = "hello-world";
    private static final String hexChecksum = "2095312189753de6ad47dfe20cbe97ec";
    private static final String base64Checksum = "IJUxIYl1PeatR9/iDL6X7A==";

    @After
    public void tearDown() throws IOException {
        if (null != contentStream) {
            contentStream.close();
        }

        if (null != s3Client) {
            verify(s3Client);
        }
    }

    private void setupS3Client() {
        s3Client = createMock("AmazonS3", AmazonS3.class);
    }

    @Test
    public void testGetStorageProviderType() {
        S3StorageProvider provider = new S3StorageProvider(accessKey, secretKey);
        assertEquals(StorageProviderType.AMAZON_S3, provider.getStorageProviderType());
    }

    @Test
    public void testGetContent() {
        GetObjectRequest objectRequest = setupTestGetContent(null);

        assertNotNull(objectRequest);
        assertTrue(objectRequest.getBucketName().contains(spaceId));
        assertEquals(contentId, objectRequest.getS3ObjectId().getKey());
        assertNull(objectRequest.getCustomRequestHeaders());
    }

    @Test
    public void testGetContentRange() {
        String range = "bytes=1-10";
        GetObjectRequest objectRequest = setupTestGetContent(range);

        assertNotNull(objectRequest);
        assertTrue(objectRequest.getBucketName().contains(spaceId));
        assertEquals(contentId, objectRequest.getS3ObjectId().getKey());

        long[] requestRange = objectRequest.getRange();
        assertEquals(1, requestRange[0]);
        assertEquals(10, requestRange[1]);
    }

    private GetObjectRequest setupTestGetContent(String range) {
        setupS3Client();
        S3StorageProvider provider = new S3StorageProvider(s3Client, accessKey, null);

        String bucketName = accessKey + "." + spaceId;
        Bucket bucket = createMock(Bucket.class);
        expect(this.s3Client.listBuckets()).andReturn(Arrays.asList(bucket));
        expect(bucket.getName()).andReturn(bucketName);

        Capture<GetObjectRequest> getObjectRequestCapture = newCapture();
        expect(s3Client.getObject(capture(getObjectRequestCapture)))
                .andReturn(new S3Object());

        replay(s3Client, bucket);

        if (null == range) {
            provider.getContent(spaceId, contentId);
        } else {
            provider.getContent(spaceId, contentId, range);
        }

        return getObjectRequestCapture.getValue();
    }

    @Test
    public void testAddContent() {
        Capture<PutObjectRequest> capturedRequest =
            createS3ClientAddContent(hexChecksum);
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, new HashMap<String, String>());

        String content = "hello";
        contentStream = createStream(content);
        String mimetype = "mimetype";
        String userMetaName = "user-metadata-name";
        String userMetaValue = "user-metadata-value";
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put(userMetaName, userMetaValue);

        String resultChecksum =
            provider.addContent(spaceId,
                                "contentId",
                                mimetype,
                                userMeta,
                                content.length(),
                                hexChecksum,
                                contentStream);

        Assert.assertEquals(hexChecksum, resultChecksum);

        PutObjectRequest request = capturedRequest.getValue();
        Assert.assertNotNull(request);

        ObjectMetadata requestMetadata = request.getMetadata();
        Assert.assertEquals(base64Checksum, requestMetadata.getContentMD5());
        Assert.assertEquals(mimetype, requestMetadata.getContentType());
        Assert.assertEquals(provider.encodeHeaderValue(userMetaValue),
                            requestMetadata.getUserMetadata().get(provider.encodeHeaderKey(userMetaName)));
    }

    @Test
    public void testEventuallyConsistentAddContent() {
        Capture<PutObjectRequest> capturedRequest =
            createS3ClientAddContentWithClientError(hexChecksum);
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, new HashMap<String, String>());

        String content = "hello";
        contentStream = createStream(content);
        String mimetype = "mimetype";
        String userMetaName = "user-metadata-name";
        String userMetaValue = "user-metadata-value";
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put(userMetaName, userMetaValue);

        String resultChecksum =
            provider.addContent(spaceId,
                                "contentId",
                                mimetype,
                                userMeta,
                                content.length(),
                                hexChecksum,
                                contentStream);

        Assert.assertEquals(hexChecksum, resultChecksum);

        PutObjectRequest request = capturedRequest.getValue();
        Assert.assertNotNull(request);

        ObjectMetadata requestMetadata = request.getMetadata();
        Assert.assertEquals(base64Checksum, requestMetadata.getContentMD5());
        Assert.assertEquals(mimetype, requestMetadata.getContentType());
        Assert.assertEquals(provider.encodeHeaderValue(userMetaValue),
                            requestMetadata.getUserMetadata().get(provider.encodeHeaderKey(userMetaName)));
    }

    /*
     * Tests the addContent() call response when an invalid etag value is returned
     * from a call to put content into S3.
     */
    @Test
    public void testAddContentInvalidMimetypeResponse() {
        String content = "hello";
        String contentId = "contentId";

        Capture<PutObjectRequest> capturedRequest =
            createS3ClientAddContentInvalidChecksum(contentId);
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, new HashMap<String, String>());

        contentStream = createStream(content);
        String mimetype = "mimetype";
        Map<String, String> userMeta = new HashMap<>();

        try {
            String resultChecksum =
                provider.addContent(spaceId,
                                    contentId,
                                    mimetype,
                                    userMeta,
                                    content.length(),
                                    hexChecksum,
                                    contentStream);
            fail("Checksum mismatch exception expected. Instead checksum '" +
                 resultChecksum + "' was returned");
        } catch (ChecksumMismatchException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void testStorageClassStandard() {
        doTestStorageClass(StorageClass.Standard.toString());
    }

    private void doTestStorageClass(String expected) {
        Map<String, String> options = new HashMap<>();

        Capture<PutObjectRequest> capturedRequest =
            createS3ClientAddContent(hexChecksum);
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, options);

        contentStream = createStream(content);
        provider.addContent(spaceId,
                            "contentId",
                            "mime",
                            null,
                            content.length(),
                            hexChecksum,
                            contentStream);

        PutObjectRequest request = capturedRequest.getValue();
        Assert.assertNotNull(request);

        String requestStorageClass = request.getStorageClass();
        Assert.assertNotNull(requestStorageClass);
        Assert.assertEquals(expected, requestStorageClass);
    }

    private Capture<PutObjectRequest> createS3ClientAddContent(String checksum) {
        setupS3Client();
        addListBucketsMock();

        PutObjectResult result = createMock("PutObjectResult",
                                                     PutObjectResult.class);
        expect(result.getETag()).andReturn(checksum);
        replay(result);

        Capture<PutObjectRequest> capturedRequest =
            Capture.newInstance(CaptureType.FIRST);
        expect(s3Client.putObject(capture(capturedRequest)))
                .andReturn(result);

        replay(s3Client);
        return capturedRequest;
    }

    private Capture<PutObjectRequest> createS3ClientAddContentWithClientError(String checksum) {
        setupS3Client();
        addListBucketsMock();

        PutObjectResult result = createMock("PutObjectResult",
                                                     PutObjectResult.class);

        AmazonS3Exception ex = new AmazonS3Exception("message");
        ex.setErrorCode("errorCode");
        ex.setStatusCode(503);
        ex.setErrorMessage("message");

        expect(result.getETag()).andThrow(ex);
        replay(result);

        ObjectMetadata objectMetadata =
            createMock("ObjectMetadata", ObjectMetadata.class);
        expect(objectMetadata.getETag()).andReturn("\"oldchecksum\"");
        expect(objectMetadata.getETag())
                .andReturn("\"" + checksum + "\"");
        replay(objectMetadata);

        expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andThrow(new AmazonClientException("not found."));

        expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andReturn(objectMetadata)
                .times(2);

        Capture<PutObjectRequest> capturedRequest =
            Capture.newInstance(CaptureType.FIRST);
        expect(s3Client.putObject(capture(capturedRequest)))
                .andReturn(result);

        replay(s3Client);
        return capturedRequest;
    }

    private Capture<PutObjectRequest> createS3ClientAddContentInvalidChecksum(
        String contentId) {
        setupS3Client();
        addListBucketsMock();

        PutObjectResult result = createMock("PutObjectResult",
                                                     PutObjectResult.class);
        expect(result.getETag()).andReturn("invalid-checksum-value");
        replay(result);

        Capture<PutObjectRequest> capturedRequest = Capture.newInstance(CaptureType.FIRST);
        expect(s3Client.putObject(capture(capturedRequest)))
                .andReturn(result);

        replay(s3Client);
        return capturedRequest;
    }

    private void addListBucketsMock() {
        addListBucketsMock(null, Arrays.asList(spaceId, "dest-space-id"));
    }

    private void addListBucketsMock(Integer times, List<String> spaceIds) {
        List<Bucket> buckets = new ArrayList<>();
        for (String sid : spaceIds) {
            buckets.add(new Bucket(accessKey + "." + sid));
        }
        IExpectationSetters<List<Bucket>> expectationSetter = expect(s3Client.listBuckets())
                                                                      .andReturn(buckets);

        if (times == null) {
            expectationSetter.anyTimes();
        } else {
            expectationSetter.times(times);
        }

    }

    private InputStream createStream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }

    @Test
    public void testCopyContent() {
        Capture<CopyObjectRequest> capturedRequest =
            createS3ClientCopyContent(hexChecksum);
        S3StorageProvider provider = new S3StorageProvider(s3Client,
                                                           accessKey,
                                                           null);

        String srcSpaceId = "space-id";
        String srcContentId = "contentId";
        String destSpaceId = "dest-space-id";
        String destContentId = "destContentId";
        String md5 = provider.copyContent(srcSpaceId,
                                          srcContentId,
                                          destSpaceId,
                                          destContentId);

        Assert.assertNotNull(md5);

        CopyObjectRequest request = capturedRequest.getValue();
        Assert.assertNotNull(request);

        String srcBucket = request.getSourceBucketName();
        String srcKey = request.getSourceKey();
        String destBucket = request.getDestinationBucketName();
        String destKey = request.getDestinationKey();
        String storageClass = request.getStorageClass();

        Assert.assertNotNull(srcBucket);
        Assert.assertNotNull(srcKey);
        Assert.assertNotNull(destBucket);
        Assert.assertNotNull(destKey);
        Assert.assertNotNull(storageClass);

        String lowerSrcSpaceId = (accessKey + "." + srcSpaceId).toLowerCase();
        String lowerDestSpaceId = (accessKey + "." + destSpaceId).toLowerCase();

        Assert.assertEquals(lowerSrcSpaceId, srcBucket);
        Assert.assertEquals(srcContentId, srcKey);
        Assert.assertEquals(lowerDestSpaceId, destBucket);
        Assert.assertEquals(destContentId, destKey);
        Assert.assertEquals(StorageClass.Standard.toString(), storageClass);
    }

    private Capture<CopyObjectRequest> createS3ClientCopyContent(String checksum) {
        setupS3Client();

        addListBucketsMock();
        expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andReturn(null);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata(S3StorageProvider.encodeHeaderKey(StorageProvider.PROPERTIES_CONTENT_CHECKSUM),
                                       S3StorageProvider.encodeHeaderValue(checksum));
        expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andReturn(objectMetadata);

        CopyObjectResult result = createMock("CopyObjectResult",
                                                      CopyObjectResult.class);
        expect(result.getETag()).andReturn(checksum);
        replay(result);

        Capture<CopyObjectRequest> capturedRequest =
            Capture.newInstance(CaptureType.FIRST);
        expect(s3Client.copyObject(capture(capturedRequest)))
                .andReturn(result);

        replay(s3Client);
        return capturedRequest;
    }

    @Test
    public void testGetSpaceCount1000() {
        MockS3StorageProvider provider = new MockS3StorageProvider();
        String count = provider.getSpaceCount(spaceId, 1000);
        assertEquals("1000+", count);

        count = provider.getSpaceCount(spaceId, 1500);
        assertEquals("2000+", count);

        count = provider.getSpaceCount(spaceId, 10000);
        assertEquals("10000+", count);
    }

    private class MockS3StorageProvider extends S3StorageProvider {

        public MockS3StorageProvider() {
            super(accessKey, "secretKey");
        }

        @Override
        public List<String> getSpaceContentsChunked(String spaceId,
                                                    String prefix,
                                                    long maxResults,
                                                    String marker) {
            List<String> contents = new ArrayList<String>();
            for (int i = 0; i < maxResults; i++) {
                contents.add("contentID" + i);
            }
            return contents;
        }

    }

    @Test
    public void testDoesContentExist() {
        setupS3Client();

        expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andThrow(new AmazonClientException("message"));

        ObjectMetadata objectMetadata = new ObjectMetadata();
        String etag = "etag";
        objectMetadata.setHeader(Headers.ETAG, etag);
        expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
                .andReturn(objectMetadata);

        replay(s3Client);

        Map<String, String> options = new HashMap<String, String>();
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, options);

        String resultEtag =
            provider.doesContentExistWithExpectedChecksum("bucketname",
                                                          "contentId",
                                                          etag);
        assertNotNull(resultEtag);
        assertEquals(etag, resultEtag);
    }

    @Test
    public void testGetSpaceContentsChunked() throws Exception {
        setupS3Client();
        String prefix = null;
        long maxResults = 2;
        String marker = null;

        addListBucketsMock();

        ObjectListing objectListing =
            createMock("ObjectListing", ObjectListing.class);
        setUpListObjects(objectListing, 1);
        replay(s3Client, objectListing);

        S3StorageProvider provider = getProvider();
        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   prefix,
                                                                   maxResults,
                                                                   marker);
        Assert.assertNotNull(contentIds);
        Assert.assertEquals("item0", contentIds.get(0));

        verify(s3Client, objectListing);
    }

    private void setUpListObjects(ObjectListing objectListing, int numItems) {
        List<S3ObjectSummary> objectSummaries = new ArrayList<>();
        for (int i = 0; i < numItems; i++) {
            S3ObjectSummary summary = new S3ObjectSummary();
            summary.setKey("item" + i);
            objectSummaries.add(summary);
        }

        expect(
            s3Client.listObjects(EasyMock.isA(ListObjectsRequest.class)))
                .andReturn(objectListing);
        expect(objectListing.getObjectSummaries())
                .andReturn(objectSummaries);
    }

    private S3StorageProvider getProvider() {
        Map<String, String> options = new HashMap<>();
        return new S3StorageProvider(s3Client, accessKey, options);
    }

    @Test
    public void testGetAllSpaceProperties() throws Exception {
        setupS3Client();

        addListBucketsMock();

        Map<String, String> bucketTags = new HashMap<>();
        bucketTags.put("tag-one", "tag-one-value");
        bucketTags.put("tag-two", "tagtwo+test.com");
        BucketTaggingConfiguration tagConfig =
            new BucketTaggingConfiguration().withTagSets(new TagSet(bucketTags));
        expect(
            s3Client.getBucketTaggingConfiguration(EasyMock.isA(String.class)))
                .andReturn(tagConfig);

        ObjectListing objectListing =
            createMock("ObjectListing", ObjectListing.class);
        setUpListObjects(objectListing, 1);
        setUpListObjects(objectListing, 0);

        replay(s3Client, objectListing);

        S3StorageProvider provider = getProvider();
        Map<String, String> spaceProps = provider.getAllSpaceProperties(spaceId);
        Assert.assertNotNull(spaceProps);
        Assert.assertEquals("tag-one-value", spaceProps.get("tag-one"));
        Assert.assertEquals("tagtwo@test.com", spaceProps.get("tag-two"));
        Assert.assertEquals(String.valueOf(1),
                            spaceProps.get(
                                StorageProvider.PROPERTIES_SPACE_COUNT));

        verify(s3Client, objectListing);
    }

    @Test
    public void testDoSetSpaceProperties() {
        setupS3Client();
        String spaceId = "space-id";

        addListBucketsMock();

        // Add props as tags
        expect(
            s3Client.getBucketTaggingConfiguration(EasyMock.isA(String.class)))
                .andThrow(new NotFoundException(spaceId));
        Capture<BucketTaggingConfiguration> tagConfigCap =
            Capture.newInstance(CaptureType.FIRST);
        s3Client.setBucketTaggingConfiguration(EasyMock.isA(String.class),
                                               capture(tagConfigCap));
        EasyMock.expectLastCall().once();

        S3StorageProvider provider = getProvider();

        Bucket bucket = new Bucket();
        bucket.setName(provider.getNewBucketName(spaceId));
        bucket.setCreationDate(new Date());
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(bucket);

        replay(s3Client);

        Map<String, String> spaceProps = new HashMap<>();
        spaceProps.put("one", "one-value");
        spaceProps.put("two", "two@value.com");
        provider.doSetSpaceProperties(spaceId, spaceProps);

        BucketTaggingConfiguration tagConfig = tagConfigCap.getValue();
        Assert.assertNotNull(tagConfig);
        Map<String, String> props =
            tagConfig.getAllTagSets().get(0).getAllTags();
        Assert.assertNotNull(props);
        Assert.assertEquals(3, props.size());
        Assert.assertEquals("one-value", props.get("one"));
        Assert.assertEquals("two+value.com", props.get("two"));
        Assert.assertNotNull(
            props.get(StorageProvider.PROPERTIES_SPACE_CREATED));

        verify(s3Client);
    }

    /**
     * Verifies the fix for https://jira.duraspace.org/browse/DURACLOUD-936
     */
    @Test
    public void testDuracloud936() {
        testCreateSpace("space.id");
    }

    @Test
    public void testCreateSpace() {
        testCreateSpace("spaceid");
    }

    protected void testCreateSpace(String spaceId) {
        setupS3Client();
        List<String> spaceIds = new LinkedList<>();
        spaceIds.add("space-id");
        addListBucketsMock(1, spaceIds);
        String bucketName = accessKey + "." + spaceId;

        S3StorageProvider provider = getProvider();
        Bucket bucket = createMock(Bucket.class);
        expect(bucket.getName()).andReturn(bucketName);
        expect(bucket.getCreationDate()).andReturn(new Date());
        expect(this.s3Client.createBucket(bucketName)).andReturn(bucket);

        s3Client.deleteBucketLifecycleConfiguration(bucketName);
        EasyMock.expectLastCall().once();
        Capture<BucketLifecycleConfiguration> lifecycleConfigCapture =
            Capture.newInstance(CaptureType.FIRST);
        s3Client.setBucketLifecycleConfiguration(eq(bucketName),
                                                 capture(lifecycleConfigCapture));
        EasyMock.expectLastCall().once();

        expect(this.s3Client.listBuckets()).andReturn(Arrays.asList(bucket));

        List<String> spaceIds2 = new LinkedList<>(spaceIds);
        spaceIds2.add(spaceId);
        addListBucketsMock(2, spaceIds2);
        expect(s3Client.getBucketTaggingConfiguration(bucketName))
                .andReturn(new BucketTaggingConfiguration());
        s3Client.setBucketTaggingConfiguration(eq(bucketName),
                                               EasyMock.isA(BucketTaggingConfiguration.class));
        EasyMock.expectLastCall().once();

        expect(s3Client.listObjects(EasyMock.isA(ListObjectsRequest.class)))
                .andReturn(new ObjectListing());

        replay(s3Client, bucket);

        provider.createSpace(spaceId);

        BucketLifecycleConfiguration lifecycleConfig = lifecycleConfigCapture.getValue();
        BucketLifecycleConfiguration.Transition transition =
            lifecycleConfig.getRules().get(0).getTransitions().get(0);
        assertEquals(30, transition.getDays());
        assertEquals(StorageClass.StandardInfrequentAccess, transition.getStorageClass());

        verify(s3Client, bucket);
    }

    @Test
    public void testEncodeDecodeHeaderKey() throws Exception {
        String key = "key";
        String encodedKey = S3StorageProvider.encodeHeaderKey(key);
        Assert.assertEquals(encodedKey, "key*");
        Assert.assertEquals(key,
                            S3StorageProvider.decodeHeaderKey(encodedKey));
    }

    @Test
    public void testEncodeDecodeHeaderValue() throws Exception {
        String value = "Xanthoparmeliacoloradoënsis";
        String encodedValue = S3StorageProvider.encodeHeaderValue(value);
        Assert.assertEquals(encodedValue,
                            "UTF-8''Xanthoparmeliacoloradoe%CC%88nsis");
        Assert.assertEquals(value,
                            S3StorageProvider.decodeHeaderValue(encodedValue));

    }

    private String getBucketName(String hiddenSpace) {
        return S3StorageProvider.HIDDEN_SPACE_PREFIX + accessKey + "." + hiddenSpace;
    }

    @Test
    public void testCreateHiddenSpace() {
        setupS3Client();
        S3StorageProvider provider = getProvider();
        String hiddenSpace = "my-hidden-space";
        String bucketName = getBucketName(hiddenSpace);
        Capture<BucketLifecycleConfiguration> blcConfig = newCapture();
        Bucket bucket = createMock(Bucket.class);

        expect(s3Client.createBucket(bucketName)).andReturn(bucket);
        s3Client.setBucketLifecycleConfiguration(eq(bucketName), capture(blcConfig));
        replay(s3Client);

        String spaceId = provider.createHiddenSpace(hiddenSpace, 1);

        assertEquals("returned spaceId is not correct", hiddenSpace, spaceId);
        BucketLifecycleConfiguration config = blcConfig.getValue();
        assertEquals("one rule is configured", 1, config.getRules().size());
        BucketLifecycleConfiguration.Rule rule = config.getRules().get(0);
        assertEquals("rule is enabled", BucketLifecycleConfiguration.ENABLED, rule.getStatus());
        assertEquals("expires = 1", 1, rule.getExpirationInDays());
    }

    @Test
    public void testAddHiddenContent() throws Exception {
        setupS3Client();
        S3StorageProvider provider = getProvider();
        String hiddenSpace = "my-hidden-space";
        String etag = "etag";
        String bucketName = getBucketName(hiddenSpace);
        Bucket bucket = createMock("ListedBucket", Bucket.class);
        String data = "contents";
        InputStream content = IOUtil.writeStringToStream(data);
        String mimeType = "mime";
        Capture<PutObjectRequest> requestCapture = newCapture();
        PutObjectResult result = createMock("AddHiddenContentResult", PutObjectResult.class);

        expect(result.getETag()).andReturn(etag);
        expect(s3Client.putObject(capture(requestCapture))).andReturn(result);
        expect(bucket.getName()).andReturn(bucketName);
        expect(s3Client.listBuckets()).andReturn(Arrays.asList(bucket));
        replay(s3Client, bucket, result);

        String resultETag = provider.addHiddenContent(hiddenSpace, contentId, mimeType, content);

        PutObjectRequest request = requestCapture.getValue();
        assertEquals("etag must be returned", etag, resultETag);
        assertEquals("unexpected bucket name", bucketName, request.getBucketName());
        assertEquals("unexpected mimetype", mimeType, request.getMetadata().getContentType());
        assertEquals("unexpected access control list", CannedAccessControlList.Private, request.getCannedAcl());
        verify(result, bucket);
    }

    @Test
    public void testGetHiddenContent() {
        setupS3Client();
        S3StorageProvider provider = getProvider();
        String hiddenSpace = "my-hidden-space";
        String bucketName = getBucketName(hiddenSpace);
        Bucket bucket = createMock("ListedBucket", Bucket.class);
        ObjectMetadata metadata = new ObjectMetadata();

        expect(bucket.getName()).andReturn(bucketName);
        expect(s3Client.listBuckets()).andReturn(Arrays.asList(bucket));

        Capture<GetObjectRequest> requestCapture = newCapture();
        S3Object result = createMock("HiddenObject", S3Object.class);
        S3ObjectInputStream is = createMock("S3ObjectInputstream", S3ObjectInputStream.class);
        expect(result.getObjectContent()).andReturn(is);
        expect(result.getObjectMetadata()).andReturn(metadata);
        expect(s3Client.getObject(capture(requestCapture))).andReturn(result);
        replay(s3Client, bucket, result, is);

        RetrievedContent retrievedContent = provider.getContent(hiddenSpace, contentId);
        GetObjectRequest request = requestCapture.getValue();
        assertEquals("bucket name does not match expectation", bucketName, request.getBucketName());
        assertEquals("content id does not match expectation", contentId, request.getKey());

        assertEquals("s3 object inputstream was not set on retrieved content", is, retrievedContent.getContentStream());
        verify(result, bucket, result);
    }
}
