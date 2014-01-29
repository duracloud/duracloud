/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chrontask;

import org.duracloud.chronstorage.ChronStageStorageProvider;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Bill Branan
 *         Date: 1/29/14
 */
public class SnapshotTaskRunnerTest {

    private ChronStageStorageProvider chronProvider;
    private SnapshotTaskRunner taskRunner;

    @Before
    public void setup() {
        chronProvider = EasyMock.createMock("ChronStageStorageProvider",
                                            ChronStageStorageProvider.class);
        taskRunner = new SnapshotTaskRunner(chronProvider);
    }

    private void replayMocks() {
        EasyMock.replay(chronProvider);
    }

    @After
    public void tearDown() throws IOException {
        EasyMock.verify(chronProvider);
    }

    @Test
    public void testGetName() {
        replayMocks();
        assertEquals("snapshot", taskRunner.getName());
    }

    @Test
    public void testPerformTask() {
        replayMocks();
        String result = taskRunner.performTask("");
        Assert.assertNotNull(result);
    }

}
