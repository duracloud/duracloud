/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.streaming;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.common.util.SerializationUtil;
import org.duracloud.error.ContentStoreException;
import org.easymock.classextension.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author: Bill Branan
 * Date: Jun 4, 2010
 */
public class EnableStreamingWorkerTest {

    private ContentStore contentStore;
    private PlaylistCreator playlistCreator;
    private File workDir;

    private static final String playlistXml = "xml";

    @Before
    public void setUp() throws Exception {
        contentStore = createMockContentStore();
        playlistCreator = createMockPlaylistCreator();
        workDir = createWorkDir();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put("domain-name", "domainName");
        taskReturnMap.put("results", "results");
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock
            .expect(contentStore.performTask(EasyMock.eq("enable-streaming"),
                                            EasyMock.isA(String.class)))
            .andReturn(taskReturn)
            .times(1);

        List<String> files = new ArrayList<String>();
        files.add("video1");

        EasyMock
            .expect(contentStore.getSpaceContents(EasyMock.isA(String.class)))
            .andReturn(files.iterator())
            .times(1);

        EasyMock
            .expect(contentStore.addContent(EasyMock.isA(String.class),
                                            EasyMock.isA(String.class),
                                            EasyMock.isA(InputStream.class),
                                            EasyMock.anyLong(),
                                            EasyMock.isA(String.class),
                                            EasyMock.<String>isNull(),
                                            EasyMock.<Map<String, String>>isNull()))
            .andReturn(null)
            .times(3);

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private PlaylistCreator createMockPlaylistCreator()
        throws ContentStoreException {
        PlaylistCreator playlistCreator =
            EasyMock.createMock(PlaylistCreator.class);

        EasyMock
            .expect(playlistCreator.createPlaylist(
                EasyMock.isA(ContentStore.class),
                EasyMock.isA(String.class)))
            .andReturn(playlistXml)
            .times(1);

        EasyMock.replay(playlistCreator);
        return playlistCreator;
    }

    private File createWorkDir() throws IOException {
        File workDir = new File("target", "testWorkDir");
        workDir.mkdir();

        String singlePlayerText = "$STREAM-HOST:$MEDIA-FILE";
        File singlePlayer = new File(workDir, "singleplayer.html");
        FileUtils.writeStringToFile(singlePlayer, singlePlayerText);

        String playlistPlayerText = "$STREAM-HOST";
        File playlistPlayer = new File(workDir, "playlistplayer.html");
        FileUtils.writeStringToFile(playlistPlayer, playlistPlayerText);

        return workDir;
    }


    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
        contentStore = null;

        FileUtils.deleteDirectory(workDir);
    }

    @Test
    public void testEnableStreamingWorker() throws Exception {

        EnableStreamingWorker worker =
            new EnableStreamingWorker(contentStore,
                                      "sourceSpaceId",
                                      "viewerSpaceId",
                                      playlistCreator,
                                      workDir);

        worker.run();

        String streamHost = worker.getStreamHost();
        assertNotNull(streamHost);
        assertEquals(streamHost, "domainName");

        String streamingResult = worker.getEnableStreamingResult();
        assertNotNull(streamingResult);
        assertEquals(streamingResult, "results");

        String error = worker.getError();
        assertNull(error);
    }

}
