/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.store;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.s3.S3Credentials;
import org.duracloud.common.util.ChecksumUtil;
import org.easymock.classextension.EasyMock;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static org.duracloud.common.util.ChecksumUtil.Algorithm.MD5;

/**
 * @author Andrew Woods
 *         Date: Oct 14, 2010
 */
public class MD5UtilTest {

    private MD5Util util;

    private File testDir = new File("target", "test-md5");
    private File file = new File(testDir, "file.txt");

    private String expectedMd5;

    @Before
    public void setUp() throws Exception {
        util = new MD5Util();

        if (!testDir.exists()) {
            Assert.assertTrue(testDir.mkdir());
        }
        createContent(file);

        expectedMd5 = new ChecksumUtil(MD5).generateChecksum(file);
        Assert.assertNotNull(expectedMd5);
    }

    private void createContent(File file) throws IOException {
        OutputStream out = FileUtils.openOutputStream(file);
        IOUtils.write("hello", out);
        IOUtils.closeQuietly(out);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetMd5Local() throws Exception {
        String md5 = util.getMd5(file);
        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);

        md5 = util.getMd5(new File("junk"));
        Assert.assertNotNull(md5);
        Assert.assertEquals("file-not-found", md5);
    }

    @Test
    public void testGetMd5Remote() throws Exception {
        S3Service s3Service = createMockS3Service();
        util = new TestMD5Util(s3Service);

        S3Credentials s3Credentials = new S3Credentials();
        String spaceId = "space-id";
        String contentId = "content-id";

        String md5 = util.getMd5(s3Credentials, spaceId, contentId);
        Assert.assertNotNull(md5);
        Assert.assertEquals("item-md5-not-found", md5);

        md5 = util.getMd5(s3Credentials, spaceId, contentId);
        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);        

        EasyMock.verify(s3Service);
    }

    private class TestMD5Util extends MD5Util {
        S3Service s3Service;

        public TestMD5Util(S3Service s3Service) {
            this.s3Service = s3Service;
        }

        @Override
        protected S3Service getS3Service(S3Credentials s3Credentials)
            throws S3ServiceException {
            return s3Service;
        }
    }

    private S3Service createMockS3Service() {
        S3Service s3Service = EasyMock.createMock(S3Service.class);

        S3Object s3Object = new S3Object();
        s3Object.setETag(expectedMd5);
        try {
            EasyMock.expect(
                s3Service.getObjectDetails(EasyMock.isA(S3Bucket.class),
                                           EasyMock.isA(String.class)))
                .andThrow(new S3ServiceException(""));

            EasyMock.expect(
                s3Service.getObjectDetails(EasyMock.isA(S3Bucket.class),
                                           EasyMock.isA(String.class)))
                .andReturn(s3Object);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }

        EasyMock.replay(s3Service);
        return s3Service;
    }
    
}
