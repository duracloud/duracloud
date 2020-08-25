/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import static junit.framework.Assert.assertNotNull;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.ChecksumUtil;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: 10/24/11
 */
public class DuraStoreSyncEndpointTest {

    private DuraStoreSyncEndpoint endpoint;
    private ContentStore contentStore;
    private String username;
    private String spaceId;
    private File contentFile;

    @Before
    public void setUp() throws Exception {
        username = "userName";
        spaceId = "spaceId";
        contentStore = EasyMock.createMock(ContentStore.class);

        EasyMock.expect(contentStore.getSpaceContents(EasyMock.isA(String.class)))
                .andReturn(new ArrayList<String>().iterator())
                .anyTimes();

        EasyMock.expect(contentStore.getStoreId())
                .andReturn("0")
                .times(1);

        contentFile = File.createTempFile("content", "file.txt");
        contentFile.deleteOnExit();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);

        FileUtils.deleteQuietly(contentFile);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    private void setEndpoint(String prefix, boolean jumpStart) {
        endpoint = new DuraStoreSyncEndpoint(contentStore, username, spaceId,
                                             false, true, false, jumpStart, null,
                                             prefix);
    }

    @Test
    public void testAddUpdateFile() throws Exception {
        String contentId = "contentId";
        String content = "content-file";

        FileUtils.writeStringToFile(contentFile, content);
        ChecksumUtil checksumUtil =
            new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String checksum = checksumUtil.generateChecksum(contentFile);

        Capture<Map<String, String>> propsCapture =
            Capture.newInstance(CaptureType.FIRST);
        EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.eq(contentId),
                                                EasyMock.isA(InputStream.class),
                                                EasyMock.eq(contentFile.length()),
                                                EasyMock.eq("text/plain"),
                                                EasyMock.eq(checksum),
                                                EasyMock.capture(propsCapture)))
                .andReturn("");

        replayMocks();
        setEndpoint(null, false);

        MonitoredFile monitoredFile = new MonitoredFile(contentFile);
        endpoint.addUpdateContent(contentId, monitoredFile);

        Map<String, String> props = propsCapture.getValue();
        assertNotNull(props);
    }

    @Test
    public void testSyncJumpstart() throws Exception {
        EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                EasyMock.isA(String.class),
                                                EasyMock.isA(InputStream.class),
                                                EasyMock.eq(0L),
                                                EasyMock.eq("text/plain"),
                                                EasyMock.isA(String.class),
                                                EasyMock.isA(Map.class)))
                .andReturn("");

        replayMocks();
        setEndpoint(null, true);

        MonitoredFile monitoredFile = new MonitoredFile(contentFile);
        endpoint.syncFile(monitoredFile, contentFile.getParentFile());
    }

}
