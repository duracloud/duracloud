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
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.CopyObjectResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.StorageClass;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        Map<String, String> options = new HashMap<String,String>();
        options.put(StorageAccount.OPTS.STORAGE_CLASS.name(), storageClass);

        Capture<PutObjectRequest> capturedRequest = createS3ClientAddContent();
        S3StorageProvider provider = new S3StorageProvider(s3Client,
                                                           "accessKey",
                                                           options);

        String content = "hello";
        contentStream = createStream(content);
        provider.addContent("spaceId",
                            "contentId",
                            "mime",
                            content.length(),
                            "checksum",
                            contentStream);

        PutObjectRequest request = capturedRequest.getValue();
        Assert.assertNotNull(request);

        String requestStorageClass = request.getStorageClass();
        Assert.assertNotNull(requestStorageClass);
        Assert.assertEquals(expected, requestStorageClass);
    }


    private Capture<PutObjectRequest> createS3ClientAddContent() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(true);

        PutObjectResult result = EasyMock.createMock("PutObjectResult",
                                                     PutObjectResult.class);
        EasyMock.expect(result.getETag()).andReturn("checksum");
        EasyMock.replay(result);

        Capture<PutObjectRequest> capturedRequest = new Capture<PutObjectRequest>();
        EasyMock.expect(s3Client.putObject(EasyMock.capture(capturedRequest)))
            .andReturn(result);

        EasyMock.replay(s3Client);
        return capturedRequest;
    }

    private InputStream createStream(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }

    @Test
    public void testCopyContent() {
        Capture<CopyObjectRequest> capturedRequest =
            createS3ClientCopyContent();
        String accessKey = "accessKey";
        S3StorageProvider provider = new S3StorageProvider(s3Client,
                                                           accessKey,
                                                           null);

        String srcSpaceId = "spaceId";
        String srcContentId = "contentId";
        String destSpaceId = "destSpaceId";
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

    private Capture<CopyObjectRequest> createS3ClientCopyContent() {
        s3Client = EasyMock.createMock("AmazonS3Client", AmazonS3Client.class);

        EasyMock.expect(s3Client.getObjectMetadata(EasyMock.isA(String.class),
                                                   EasyMock.isA(String.class)))
            .andReturn(null);
        EasyMock.expect(s3Client.doesBucketExist(EasyMock.isA(String.class)))
            .andReturn(true)
            .times(2);

        ObjectMetadata objectMetadata = new ObjectMetadata();
        String checksum = "checksum";
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
        String count = provider.getSpaceCount("spaceId", 1000);
        assertEquals("1000+", count);

        count = provider.getSpaceCount("spaceId", 1500);
        assertEquals("2000+", count);

        count = provider.getSpaceCount("spaceId", 10000);
        assertEquals("10000+", count);
    }

    private class MockS3StorageProvider extends S3StorageProvider {

        public MockS3StorageProvider() {
            super("accessKey", "secretKey");
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
            new S3StorageProvider(s3Client, "accessKey", options);

        String resultEtag = provider.doesContentExist("bucketname", "contentId");
        assertNotNull(resultEtag);
        assertEquals(etag, resultEtag);
    }

}
