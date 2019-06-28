/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.duracloud.swiftstorage;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.apache.commons.lang.StringUtils;
import org.duracloud.common.util.DateUtil;
import org.duracloud.storage.domain.StorageProviderType;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Andy Foster
 * Date: Apr 24, 2019
 */
public class SwiftStorageProviderTest {

    private AmazonS3 s3Client;

    // In OpenStack, access keys are 32 bytes in length.
    private static final String accessKey = "c09417d8d454dff21664a30f1e734149";
    private static final String secretKey = "secretKey";
    private static final String contentId = "content-id";
    private static final String spaceId = "spaceid";

    private void setupS3Client() {
        s3Client = createMock("AmazonS3", AmazonS3.class);
    }

    private SwiftStorageProvider getProvider() {
        return new SwiftStorageProvider(s3Client, accessKey);
    }

    private String truncateKey() {
        return StringUtils.left(accessKey, 20);
    }

    private String getPropsBucketName() {
        return SwiftStorageProvider.HIDDEN_SPACE_PREFIX + truncateKey() +
            "." + StorageProvider.PROPERTIES_BUCKET;
    }

    private String formattedDate(Date date) {
        return DateUtil.convertToString(date.getTime());
    }

    @Test
    public void testGetStorageProviderType() {
        SwiftStorageProvider provider = new SwiftStorageProvider(accessKey, secretKey, null);
        assertEquals(StorageProviderType.SWIFT_S3, provider.getStorageProviderType());
    }

    @Test
    public void testGetNewBucketName() {
        SwiftStorageProvider provider = new SwiftStorageProvider(accessKey, secretKey, null);
        String bucketName = provider.getNewBucketName(spaceId);
        assertEquals(bucketName, truncateKey() + ".spaceid");
    }

    @Test
    public void testGetSpaceId() {
        SwiftStorageProvider provider = new SwiftStorageProvider(accessKey, secretKey, null);
        String testSpaceId = provider.getSpaceId(truncateKey() + ".test-space");
        assertEquals(testSpaceId, "test-space");
    }

    private String getBucketName(String hiddenSpace) {
        return SwiftStorageProvider.HIDDEN_SPACE_PREFIX + truncateKey() + "." + hiddenSpace;
    }

    @Test
    public void testCreateHiddenSpace() {
        setupS3Client();
        SwiftStorageProvider provider = getProvider();
        String hiddenSpace = "my-hidden-space";
        String bucketName = getBucketName(hiddenSpace);
        Bucket bucket = createMock(Bucket.class);

        expect(s3Client.createBucket(bucketName)).andReturn(bucket);
        replay(s3Client);

        String spaceId = provider.createHiddenSpace(hiddenSpace, 0);
        assertEquals("returned spaceId is not correct", hiddenSpace, spaceId);
    }

