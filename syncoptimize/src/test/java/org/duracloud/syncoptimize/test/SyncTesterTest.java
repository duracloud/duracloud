/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.test;

import org.duracloud.client.ContentStore;
import org.duracloud.sync.SyncToolInitializer;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Bill Branan
 *         Date: 5/21/14
 */
public class SyncTesterTest {

    @Test
    public void testSyncTester() throws Exception {
        String host = "host";
        String spaceId = "spaceId";
        String username = "username";
        String password = "password";

        final SyncToolInitializer syncTool =
            EasyMock.createMock(SyncToolInitializer.class);
        ContentStore contentStore = EasyMock.createMock(ContentStore.class);

        Capture<String[]> syncConfig = new Capture<>();
        syncTool.runSyncTool(EasyMock.capture(syncConfig));
        EasyMock.expectLastCall();

        contentStore.deleteContent(EasyMock.eq(spaceId),
                                   EasyMock.isA(String.class));
        EasyMock.expectLastCall().anyTimes();

        SyncOptimizeConfig config = new SyncOptimizeConfig();
        config.setHost(host);
        config.setSpaceId(spaceId);
        config.setUsername(username);
        config.setPassword(password);

        File dataDir = new File("target/classes");
        File workDir = new File("/a/b/c");

        SyncTester syncTester =
            new SyncTester(config, dataDir, workDir, contentStore) {
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
        assertEquals("-x", syncArgs[12]);
        assertEquals("-l", syncArgs[13]);
        assertEquals("-t", syncArgs[14]);
        assertEquals(String.valueOf(threads), syncArgs[15]);

        EasyMock.verify(syncTool, contentStore);
    }

}
