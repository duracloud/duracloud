/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.hadoop.base;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.duracloud.services.hadoop.store.FileWithMD5;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Tests the ProcessFileMapper.
 *
 * Note that there are no tests for methods copyFileLocal() and moveToOutput()
 * due to the fact that these methods make calls to hadoop functions which
 * use linux-specific tools to perform file transfer activities.
 *
 * @author: Bill Branan
 * Date: Aug 11, 2010
 */
public class ProcessFileMapperTest {

    private ArrayList<File> testFiles;

    @Before
    public void setUp() throws Exception {
        testFiles = new ArrayList<File>();
    }

    @After
    public void tearDown() throws Exception {
        for(File file : testFiles) {
            FileUtils.deleteQuietly(file);
        }
    }

    @Test
    public void testProcessFile() throws Exception {
        ProcessFileMapper mapper = new ProcessFileMapper();

        String fileContent = "This is test content";
        File fileToProcess = File.createTempFile("test", "file");
        testFiles.add(fileToProcess);
        FileUtils.writeStringToFile(fileToProcess, fileContent);

        FileWithMD5 fileWithMD5 = new FileWithMD5(fileToProcess, null);
        ProcessResult result = mapper.processFile(fileWithMD5, "file.txt");
        assertNotNull(result);
        File resultFile = result.getFile();
        testFiles.add(resultFile);

        assertNotNull(resultFile);
        assertTrue(resultFile.exists());
        assertTrue(resultFile.getName().endsWith(".txt"));

        String resultFileContent = FileUtils.readFileToString(resultFile);
        assertTrue(resultFileContent.contains(fileToProcess.getAbsolutePath()));
    }

    @Test
    public void testMap() throws IOException {
        Text key = new Text("/file/path");
        Text value = new Text("/output/path");

        MockProcessFileMapper mapper = new MockProcessFileMapper();

        SimpleOutputCollector<Text, Text> collector =
            new SimpleOutputCollector<Text, Text>();
        mapper.map(key, value, collector, Reporter.NULL);

        HashMap<Text, Text> collection = collector.getCollection();
        assertNotNull(collection);
        assertEquals(1, collection.size());

        Text resultKey = collection.keySet().iterator().next();
        assertNotNull(resultKey);
        assertTrue(resultKey.toString().contains("success"));
        assertTrue(resultKey.toString().contains(key.toString()));
    }

    private class SimpleOutputCollector<K, V>
        implements OutputCollector<Text, Text> {

        HashMap<Text, Text> collection = new HashMap<Text, Text>();

        @Override
        public void collect(Text key, Text value) throws IOException {
            collection.put(key, value);
        }

        public HashMap<Text, Text> getCollection() {
            return collection;   
        }
    }

    private class MockProcessFileMapper extends ProcessFileMapper {
        @Override
        protected FileWithMD5 copyFileLocal(Path remotePath, Reporter reporter)
            throws IOException {
            return new FileWithMD5(new File("/local/file"), null);
        }

        @Override
        protected ProcessResult processFile(FileWithMD5 file, String origContentId)
            throws IOException {
            return new ProcessResult(new File("/processed/file"), "file");
        }

        @Override
        protected String moveToOutput(File resultFile,
                                      String fileName,
                                      String outputPath,
                                      Reporter reporter) throws IOException {
            return outputPath + "/" + fileName;
        }
    }

    /*
     * Verifies that waitOnThread() exits properly when thread is not active
     * at the start.
     */
    @Test
    public void testWaitOnThread1() {
        ProcessFileMapper mapper = new ProcessFileMapper();
        Reporter reporter = setUpReporterMock(false); // expect no calls

        // thread not started, so not active
        Thread thread = new WaitingThread(0);
        boolean result = mapper.waitOnThread(thread, reporter, 5000);
        assertEquals(true, result);

        EasyMock.verify(reporter);
    }

    /*
     * Verifies that waitOnThread() exits properly when thread terminates
     */
    @Test
    public void testWaitOnThread2() {
        ProcessFileMapper mapper = new ProcessFileMapper();
        Reporter reporter = setUpReporterMock(true);

        Thread thread = new WaitingThread(3000);
        thread.start();
        // Will wait for a long time unless it notices that the
        // thread is complete
        boolean result = mapper.waitOnThread(thread, reporter, 1000000);
        assertEquals(true, result);

        EasyMock.verify(reporter);
    }

    /*
     * Verifies that waitOnThread() exits properly when max timeout is reached
     */
    @Test
    public void testWaitOnThread3() {
        ProcessFileMapper mapper = new ProcessFileMapper();
        Reporter reporter = setUpReporterMock(true);

        Thread thread = new WaitingThread(1000000);
        thread.start();
        // Will wait for a long time unless it stops at the max timeout
        boolean result = mapper.waitOnThread(thread, reporter, 3000);
        assertEquals(false, result);
        thread.interrupt();

        EasyMock.verify(reporter);
    }

    private Reporter setUpReporterMock(boolean expectCalls) {
        Reporter reporter = EasyMock.createMock(Reporter.class);
        if(expectCalls) {
            reporter.progress();
            EasyMock.expectLastCall().atLeastOnce();
        }
        EasyMock.replay(reporter);
        return reporter;
    }

    private class WaitingThread extends Thread {
        private long waitMillis;

        public WaitingThread(long waitMillis) {
            this.waitMillis = waitMillis;
        }

        @Override
        public void run() {
            try {
                this.sleep(waitMillis);
            } catch(InterruptedException e) {}
        }
    }

}
