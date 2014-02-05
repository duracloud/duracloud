/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import org.apache.commons.io.FileUtils;
import org.duracloud.client.ContentStore;
import org.duracloud.retrieval.RetrievalTestBase;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author: Erik Paulsson
 * Date: June 27, 2013
 */
public class SpaceListWorkerTest extends RetrievalTestBase {

    private final String contentId = "path/to/content-id";
    private List<String> contents1;
    private List<String> contents2;
    private ContentStore contentStore;

    @Override
    @After
    public void tearDown() throws Exception {
        // So we don't delete the tempDir after every test
    }

    @AfterClass
    public static void finalTearDown() throws Exception {
        // Delete the tempDir only after all tests
        FileUtils.deleteDirectory(new File("target/" +
                                  SpaceListWorkerTest.class.getName()));
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        contents1 = new ArrayList<String>();
        contents1.add("item1");
        contents1.add("item2");
        contents1.add("item3");
        contents1.add("item4");
        contents1.add("item5");
        
        contents2 = new ArrayList<String>();
        contents2.add("item6");
        contents2.add("item7");
        contents2.add("item8");
        contents2.add("item9");
        contents2.add("item10");
        contents2.add("item11");

        contentStore = EasyMock.createMock(ContentStore.class);
        EasyMock.expect(contentStore.getStorageProviderType()).andReturn("mock-provider").times(3);
        EasyMock.expect(contentStore.getSpaceContents("space1")).andReturn(contents1.iterator()).times(2);
        EasyMock.expect(contentStore.getSpaceContents("space2")).andReturn(contents2.iterator());
        EasyMock.replay(contentStore);
    }

    @Test
    public void testSpaceList1() throws Exception {
        SpaceListWorker worker = new SpaceListWorker(contentStore,
                                                     "space1",
                                                     tempDir,
                                                     true);
        worker.run();
        File outputFile = worker.getOutputFile();
        assertEquals(outputFile.getName(),
                     "space1-content-listing-mock-provider.txt");
        compareFileContents(outputFile, contents1);
    }

    @Test
    public void testSpaceList2() throws Exception {
        SpaceListWorker worker = new SpaceListWorker(contentStore,
                                     "space2",
                                     tempDir,
                                     true);
        worker.run();
        File outputFile = worker.getOutputFile();
        assertEquals(outputFile.getName(),
                     "space2-content-listing-mock-provider.txt");
        compareFileContents(outputFile, contents2);
    }

    // run space1 again to make sure the previous file was overwritten
    @Test
    public void testSpaceList1Again() throws Exception { 
        SpaceListWorker worker = new SpaceListWorker(contentStore,
                                     "space1",
                                     tempDir,
                                     true);
        worker.run();
        File outputFile = worker.getOutputFile();
        assertEquals(outputFile.getName(),
                     "space1-content-listing-mock-provider.txt");
        compareFileContents(outputFile, contents1);
    }

    @Test
    public void testSpaceList1NoOverwrite() throws Exception {
        SpaceListWorker worker = new SpaceListWorker(contentStore,
                                     "space1",
                                     tempDir,
                                     false);
        worker.run();
        File outputFile = worker.getOutputFile();
        String fileName = outputFile.getName();
        assertThat("space1-content-listing-mock-provider.txt", is(not(fileName)));
        assertTrue(fileName.startsWith("space1-content-listing-mock-provider"));
        compareFileContents(outputFile, contents1);
    }

    private void compareFileContents(File file, List<String> expectedContents)
        throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line = null;
        for(String expectedLine: expectedContents) {
            line = br.readLine();
            assertNotNull(expectedLine);
            assertNotNull(line);
            assertEquals(expectedLine, line);
        }
        line = br.readLine();
        assertNull(line);  // no more lines in file
        br.close();
    }

}