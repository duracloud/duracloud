/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
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
public class FileCopierTest {

    private FileCopier copier;

    private File testDir = new File("target", "test-copier");

    private File localFile = new File(testDir, "localFile.txt");
    private File remoteFile = new File(testDir, "remoteFile.txt");
    private boolean toLocal;
    private ContentStore store;

    @Before
    public void setUp() {
        if (!testDir.exists()) {
            Assert.assertTrue(testDir.mkdir());
        }
    }

    @After
    public void tearDown() {
        EasyMock.verify(store);
    }

    @Test
    public void testRunToLocal() throws Exception {
        setUpToLocal();
        copier.run();
        copier.run();
    }

    private void setUpToLocal() throws Exception {
        createContent(remoteFile);
        Path remotePath = new Path(remoteFile.getAbsolutePath());
        toLocal = true;
        store = createMockStoreToLocal();

        copier = new FileCopier(localFile, remotePath, toLocal, store);
    }

    private void createContent(File file) throws IOException {
        OutputStream out = FileUtils.openOutputStream(file);
        IOUtils.write("hello", out);
        IOUtils.closeQuietly(out);
    }

    private ContentStore createMockStoreToLocal() throws Exception {
        ContentStore store = EasyMock.createMock(ContentStore.class);

        String md5 = new ChecksumUtil(MD5).generateChecksum(remoteFile);

        Map<String, String> metadata = new HashMap<String, String>();
        metadata.put(StorageProvider.METADATA_CONTENT_CHECKSUM, md5);
        EasyMock.expect(store.getContentMetadata(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andReturn(metadata);

        EasyMock.expect(store.getContentMetadata(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andReturn(null);
        EasyMock.expect(store.getContentMetadata(EasyMock.isA(String.class),
                                                 EasyMock.isA(String.class)))
            .andReturn(metadata);

        EasyMock.replay(store);
        return store;
    }

    @Test
    public void testRunFromLocal() throws Exception {
        setUpFromLocal();
        copier.run();
    }

    private void setUpFromLocal() throws IOException {
        createContent(localFile);
        Path remotePath = new Path(remoteFile.getAbsolutePath());
        toLocal = false;
        store = createMockStoreFromLocal();

        copier = new FileCopier(localFile, remotePath, toLocal, store);
    }

    private ContentStore createMockStoreFromLocal() {
        ContentStore store = EasyMock.createMock(ContentStore.class);
        // nothing to do.
        EasyMock.replay(store);
        return store;
    }
}
