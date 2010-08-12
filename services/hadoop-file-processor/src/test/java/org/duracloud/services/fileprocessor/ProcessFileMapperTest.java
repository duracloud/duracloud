/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.fileprocessor;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
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

        File resultFile = mapper.processFile(fileToProcess, "fileName");
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

        Text resultValue = collection.get(resultKey);
        assertNotNull(resultValue);
        assertTrue(resultValue.toString().contains(key.toString()));
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
        protected File copyFileLocal(Path remotePath) throws IOException {
            return new File("/local/file");
        }

        @Override
        protected File processFile(File file, String fileName) throws IOException {
            return new File("/processed/file");
        }

        @Override
        protected String moveToOutput(File resultFile,
                                      String fileName,
                                      String outputPath) throws IOException {
            return outputPath + "/" + fileName;
        }
    }

}
