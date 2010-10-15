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
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

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
        ContentStore store = createMockStore();
        String spaceId = "space-id";
        String contentId = "content-id";

        String md5 = util.getMd5(store, spaceId, contentId);
        Assert.assertNotNull(md5);
        Assert.assertEquals("item-not-found", md5);

        md5 = util.getMd5(store, spaceId, contentId);
        Assert.assertNotNull(md5);
        Assert.assertEquals(expectedMd5, md5);

        md5 = util.getMd5(store, spaceId, contentId);
        Assert.assertNotNull(md5);
        Assert.assertEquals("md5-not-found", md5);

        EasyMock.verify(store);
    }

    private ContentStore createMockStore() throws Exception {
        ContentStore store = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(store.getContentMetadata(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andThrow(new ContentStoreException("test"));

        String md5 = new ChecksumUtil(MD5).generateChecksum(file);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(StorageProvider.METADATA_CONTENT_MD5, md5);
        EasyMock.expect(store.getContentMetadata(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andReturn(metadata);

        EasyMock.expect(store.getContentMetadata(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andReturn(new HashMap<String, String>());

        EasyMock.replay(store);
        return store;
    }
    
}