    @Test
    public void testCreateSpace() {
        Date date = new Date();
        setupS3Client();
        String bucketName = truncateKey() + "." + spaceId;
        String propsBucketName = getPropsBucketName();
        SwiftStorageProvider provider = getProvider();

        Bucket bucket = createMock(Bucket.class);
        Bucket propsBucket = createMock(Bucket.class);
        expect(bucket.getName()).andReturn(bucketName).anyTimes();
        expect(bucket.getCreationDate()).andReturn(date);
        expect(propsBucket.getName()).andReturn(propsBucketName).anyTimes();
        expect(s3Client.createBucket(bucketName)).andReturn(bucket);
        expect(s3Client.createBucket(propsBucketName)).andReturn(propsBucket).anyTimes();
        expect(s3Client.listBuckets()).andReturn(new ArrayList<Bucket>());
        expect(s3Client.listBuckets()).andReturn(Arrays.asList(bucket));
        expect(s3Client.listBuckets()).andReturn(Arrays.asList(propsBucket));
        expect(s3Client.putObject(
            propsBucketName, spaceId, "{space-created=" + formattedDate(date) + "}"
        )).andReturn(new PutObjectResult());

        replay(s3Client, bucket, propsBucket);

        provider.createSpace(spaceId);

        verify(s3Client, bucket, propsBucket);
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

    @Test
    public void testGetAllSpaceProperties() {
        setupS3Client();
        String bucketName = truncateKey() + "." + spaceId;
        String propsBucketName = getPropsBucketName();
        String properties =
            "{key1=value1, key2=value2, key3=value+3}";

        Bucket bucket = createMock(Bucket.class);
        Bucket propsBucket = createMock(Bucket.class);
        expect(bucket.getName()).andReturn(bucketName).anyTimes();
        expect(propsBucket.getName()).andReturn(propsBucketName);
        expect(s3Client.listBuckets()).andReturn(Arrays.asList(bucket, propsBucket)).anyTimes();
        expect(s3Client.getObjectAsString(propsBucketName, spaceId)).andReturn(properties);
        ObjectListing objectListing = createMock("ObjectListing", ObjectListing.class);
        setUpListObjects(objectListing, 1);
        setUpListObjects(objectListing, 0);

        SwiftStorageProvider provider = getProvider();

        replay(s3Client, bucket, propsBucket, objectListing);

        Map<String, String> spaceProps = provider.getAllSpaceProperties(spaceId);
        assertNotNull(spaceProps);
        assertEquals("value1", spaceProps.get("key1"));
        assertEquals("value2", spaceProps.get("key2"));
        assertEquals("value@3", spaceProps.get("key3"));
        assertEquals(String.valueOf(1),
                     spaceProps.get(StorageProvider.PROPERTIES_SPACE_COUNT));

        verify(s3Client, bucket, propsBucket, objectListing);
    }

    @Test
    public void testDoSetSpaceProperties() {
        Date date = new Date();
        setupS3Client();
        String bucketName = truncateKey() + "." + spaceId;
        String propsBucketName = getPropsBucketName();
        String properties =
            "{key1=value1, key2=value2, key3=value+3}";
        String propsWithDate =
            "{key1=value1, key2=value2, space-created=" +
            formattedDate(date) + ", key3=value+3}";

        Date mockDate = createMock(Date.class);

        expect(mockDate.getTime()).andReturn(date.getTime());
        Bucket bucket = createMock(Bucket.class);
        Bucket propsBucket = createMock(Bucket.class);
        expect(bucket.getName()).andReturn(bucketName).anyTimes();
        expect(propsBucket.getName()).andReturn(propsBucketName).anyTimes();
        expect(s3Client.listBuckets()).andReturn(Arrays.asList(bucket, propsBucket)).anyTimes();
        expect(s3Client.getObjectAsString(propsBucketName, spaceId)).andReturn(properties);
        ObjectListing objectListing = createMock("ObjectListing", ObjectListing.class);
        setUpListObjects(objectListing, 1);
        setUpListObjects(objectListing, 0);
        expect(s3Client.putObject(
            propsBucketName, spaceId, propsWithDate
        )).andReturn(createMock(PutObjectResult.class));

        SwiftStorageProvider provider = getProvider();

        replay(s3Client, bucket, propsBucket, objectListing);

        Map<String, String> spaceProps = new HashMap<>();
        spaceProps.put("key1", "value1");
        spaceProps.put("key2", "value2");
        spaceProps.put("key3", "value@3");
        provider.doSetSpaceProperties(spaceId, spaceProps);

        verify(s3Client, bucket, propsBucket, objectListing);
    }

    @Test
    public void testPrepContentProperties() {
        Date date = new Date();
        setupS3Client();
        Map<String, String> userMetadata = new HashMap<>();
        userMetadata.put("header1", "value1");
        userMetadata.put("header2", "value2");

        Map<String, Object> responseMetadata = new HashMap<>();
        responseMetadata.put("x-trans-id", "abc");
        responseMetadata.put("x-openstack-request-id", "abc");

        ObjectMetadata objectMetadata = createMock("ObjectMetadata", ObjectMetadata.class);
        expect(objectMetadata.getRawMetadata()).andReturn(responseMetadata);
        expect(objectMetadata.getUserMetadata()).andReturn(userMetadata);
        expect(objectMetadata.getContentType()).andReturn("fakeContentType");
        expect(objectMetadata.getContentEncoding()).andReturn("fakeEncoding");
        expect(objectMetadata.getContentLength()).andReturn(1234L);
        expect(objectMetadata.getETag()).andReturn("fakeETag");
        expect(objectMetadata.getLastModified()).andReturn(date);

        SwiftStorageProvider provider = getProvider();

        replay(s3Client, objectMetadata);

        Map<String, String> preppedProperties = provider.prepContentProperties(objectMetadata);

        assertNotNull(preppedProperties);
        assertEquals("value1", preppedProperties.get("header1"));
        assertEquals("value2", preppedProperties.get("header2"));
        assertEquals("1234", preppedProperties.get("content-size"));
        assertEquals("1234", preppedProperties.get("Content-Length"));
        assertEquals("fakeETag", preppedProperties.get("content-md5"));
        assertEquals("fakeETag", preppedProperties.get("content-checksum"));
        assertEquals("fakeETag", preppedProperties.get("ETag"));
        assertEquals("fakeContentType", preppedProperties.get("content-mimetype"));
        assertEquals("fakeContentType", preppedProperties.get("Content-Type"));
        assertEquals("fakeEncoding", preppedProperties.get("Content-Encoding"));
        assertEquals(formattedDate(date), preppedProperties.get("Last-Modified"));
        assertEquals(formattedDate(date), preppedProperties.get("content-modified"));
        // Ensure the Swift metadata has been removed
        assertNull(preppedProperties.get("x-trans-id"));
        assertNull(preppedProperties.get("x-openstack-request-id"));

        verify(s3Client, objectMetadata);
    }
}