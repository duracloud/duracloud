/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.test;

import static org.junit.Assert.assertEquals;

import org.duracloud.client.ContentStore;
import org.duracloud.syncoptimize.config.SyncOptimizeConfig;
import org.duracloud.syncoptimize.status.SyncTestStatus;
import org.easymock.EasyMock;
import org.junit.Test;

/**
 * @author Bill Branan
 * Date: 5/23/14
 */
public class SyncTestManagerTest {

    @Test
    public void testSyncTestManager() throws Exception {
        SyncOptimizeConfig config = new SyncOptimizeConfig();
        config.setNumFiles(10);
        config.setSizeFiles(5);
        SyncTestStatus status = new SyncTestStatus(false);

        final ContentStore contentStore = EasyMock.createMock(ContentStore.class);
        final SyncTester syncTester = EasyMock.createMock(SyncTester.class);

        SyncTestManager testManager =
            new SyncTestManager(config, null, null, status, null) {
                @Override
                protected SyncTester getSyncTester() {
                    return syncTester;
                }

                @Override
                protected ContentStore getContentStore() {
                    return contentStore;
                }
            };

        // This flow shows the progression of tests as they would
        // be run. The first two tests are the starting seeded min
        // and max numbers. The next thread value is half way
        // between the first two, and following tests are run on
        // thread counts which are half way between the two previous
        // runs, progressing in the direction of the fastest run.
        EasyMock.expect(syncTester.runSyncTest(2)).andReturn(100l);
        EasyMock.expect(syncTester.runSyncTest(30)).andReturn(10l);
        EasyMock.expect(syncTester.runSyncTest(16)).andReturn(50l);
        EasyMock.expect(syncTester.runSyncTest(23)).andReturn(25l);
        EasyMock.expect(syncTester.runSyncTest(26)).andReturn(20l);
        EasyMock.expect(syncTester.runSyncTest(28)).andReturn(15l);

        // Once the test determines that the max value (30) is the
        // fastest, it starts over with that as the min and double
        // its value as the max and runs through the test set again.
        EasyMock.expect(syncTester.runSyncTest(30)).andReturn(10l);
        EasyMock.expect(syncTester.runSyncTest(60)).andReturn(20l);
        EasyMock.expect(syncTester.runSyncTest(45)).andReturn(3l);
        EasyMock.expect(syncTester.runSyncTest(37)).andReturn(8l);
        EasyMock.expect(syncTester.runSyncTest(41)).andReturn(6l);
        EasyMock.expect(syncTester.runSyncTest(43)).andReturn(2l);

        EasyMock.replay(contentStore, syncTester);

        int optimalThreadCount = testManager.runTest();
        assertEquals(43, optimalThreadCount);

        EasyMock.verify(contentStore, syncTester);
    }

}
