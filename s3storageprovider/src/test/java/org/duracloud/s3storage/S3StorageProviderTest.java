/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.s3storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.StorageClass;
import org.easymock.Capture;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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
        doTestStorageClass(StorageClass.Standard.toString());
    }

    @Test
    public void testStorageClassRRS() {
        doTestStorageClass(StorageClass.ReducedRedundancy.toString());
    }

    @Test
    public void testStorageClassNull() {
        doTestStorageClass(null);
    }

    private void doTestStorageClass(String storageClass) {
        Capture<PutObjectRequest> capturedRequest = createS3ClientAddContent();
        S3StorageProvider provider = new S3StorageProvider(s3Client,
                                                           "accessKey",
                                                           storageClass);

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

        if (null == storageClass) {
            Assert.assertEquals(StorageClass.Standard.toString(),
                                requestStorageClass);
        } else {
            Assert.assertEquals(storageClass, requestStorageClass);
        }
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

}
