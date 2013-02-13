/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.model.ContentItem;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.common.util.DateUtil;
import org.duracloud.retrieval.RetrievalTestBase;
import org.duracloud.retrieval.source.ContentStream;
import org.duracloud.retrieval.source.RetrievalSource;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * @author: Bill Branan
 * Date: Oct 14, 2010
 */
public class RetrievalWorkerTest extends RetrievalTestBase {

    private final String spaceId = "space-id";
    private final String contentId = "path/to/content-id";
    private String contentValue = "content-value";
    private long testTime = 946684800000L; // Jan 1, 2000 00:00:00 GMT

    @Test
    public void testRetrieveFile() throws Exception {
        RetrievalWorker worker = createRetrievalWorker(true);
        File localFile = worker.getLocalFile();
        assertFalse(localFile.exists());

        StatusManager status = StatusManager.getInstance();
        status.reset();

        // Local file does not exist
        worker.retrieveFile();
        checkFile(localFile, contentValue);
        checkStatus(status, 1, 0, 0);

        // Local file exists, and is the same
        worker.retrieveFile();
        checkFile(localFile, contentValue);
        checkStatus(status, 0, 1, 0);

        // Local file exists, but is different, overwrite on
        String newValue = "new-value";
        FileUtils.writeStringToFile(localFile, newValue);
        worker.retrieveFile();
        checkFile(localFile, contentValue);
        checkStatus(status, 1, 0, 0);
        File copyFile = new File(localFile.getAbsolutePath() + "-copy");
        assertFalse(copyFile.exists());

        // Local file exists, but is different, overwrite off
        worker = createRetrievalWorker(false);
        FileUtils.writeStringToFile(localFile, newValue);
        worker.retrieveFile();
        checkFile(localFile, contentValue);
        checkStatus(status, 1, 0, 0);
        assertTrue(copyFile.exists());
        checkFile(copyFile, newValue);
    }

    private void checkFile(File file, String value) throws IOException {
        assertTrue(file.exists());
        String fileValue = FileUtils.readFileToString(file);
        assertEquals(fileValue, value);
    }

    private void checkStatus(StatusManager status,
                             int success,
                             int noChange,
                             int failure) {
        assertEquals(success, status.getSucceeded());
        assertEquals(noChange, status.getNoChange());
        assertEquals(failure, status.getFailed());
        status.reset();
    }

    @Test
    public void testGetLocalFile() throws Exception {
        RetrievalWorker worker = createRetrievalWorker(true);
        File localFile = worker.getLocalFile();
        assertNotNull(localFile);
        assertTrue(localFile.getAbsolutePath().contains(spaceId));
        for(String pathElem : contentId.split("/")) {
            assertTrue(localFile.getAbsolutePath().contains(pathElem));
        }

        worker = createRetrievalWorkerSingleSpace(true);
        localFile = worker.getLocalFile();
        assertNotNull(localFile);
        assertFalse(localFile.getAbsolutePath().contains(spaceId));
        for(String pathElem : contentId.split("/")) {
            assertTrue(localFile.getAbsolutePath().contains(pathElem));
        }
    }

    @Test
    public void testChecksumsMatch() throws Exception {
        RetrievalWorker worker = createRetrievalWorker(true);
        File localFile = new File(tempDir, "checksum-test");

        FileUtils.writeStringToFile(localFile, contentValue);
        assertTrue(worker.checksumsMatch(localFile));

        FileUtils.writeStringToFile(localFile, "invalid-value");
        assertFalse(worker.checksumsMatch(localFile));
    }

    @Test
    public void testRenameFile() throws Exception {
        RetrievalWorker worker = createRetrievalWorker(true);
        File localFile = new File(tempDir, "rename-test");
        String localFilePath = localFile.getAbsolutePath();
        FileUtils.writeStringToFile(localFile, "test");

        File newFile = worker.renameFile(localFile);
        assertEquals(localFilePath, localFile.getAbsolutePath());
        assertFalse(localFile.exists());
        assertTrue(newFile.exists());

        assertEquals(localFile.getAbsolutePath() + "-copy",
                     newFile.getAbsolutePath());

        FileUtils.writeStringToFile(localFile, "test");
        File newFile2 = worker.renameFile(localFile);
        assertEquals(localFilePath, localFile.getAbsolutePath());
        assertFalse(localFile.exists());
        assertTrue(newFile2.exists());

        assertEquals(localFile.getAbsolutePath() + "-copy-2", 
                     newFile2.getAbsolutePath());
    }

    @Test
    public void testDeleteFile() throws Exception {
        RetrievalWorker worker = createRetrievalWorker(true);
        File localFile = new File(tempDir, "rename-test");
        FileUtils.writeStringToFile(localFile, "test");

        worker.deleteFile(localFile);
        assertFalse(localFile.exists());
    }

