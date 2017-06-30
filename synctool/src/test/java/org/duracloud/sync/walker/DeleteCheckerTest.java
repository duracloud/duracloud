/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.walker;

import org.apache.commons.io.FileUtils;
import org.duracloud.sync.SyncTestBase;
import org.duracloud.sync.endpoint.SyncEndpoint;
import org.duracloud.sync.mgmt.ChangedFile;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author: Bill Branan
 * Date: Mar 29, 2010
 */
public class DeleteCheckerTest extends SyncTestBase {

    private File tempDir;
    private SyncEndpoint syncEndpoint;
    private String spaceId = "space-id";

    @Before
    public void setUp() throws Exception {
        super.setUp();
        tempDir = createTempDir("delete-check");
        syncEndpoint = EasyMock.createMock(SyncEndpoint.class);
    }

    private void replayMocks() {
        EasyMock.replay(syncEndpoint);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        FileUtils.deleteDirectory(tempDir);
        EasyMock.verify(syncEndpoint);
    }

    /*
     * Verifies that:
     * 1. An existing file in the content dir is not added to the changed list
     * 2. A file which is not in the content dir is added to the changed list
     */
    @Test
    public void testDeleteChecker() throws Exception {
        File tempFile = File.createTempFile("temp", "file", tempDir);
        String delFile = "deletedFile";

        List<String> filesList = new ArrayList<>();
        filesList.add(tempFile.getName());
        filesList.add(delFile);

        List<File> syncDirs = new ArrayList<>();
        syncDirs.add(tempDir);

        EasyMock.expect(syncEndpoint.getFilesList())
                .andReturn(filesList.iterator());
        syncEndpoint.deleteContent(spaceId, delFile);
        EasyMock.expectLastCall().once();

        replayMocks();

        DeleteChecker deleteChecker =
            new DeleteChecker(syncEndpoint, spaceId, syncDirs, null);
        deleteChecker.run();
    }

    /*
     * Verifies that:
     * 1. An existing file in the content dir, that has a known prefix in the
     *    content ID, is not added to the changed list
     * 2. A file which is not in the content dir is added to the changed list
     */
    @Test
    public void testDeleteCheckerPrefix() throws Exception {
        File tempFile = File.createTempFile("temp", "file", tempDir);
        String delFile = "deletedFile";

        String prefix = "prefix/";

        List<String> filesList = new ArrayList<>();
        filesList.add(prefix + tempFile.getName());
        filesList.add(delFile);

        List<File> syncDirs = new ArrayList<>();
        syncDirs.add(tempDir);

        EasyMock.expect(syncEndpoint.getFilesList())
                .andReturn(filesList.iterator());
        syncEndpoint.deleteContent(spaceId, delFile);
        EasyMock.expectLastCall().once();

        replayMocks();

        DeleteChecker deleteChecker =
            new DeleteChecker(syncEndpoint, spaceId, syncDirs, prefix);
        deleteChecker.run();
    }

    /*
     * Verifies that:
     * 1. An existing file in the content dir, that has no prefix in the
     *    content ID, even though a prefix is expected, is added to the
     *    changed list
     */
    @Test
    public void testDeleteCheckerNewPrefix() throws Exception {
        File tempFile = File.createTempFile("temp", "file", tempDir);

        String prefix = "prefix/";

        List<String> filesList = new ArrayList<>();
        filesList.add(tempFile.getName());

        List<File> syncDirs = new ArrayList<>();
        syncDirs.add(tempDir);

        EasyMock.expect(syncEndpoint.getFilesList())
                .andReturn(filesList.iterator());
        syncEndpoint.deleteContent(spaceId, tempFile.getName());
        EasyMock.expectLastCall().once();

        replayMocks();

        DeleteChecker deleteChecker =
            new DeleteChecker(syncEndpoint, spaceId, syncDirs, prefix);
        deleteChecker.run();
    }

}
