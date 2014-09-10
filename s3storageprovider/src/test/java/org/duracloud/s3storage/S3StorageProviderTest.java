/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.BucketTaggingConfiguration;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.StorageClass;
import com.amazonaws.services.s3.model.TagSet;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.NotFoundException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: Aug 3, 2010
 */
public class S3StorageProviderTest {

    private AmazonS3Client s3Client;
    private InputStream contentStream;

    // Must be 20 char alphanum (and lowercase, to match bucket naming pattern)
    private static final String accessKey = "abcdefghijklmnopqrst";
    private static final String spaceId = "space-id";

    private static final String content = "hello-world";
    private static final String hexChecksum = "2095312189753de6ad47dfe20cbe97ec";
    private static final String base64Checksum = "IJUxIYl1PeatR9/iDL6X7A==";

    @After
    public void tearDown() throws IOException {
        if (null != contentStream) {
            contentStream.close();
        }

        if (null != s3Client) {
            EasyMock.verify(s3Client);
        }
    }

    @Test
    public void testAddContent() {
        Capture<PutObjectRequest> capturedRequest =
            createS3ClientAddContent(hexChecksum);
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, new HashMap());

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
        Assert.assertEquals(userMetaValue,
                            requestMetadata.getUserMetadata().get(userMetaName));
    }

    @Test
    public void testStorageClassStandard() {
        String expected = StorageClass.Standard.toString();
        doTestStorageClass(StorageClass.Standard.toString(), expected);
        doTestStorageClass("xx", expected);
    }

    @Test
    public void testStorageClassRRS() {
        String expected = StorageClass.ReducedRedundancy.toString();
        doTestStorageClass(StorageClass.ReducedRedundancy.toString(), expected);
        doTestStorageClass("ReducEDreDUNdanCY", expected);
        doTestStorageClass("ReducED", expected);
        doTestStorageClass("rrs", expected);
    }

    @Test
    public void testStorageClassNull() {
        String expected = StorageClass.Standard.toString();
        doTestStorageClass(null, expected);
    }

    private void doTestStorageClass(String storageClass, String expected) {
        Map<String, String> options = new HashMap<>();
        options.put(StorageAccount.OPTS.STORAGE_CLASS.name(), storageClass);

        Capture<PutObjectRequest> capturedRequest =
            createS3ClientAddContent(hexChecksum);
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey,options);


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
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        addListBucketsMock();

        PutObjectResult result = EasyMock.createMock("PutObjectResult",
                                                     PutObjectResult.class);
        EasyMock.expect(result.getETag()).andReturn(checksum);
        EasyMock.replay(result);

        Capture<PutObjectRequest> capturedRequest = new Capture<>();
        EasyMock.expect(s3Client.putObject(EasyMock.capture(capturedRequest)))
            .andReturn(result);

        EasyMock.replay(s3Client);
        return capturedRequest;
    }

    private void addListBucketsMock() {
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(new Bucket(accessKey + "." + spaceId));
        buckets.add(new Bucket(accessKey + ".dest-space-id"));
        EasyMock.expect(s3Client.listBuckets())
                .andReturn(buckets)
                .anyTimes();
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
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);

        addListBucketsMock();
        EasyMock.expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
            .andReturn(null);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.addUserMetadata(StorageProvider.PROPERTIES_CONTENT_CHECKSUM,
                                       checksum);
        EasyMock.expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
            .andReturn(objectMetadata);

        CopyObjectResult result = EasyMock.createMock("CopyObjectResult",
                                                      CopyObjectResult.class);
        EasyMock.expect(result.getETag()).andReturn(checksum);
        EasyMock.replay(result);

        Capture<CopyObjectRequest> capturedRequest =
            new Capture<CopyObjectRequest>();
        EasyMock.expect(s3Client.copyObject(EasyMock.capture(capturedRequest)))
            .andReturn(result);

        EasyMock.replay(s3Client);
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
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);

        EasyMock.expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
            .andThrow(new AmazonClientException("message"));

        ObjectMetadata objectMetadata = new ObjectMetadata();
        String etag = "etag";
        objectMetadata.setHeader(Headers.ETAG, etag);
        EasyMock.expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
            .andReturn(objectMetadata);

        EasyMock.replay(s3Client);

        Map<String, String> options = new HashMap<String,String>();
        S3StorageProvider provider =
            new S3StorageProvider(s3Client, accessKey, options);

        String resultEtag = provider.doesContentExist("bucketname", "contentId");
        assertNotNull(resultEtag);
        assertEquals(etag, resultEtag);
    }

    @Test
    public void testGetSpaceContentsChunked() throws Exception {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        String prefix = null;
        long maxResults = 2;
        String marker = null;

        addListBucketsMock();

        ObjectListing objectListing =
            EasyMock.createMock("ObjectListing", ObjectListing.class);
        setUpListObjects(objectListing, 1);
        EasyMock.replay(s3Client, objectListing);

        S3StorageProvider provider = getProvider();
        List<String> contentIds = provider.getSpaceContentsChunked(spaceId,
                                                                   prefix,
                                                                   maxResults,
                                                                   marker);
        Assert.assertNotNull(contentIds);
        Assert.assertEquals("item0", contentIds.get(0));

        EasyMock.verify(s3Client, objectListing);
    }

    private void setUpListObjects(ObjectListing objectListing, int numItems) {
        List<S3ObjectSummary> objectSummaries = new ArrayList<>();
        for(int i=0; i<numItems; i++) {
            S3ObjectSummary summary = new S3ObjectSummary();
            summary.setKey("item" + i);
            objectSummaries.add(summary);
        }

        EasyMock.expect(
            s3Client.listObjects(EasyMock.isA(ListObjectsRequest.class)))
                .andReturn(objectListing);
        EasyMock.expect(objectListing.getObjectSummaries())
                .andReturn(objectSummaries);
    }

    private S3StorageProvider getProvider() {
        Map<String, String> options = new HashMap<>();
        return new S3StorageProvider(s3Client, accessKey, options);
    }

    @Test
    public void testGetAllSpaceProperties() throws Exception {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);

        addListBucketsMock();

        Map<String, String> bucketTags = new HashMap<>();
        bucketTags.put("tag-one", "tag-one-value");
        bucketTags.put("tag-two", "tagtwo+test.com");
        BucketTaggingConfiguration tagConfig =
            new BucketTaggingConfiguration().withTagSets(new TagSet(bucketTags));
        EasyMock.expect(
            s3Client.getBucketTaggingConfiguration(EasyMock.isA(String.class)))
                .andReturn(tagConfig);

        ObjectListing objectListing =
            EasyMock.createMock("ObjectListing", ObjectListing.class);
        setUpListObjects(objectListing, 1);
        setUpListObjects(objectListing, 0);

        EasyMock.replay(s3Client, objectListing);

        S3StorageProvider provider = getProvider();
        Map<String, String> spaceProps = provider.getAllSpaceProperties(spaceId);
        Assert.assertNotNull(spaceProps);
        Assert.assertEquals("tag-one-value", spaceProps.get("tag-one"));
        Assert.assertEquals("tagtwo@test.com", spaceProps.get("tag-two"));
        Assert.assertEquals(String.valueOf(1),
                            spaceProps.get(
                                StorageProvider.PROPERTIES_SPACE_COUNT));

        EasyMock.verify(s3Client, objectListing);
    }

    @Test
    public void testDoSetSpaceProperties() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        String spaceId = "space-id";

        addListBucketsMock();

        // Add props as tags
        EasyMock.expect(
            s3Client.getBucketTaggingConfiguration(EasyMock.isA(String.class)))
                .andThrow(new NotFoundException(spaceId));
        Capture<BucketTaggingConfiguration> tagConfigCap = new Capture<>();
        s3Client.setBucketTaggingConfiguration(EasyMock.isA(String.class),
                                               EasyMock.capture(tagConfigCap));
        EasyMock.expectLastCall();

        S3StorageProvider provider = getProvider();

        Bucket bucket = new Bucket();
        bucket.setName(provider.getNewBucketName(spaceId));
        bucket.setCreationDate(new Date());
        List<Bucket> buckets = new ArrayList<>();
        buckets.add(bucket);

        EasyMock.replay(s3Client);

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

        EasyMock.verify(s3Client);
    }

}
