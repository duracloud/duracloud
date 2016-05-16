/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chunk;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.duracloud.chunk.error.NotFoundException;
import org.duracloud.chunk.writer.ContentWriter;
import org.duracloud.chunk.writer.FilesystemContentWriter;
import org.duracloud.common.util.ChecksumUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * @author Andrew Woods
 *         Date: Feb 4, 2010
 */
public class FileChunkerTest {

    private ContentWriter writer;

    private File testDir = new File("target/test-filechunker");
    private File srcDir = new File(testDir, "src");
    private File destDir = new File(testDir, "dest");

    @Before
    public void setUp() {
        if (!testDir.exists()) {
            Assert.assertTrue(testDir.mkdirs());
        }

        if (!destDir.exists()) {
            Assert.assertTrue(destDir.mkdirs());
        }

        if (!srcDir.exists()) {
            Assert.assertTrue(srcDir.mkdirs());
        }

        writer = new FilesystemContentWriter();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(srcDir);
        FileUtils.deleteDirectory(destDir);
    }

    @Test
    public void testCreateContent() throws IOException {
        long chunkSize = 10000;
        long size = chunkSize * 4 + chunkSize / 2;

        String name = "create-test.txt";
        createAndVerifyContent(name, size);
    }

    private File createAndVerifyContent(String contentName, long contentSize)
        throws IOException {
        File outFile = new File(srcDir, contentName);
        InputStream is = FileChunker.createTestContent(outFile, contentSize);
        IOUtils.closeQuietly(is);

        Assert.assertTrue(outFile.exists());
        Assert.assertEquals(contentSize, outFile.length());

        return outFile;
    }

    @Test
    public void testLoadContent() throws IOException, NotFoundException {
        long chunkSize = 16000;
        long contentSize = chunkSize * 4 + chunkSize / 2;

        String suffix = ".dura-chunk-\\d+";

        // test 0
        int runNumber = 0;
        doTestLoadContent(runNumber, suffix, contentSize, chunkSize);

        // test 1
        runNumber++;
        doTestLoadContent(runNumber, suffix, contentSize, chunkSize / 2);

        // test 2
        runNumber++;
        doTestLoadContent(runNumber, suffix, contentSize, chunkSize / 16);

        // test 3 : single chunk
        suffix = "";
        runNumber++;
        doTestLoadContent(runNumber, suffix, contentSize, contentSize * 2);
    }

    private void doTestLoadContent(int runNumber,
                                   String suffixPattern,
                                   long contentSize,
                                   long chunkSize)
        throws IOException, NotFoundException {
        String prefix = "load-test";
        String ext = ".txt";
        String suffix = ext + suffixPattern;
        String name = prefix + runNumber + ext;

        IOFileFilter dirFilter = FileFilterUtils.nameFileFilter(name);
        FileChunkerOptions options = new FileChunkerOptions(dirFilter,
                                                            chunkSize);

        FileChunker chunker = new FileChunker(writer, options);
        File content = createAndVerifyContent(name, contentSize);
        chunker.addContentFrom(srcDir, destDir.getPath());

        Pattern p = Pattern.compile(".*" + prefix + runNumber + suffix);
        IOFileFilter filter = new RegexFileFilter(p);
        IOFileFilter all = FileFilterUtils.trueFileFilter();
        Collection<File> files = FileUtils.listFiles(destDir, filter, all);

        Assert.assertNotNull(files);

        int partial = 1;
        if (contentSize % chunkSize == 0) {
            partial = 0;
        }
        Assert.assertEquals(contentSize / chunkSize + partial, files.size());

        long totalChunksSize = 0;
        for (File file : files) {
            totalChunksSize += file.length();
        }
        Assert.assertEquals(content.length(), totalChunksSize);

    }

    @Test
    public void testAddContent() throws Exception {
        long chunkSize = 1000;
        long fileSize = chunkSize + chunkSize/2;
        String fileName = "add-content-test.txt";

        FileChunker chunker =
            new FileChunker(writer, new FileChunkerOptions(chunkSize));
        File file = createAndVerifyContent(fileName, fileSize);

        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.MD5);
        String fileChecksum = util.generateChecksum(file);
        chunker.addContent(destDir.getPath(), fileName, fileChecksum, file);