    @Test
    public void testRetrieveToFile() throws Exception {
        RetrievalWorker worker = createRetrievalWorker(true);
        File localFile = new File(tempDir, "retrieve-to-file-test");
        assertFalse(localFile.exists());

        worker.retrieveToFile(localFile);
        assertTrue(localFile.exists());

        // Test timestamps
        BasicFileAttributes fileAttributes =
            Files.readAttributes(localFile.toPath(), BasicFileAttributes.class);
        assertEquals(testTime, fileAttributes.creationTime().toMillis());
        assertEquals(testTime, fileAttributes.lastAccessTime().toMillis());
        assertEquals(testTime, fileAttributes.lastModifiedTime().toMillis());

        // Test file value
        String fileValue = FileUtils.readFileToString(localFile);
        assertEquals(fileValue, contentValue);

        // Test failure
        worker = createBrokenRetrievalWorker(true);
        localFile = new File(tempDir, "retrieve-to-file-failure-test");
        assertFalse(localFile.exists());

        try {
            worker.retrieveToFile(localFile);
            fail("Exception expected with non-matching checksum");
        } catch(IOException expected) {
            assertNotNull(expected);
        }
    }

    @Test
    public void testApplyTimestamps() throws Exception {
        String time1 = DateUtil.convertToStringLong(testTime + 100000);
        String time2 = DateUtil.convertToStringLong(testTime + 200000);
        String time3 = DateUtil.convertToStringLong(testTime + 300000);
        ContentStream content =
            new ContentStream(null, null, time1, time2, time3);

        File localFile = new File(tempDir, "timestamp-test");
        FileUtils.writeStringToFile(localFile, contentValue);

        // Check that initial timestamps are current
        BasicFileAttributes fileAttributes =
            Files.readAttributes(localFile.toPath(), BasicFileAttributes.class);
        long now = System.currentTimeMillis();
        assertTrue(isTimeClose(fileAttributes.creationTime().toMillis(), now));
        assertTrue(isTimeClose(fileAttributes.lastAccessTime().toMillis(),now));
        assertTrue(isTimeClose(fileAttributes.lastModifiedTime().toMillis(), now));

        RetrievalWorker worker = createRetrievalWorker(true);
        worker.applyTimestamps(content, localFile);

        // Verify that timestamps were set
        fileAttributes =
            Files.readAttributes(localFile.toPath(), BasicFileAttributes.class);
        long creationTime = fileAttributes.creationTime().toMillis();
        long lastAccessTime = fileAttributes.lastAccessTime().toMillis();
        long lastModifiedTime = fileAttributes.lastModifiedTime().toMillis();

        assertFalse(isTimeClose(creationTime, now));
        assertFalse(isTimeClose(lastAccessTime, now));
        assertFalse(isTimeClose(lastModifiedTime, now));
        assertTrue(testTime + 100000 == creationTime || // windows
                   testTime + 300000 == creationTime);  // linux
        assertEquals(testTime + 200000, lastAccessTime);
        assertEquals(testTime + 300000, lastModifiedTime);
    }

    // Determines if two time values (in millis) are within 10 minutes of each other
    private boolean isTimeClose(long time1, long time2) {
        return Math.abs(time1 - time2) < 600000;
    }

    private RetrievalWorker createRetrievalWorker(boolean overwrite) {
        return new RetrievalWorker(new ContentItem(spaceId, contentId),
                                   new MockRetrievalSource(),
                                   tempDir,
                                   overwrite,
                                   createMockOutputWriter(),
                                   true,
                                   true);
    }

    private RetrievalWorker createRetrievalWorkerSingleSpace(boolean overwrite) {
        return new RetrievalWorker(new ContentItem(spaceId, contentId),
                                   new MockRetrievalSource(),
                                   tempDir,
                                   overwrite,
                                   createMockOutputWriter(),
                                   false,
                                   false);
    }

    private RetrievalWorker createBrokenRetrievalWorker(boolean overwrite) {
        return new RetrievalWorker(new ContentItem(spaceId, contentId),
                                   new BrokenMockRetrievalSource(),
                                   tempDir,
                                   overwrite,
                                   createMockOutputWriter(),
                                   true,
                                   false);
    }

    private class MockRetrievalSource implements RetrievalSource {
        @Override
        public ContentItem getNextContentItem() {
            return null;
        }

        @Override
        public String getSourceChecksum(ContentItem contentItem) {
            ChecksumUtil checksumUtil =
                new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
            InputStream valueStream =
                new ByteArrayInputStream(contentValue.getBytes());
            return checksumUtil.generateChecksum(valueStream);
        }

        @Override
        public ContentStream getSourceContent(ContentItem contentItem) {
            InputStream stream =
                new ByteArrayInputStream(contentValue.getBytes());

            String time = DateUtil.convertToStringLong(testTime);
            return new ContentStream(stream, getSourceChecksum(contentItem),
                                     time, time, time);
        }
    }

    /*
     * Create a retrieval source that will always provide content streams
     * with checksums that do not match
     */
    private class BrokenMockRetrievalSource extends MockRetrievalSource {
        @Override
        public ContentStream getSourceContent(ContentItem contentItem) {
            InputStream stream =
                new ByteArrayInputStream(contentValue.getBytes());
            return new ContentStream(stream, "invalid-checksum",
                                     null, null, null);
        }
    }

}
