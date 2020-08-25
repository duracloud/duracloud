/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.duracloud.client.ContentStore;
import org.duracloud.sync.SyncToolInitializer;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 5/21/14
 */
public class SyncTesterTest {

    @Test
    public void testSyncTester() throws Exception {
        String host = "host";
        String spaceId = "spaceId";
        String username = "username";
        String password = "password";
        String prefix = "prefix";

        final SyncToolInitializer syncTool =
            EasyMock.createMock(SyncToolInitializer.class);
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Capture<String[]> syncConfig = Capture.newInstance(CaptureType.FIRST);
        syncTool.runSyncTool(EasyMock.capture(syncConfig));
        EasyMock.expectLastCall().once();

        contentStore.deleteContent(EasyMock.eq(spaceId),
                                   EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        EasyMock.expect(contentStore.getSpaceContents(spaceId, prefix))
                .andReturn(new ArrayList<String>().iterator())
                .times(2);

        SyncOptimizeConfig config = new SyncOptimizeConfig();
        config.setHost(host);
        config.setSpaceId(spaceId);
        config.setUsername(username);
        config.setPassword(password);

        File dataDir = new File("target/classes");
        File workDir = new File("/a/b/c");

        SyncTester syncTester =
            new SyncTester(config, dataDir, workDir, contentStore, prefix) {
                @Override
                protected SyncToolInitializer getSyncTool() {
                    return syncTool;
                }
            };

        EasyMock.replay(syncTool, contentStore);

        int threads = 7;
        long elapsed = syncTester.runSyncTest(threads);
        assertTrue(elapsed >= 0);

        String[] syncArgs = syncConfig.getValue();
        assertEquals("-h", syncArgs[0]);
        assertEquals(host, syncArgs[1]);
        assertEquals("-s", syncArgs[2]);
        assertEquals(spaceId, syncArgs[3]);
        assertEquals("-u", syncArgs[4]);
        assertEquals(username, syncArgs[5]);
        assertEquals("-p", syncArgs[6]);
        assertEquals(password, syncArgs[7]);
        assertEquals("-c", syncArgs[8]);
        assertEquals(dataDir.getAbsolutePath(), syncArgs[9]);
        assertEquals("-w", syncArgs[10]);
        assertEquals(workDir.getAbsolutePath(), syncArgs[11]);
        assertEquals("-a", syncArgs[12]);
        assertEquals(prefix, syncArgs[13]);
        assertEquals("-x", syncArgs[14]);
        assertEquals("-l", syncArgs[15]);
        assertEquals("-j", syncArgs[16]);
        assertEquals("-t", syncArgs[17]);
        assertEquals(String.valueOf(threads), syncArgs[18]);

        EasyMock.verify(syncTool, contentStore);
    }

    @Test
    public void testCleanupSync() throws Exception {
        String spaceId = "spaceId";
        String prefix = "prefix/";
        String contentId1 = prefix + "content1";
        String contentId2 = prefix + "content2";

        SyncOptimizeConfig config = new SyncOptimizeConfig();
        config.setSpaceId(spaceId);
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        List<String> contentIds = new ArrayList<>();
        contentIds.add(contentId1);
        contentIds.add(contentId2);
        EasyMock.expect(contentStore.getSpaceContents(spaceId, prefix))
                .andReturn(contentIds.iterator());

        contentStore.deleteContent(spaceId, contentId1);
        EasyMock.expectLastCall().once();
        contentStore.deleteContent(spaceId, contentId2);
        EasyMock.expectLastCall().once();

        EasyMock.replay(contentStore);

        SyncTester syncTester =
            new SyncTester(config, null, null, contentStore, prefix);
        syncTester.cleanupSync();

        EasyMock.verify(contentStore);
    }

}
