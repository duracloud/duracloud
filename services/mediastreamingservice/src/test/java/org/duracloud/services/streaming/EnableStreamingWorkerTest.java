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
import java.util.HashMap;
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
    private File workDir;

    @Before
    public void setUp() throws Exception {
        contentStore = createMockContentStore();
        workDir = createWorkDir();
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);

        Map<String, String> taskReturnMap = new HashMap<String, String>();
        taskReturnMap.put("domain-name", "domainName");
        taskReturnMap.put("results", "results");
        String taskReturn = SerializationUtil.serializeMap(taskReturnMap);

        EasyMock
            .expect(contentStore.performTask(EasyMock.eq("enable-streaming"),
                                            EasyMock.isA(String.class)))
            .andReturn(taskReturn)
            .times(1);

        EasyMock.replay(contentStore);
        return contentStore;
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

        EnableStreamingWorker worker = new EnableStreamingWorker(contentStore,
                                                                 "sourceSpaceId");

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
