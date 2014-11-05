/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.model.AclType;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.duracloud.chunk.manifest.ChunksManifest.chunkSuffix;
import static org.duracloud.chunk.manifest.ChunksManifest.manifestSuffix;

/**
 * @author Andrew Woods
 *         Date: 9/9/11
 */
public class DuraStoreChunkSyncEndpointTest {

    private DuraStoreChunkSyncEndpoint endpoint;

    private ContentStore contentStore;
    private final String username = "user-name";
    private final String spaceId = "space-id";
    private boolean syncDeletes;
    private final long maxFileSize = 1024;

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);
        EasyMock.expect(contentStore.getStoreId()).andReturn("0");
        syncDeletes = false;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
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

        EasyMock.expect(contentStore.getSpaceContents(spaceId)).andReturn(
            contents.iterator()).times(2);

    }

    private void setEndpoint() {
        endpoint = new DuraStoreChunkSyncEndpoint(contentStore, username,
                                                  spaceId, syncDeletes,
                                                  maxFileSize, true, false,
                                                  false, null, null);
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
                .andReturn(new HashMap<String, AclType>()).anyTimes();

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

}