        File chunk1 = new File(destDir, fileName + ".dura-chunk-0000");
        File chunk2 = new File(destDir, fileName + ".dura-chunk-0001");
        File manifest = new File(destDir, fileName + ".dura-manifest");
        Assert.assertTrue(chunk1.exists());
        Assert.assertTrue(chunk2.exists());
        Assert.assertTrue(manifest.exists());
    }

    @Test
    public void testIgnoreFlag() throws Exception {
        long chunkSize = 1000;
        String bigFilename0 = "big0.txt";
        String bigFilename1 = "big1.txt";
        String smallFilename = "small.txt";

        File bigFile0 = new File(srcDir, bigFilename0);
        File bigFile1 = new File(srcDir, bigFilename1);
        File smallFile = new File(srcDir, smallFilename);
        FileChunker.createTestContent(bigFile0, chunkSize + 1).close();
        FileChunker.createTestContent(bigFile1, chunkSize + 1).close();
        FileChunker.createTestContent(smallFile, chunkSize - 1).close();

        boolean ignoreLargeFiles = true;
        FileChunkerOptions options;
        options = new FileChunkerOptions(chunkSize, ignoreLargeFiles);
        FileChunker chunker = new FileChunker(writer, options);
        chunker.addContentFrom(srcDir, destDir.getPath());

        Assert.assertTrue(!new File(destDir, bigFilename0).exists());
        Assert.assertTrue(!new File(destDir, bigFilename1).exists());
        Assert.assertTrue(new File(destDir, smallFilename).exists());

    }

    @Test
    public void testInputFilters() throws Exception {
        createContentTree();

        int id = 0;
        IOFileFilter fileFilter = FileFilterUtils.trueFileFilter();
        IOFileFilter dirFilter = FileFilterUtils.trueFileFilter();
        doTestInputFilters(fileFilter, dirFilter, id, 13);

        id = 1;
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.nameFileFilter("dir-0-b");
        doTestInputFilters(fileFilter, dirFilter, id, 2);

        id = 2;
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.orFileFilter(FileFilterUtils.nameFileFilter(
            "dir-0-b"), FileFilterUtils.nameFileFilter("dir-0-c"));
        doTestInputFilters(fileFilter, dirFilter, id, 3);

        id = 3;
        fileFilter = FileFilterUtils.trueFileFilter();
        dirFilter = FileFilterUtils.falseFileFilter();
        doTestInputFilters(fileFilter, dirFilter, id, 1);

        id = 4;
        fileFilter = FileFilterUtils.falseFileFilter();
        dirFilter = FileFilterUtils.trueFileFilter();
        try {
            doTestInputFilters(fileFilter, dirFilter, id, 0);
            Assert.fail("exception expected since no files found.");
        } catch (Exception e) {
        }

    }

    private void doTestInputFilters(IOFileFilter fileFilter,
                                    IOFileFilter dirFilter,
                                    int id,
                                    int numFiles) throws NotFoundException {
        long chunkSize = 1000;
        FileChunkerOptions options;
        options = new FileChunkerOptions(fileFilter, dirFilter, chunkSize);

        File testDestDir = new File(destDir, "test" + id);
        String dest = testDestDir.getPath();

        FileChunker chunker = new FileChunker(writer, options);
        chunker.addContentFrom(srcDir, dest);
        verifyFilters(testDestDir, numFiles);
    }

    private void verifyFilters(File dir, int numFiles) {
        boolean recurse = true;
        Collection<File> files = FileUtils.listFiles(dir, null, recurse);
        Assert.assertEquals(numFiles, files.size());
    }

    private void createContentTree() throws Exception {
        String ext = ".txt";
        write(new File(srcDir, "file" + ext));

        char[] dirSuffixes = new char[]{'a', 'b', 'c'};
        for (char suffix0 : dirSuffixes) {
            File dir0 = new File(srcDir, "dir-0-" + suffix0);
            dir0.mkdir();
            write(new File(dir0, "file-0" + suffix0 + ext));
            for (char suffix1 : dirSuffixes) {
                File dir1 = new File(dir0, "dir-1-" + suffix1);
                dir1.mkdir();
                write(new File(dir1, "file-0" + suffix0 + "1" + suffix1 + ext));
            }
        }
    }

    private void write(File file) throws Exception {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write("hello".getBytes());
        fos.close();
    }

}
