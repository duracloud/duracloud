/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import static org.duracloud.chunk.manifest.ChunksManifest.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.ChecksumUtil.Algorithm;
import org.duracloud.common.util.OperationTimer;

import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods Date: 9/9/11
 */
public class DuraStoreChunkSyncEndpointTest {
    private DuraStoreChunkSyncEndpoint endpoint;
    private ContentStore contentStore;
    private final String username = "user-name";
    private final String spaceId = "space-id";
    private boolean syncDeletes;
    private final long maxFileSize = 1000;
    private File file;
    private MonitoredFile monitoredFile;

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
        EasyMock.expect(contentStore.getStoreId()).andReturn("0");
        syncDeletes = false;
        file = EasyMock.createMock("File", File.class);
        monitoredFile =
            EasyMock.createMock("MonitoredFile", MonitoredFile.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore, file, monitoredFile);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore, file, monitoredFile);
    }

    @Test
    public void testGetFilesList() throws Exception {
        List<String> contents = new ArrayList<String>();
        contents.add("item-0");
        contents.add("item-1");
        contents.add("item-2");
        contents.add("chunk" + chunkSuffix + 0);
        contents.add("chunk" + chunkSuffix + 1);
        contents.add("manifest" + manifestSuffix);

        createGetFilesListMocks(contents);
        replayMocks();
        setEndpoint();

        Iterator<String> filesList = endpoint.getFilesList();
        Assert.assertNotNull(filesList);

        Assert.assertEquals(contents.get(0), filesList.next());
        Assert.assertEquals(contents.get(1), filesList.next());
        Assert.assertEquals(contents.get(2), filesList.next());
        Assert.assertEquals(contents.get(5), filesList.next() + manifestSuffix);
        Assert.assertNull(filesList.next());
    }

    private void createGetFilesListMocks(List<String> contents)
        throws ContentStoreException {

        EasyMock.expect(contentStore.getSpaceContents(spaceId))
                .andReturn(contents.iterator())
                .times(2);

    }

    private void setEndpoint(long maxChunkSize) {
        endpoint =
            new DuraStoreChunkSyncEndpoint(contentStore,
                                           username,
                                           spaceId,
                                           syncDeletes,
                                           maxChunkSize,
                                           true,
                                           false,
                                           false,
                                           null,
                                           null);

    }

    private void setEndpoint() {
        setEndpoint(maxFileSize);
    }

    @Test
    public void testAddUpdateFile() throws Exception {
        String contentId = "contentId";
        String content = "content-file";
        File contentFile = File.createTempFile("content", "file.txt");
        FileUtils.writeStringToFile(contentFile, content);
        ChecksumUtil checksumUtil =
            new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String checksum = checksumUtil.generateChecksum(contentFile);

        EasyMock.expect(contentStore.getSpaceContents(spaceId))
                .andReturn(new ArrayList<String>().iterator());
        EasyMock.expect(contentStore.getSpaceACLs(spaceId))
                .andReturn(new HashMap<String, AclType>())
                .anyTimes();

        Capture<Map<String, String>> propsCapture =
            new Capture<Map<String, String>>();
        EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.eq(contentId),
                                                EasyMock.isA(InputStream.class),
                                                EasyMock.eq(contentFile.length()),
                                                EasyMock.eq("application/octet-stream"),
                                                EasyMock.eq(checksum),
                                                EasyMock.capture(propsCapture)))
                .andReturn("");

        replayMocks();
        setEndpoint();

        MonitoredFile monitoredFile = new MonitoredFile(contentFile);
        endpoint.addUpdateContent(contentId, monitoredFile);

        Map<String, String> props = propsCapture.getValue();
        assertNotNull(props);
        String creator = props.get(StorageProvider.PROPERTIES_CONTENT_CREATOR);
        assertEquals(username, creator);

        FileUtils.deleteQuietly(contentFile);
    }

    @Test
    public void testAddUpdate3MBFileWith1MBChunks() throws Exception {
        testAddChunkedFile(3, 10 * 1000 * 1000);
    }

    // /**
    // * This test takes about 50 minutes due to the large file size.
    // * In order to prevent a major slowdown in the build I have
    // * commented it out.
    // *
    // * @throws Exception
    // */
    // @Test
    // public void testAddUpdate150GBFile() throws Exception {
    // testAddChunkedFile(150,1000*1000*1000);
    // }

    protected void testAddChunkedFile(int chunkCount, long chunkSize)
        throws Exception {
        String contentId = "contentId";
        File tmpFile = File.createTempFile("test", "txt");
        tmpFile.deleteOnExit();

        EasyMock.expect(contentStore.getSpaceContents(spaceId))
                .andReturn(new ArrayList<String>().iterator());
        EasyMock.expect(contentStore.getSpaceACLs(spaceId))
                .andReturn(new HashMap<String, AclType>())
                .anyTimes();
        long fileSize = chunkCount * chunkSize;
        int fileCount = chunkCount + 1;

        final Capture<InputStream> isCapture = new Capture<>();

        EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.isA(String.class),
                                                EasyMock.capture(isCapture),
                                                EasyMock.anyLong(),
                                                EasyMock.isA(String.class),
                                                EasyMock.isA(String.class),
                                                EasyMock.isA(Map.class)))
                .andAnswer(new IAnswer<String>() {
                    @Override
                    public String answer() throws Throwable {
                        InputStream is = isCapture.getValue();
                        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
                        return (new OperationTimer<String>("Generate the checksum") {
                            public String executeImpl() {
                                String checksum = util.generateChecksum(is);
                                return checksum;
                            }
                        }).execute();
                    }
                })
                .times(fileCount);
        
        EasyMock.expect(contentStore.contentExists(EasyMock.eq(spaceId),
                                                   EasyMock.isA(String.class)))
                .andReturn(false)
                .times(chunkCount);

        ChecksumUtil util = new ChecksumUtil(Algorithm.MD5);
        InputStream checksumIs = getInputStream(fileSize);
        String wholeFileChecksum = null;
        try{ 
            wholeFileChecksum = util.generateChecksum(checksumIs);
        }finally {
            IOUtils.closeQuietly(checksumIs);
        }
        
        PipedInputStream is = getInputStream(fileSize);
        MonitoredInputStream mfis = new MonitoredInputStream(is);

        try {
            EasyMock.expect(monitoredFile.getAbsolutePath())
                    .andReturn(tmpFile.getAbsolutePath());
            EasyMock.expect(monitoredFile.getChecksum()).andReturn(wholeFileChecksum);
            EasyMock.expect(monitoredFile.length()).andReturn(fileSize);

            EasyMock.expect(monitoredFile.getStream()).andReturn(mfis);
            replayMocks();
            setEndpoint(chunkSize);

            endpoint.addUpdateContent(contentId, monitoredFile);

        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(mfis);

        }

    }

    protected PipedInputStream getInputStream(long fileSize)
        throws IOException {
        int bufferSize = 1000000;
        PipedOutputStream os = new PipedOutputStream();
        PipedInputStream is = new PipedInputStream(bufferSize);
        os.connect(is);
        final Thread t = new Thread(() -> {
            byte[] buf = new byte[bufferSize];
            try {
                long iterations = fileSize / buf.length;

                for (long i = 0; i < iterations; i++) {
                    os.write(buf);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(os);
            }
        });

        t.start();
        return is;
    }
}
