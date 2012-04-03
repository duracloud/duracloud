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
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.storage.provider.StorageProvider;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * @author: Bill Branan
 * Date: 10/24/11
 */
public class DuraStoreSyncEndpointTest {

    private DuraStoreSyncEndpoint endpoint;
    private ContentStore contentStore;
    private String username;
    private String spaceId;

    @Before
    public void setUp() throws Exception {
        username = "userName";
        spaceId = "spaceId";
        contentStore = EasyMock.createMock(ContentStore.class);

        EasyMock
            .expect(contentStore.getSpaceContents(EasyMock.isA(String.class)))
            .andReturn(new ArrayList<String>().iterator())
            .anyTimes();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testGetContentId() throws Exception {
        replayMocks();
        setEndpoint();

        File watchDir = new File("a");
        MonitoredFile file = new MonitoredFile(new File("a/b/c", "file.txt"));

        // Get Content ID with a watch dir
        String contentId = endpoint.getContentId(file, watchDir);
        assertEquals("b/c/file.txt", contentId);

        // Get Content ID with now watch dir
        contentId = endpoint.getContentId(file, null);
        assertEquals("file.txt", contentId);
    }

    private void setEndpoint() {
        endpoint =
            new DuraStoreSyncEndpoint(contentStore, username, spaceId, false);
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

        Capture<Map<String, String>> propsCapture =
            new Capture<Map<String, String>>();
        EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.eq(contentId),
                                                EasyMock.isA(InputStream.class),
                                                EasyMock.eq(contentFile.length()),
                                                EasyMock.eq("text/plain"),
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
