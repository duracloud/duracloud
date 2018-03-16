/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.mgmt;

import static junit.framework.Assert.assertEquals;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.io.File;

import org.duracloud.sync.endpoint.MonitoredFile;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.endpoint.SyncResultType;
import org.easymock.EasyMockRunner;
import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Daniel Bernstein
 * @since July 29, 2017
 */
@RunWith(EasyMockRunner.class)
public class SyncWorkerTest extends EasyMockSupport {

    @Before
    public void setup() {

    }

    @After
    public void teardown() {
        verifyAll();
    }

    @Test
    public void testDuracloud1131() {
        //test the fact that retries are not occurring as expected on error.
        assertEquals(0, ChangedList.getInstance().getListSizeIncludingReservedFiles());
        ChangedList.getInstance().addChangedFile(new File("path"));
        assertEquals(1, ChangedList.getInstance().getListSizeIncludingReservedFiles());
        ChangedFile changedFile = ChangedList.getInstance().reserve();
        assertEquals(0, ChangedList.getInstance().getListSize());
        assertEquals(1, ChangedList.getInstance().getListSizeIncludingReservedFiles());
        File watchDir = createMock(File.class);
        SyncEndpoint endpoint = createMock(SyncEndpoint.class);
        expect(endpoint.syncFileAndReturnDetailedResult(isA(MonitoredFile.class), isA(File.class)))
            .andReturn(SyncResultType.FAILED);
        replayAll();
        SyncWorker worker = new SyncWorker(changedFile, watchDir, endpoint);
        worker.run();
        assertEquals(1, ChangedList.getInstance().getListSizeIncludingReservedFiles());

    }
}
