/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * @author: Bill Branan
 * Date: 10/21/11
 */
public class MonitoredInputStreamTest {

    private File file;
    private MonitoredInputStream stream;

    @Before
    public void setUp() throws Exception {
        file = File.createTempFile("temp", "file");
        FileUtils.writeStringToFile(file, "This file is used to execute tests");

        stream = new MonitoredInputStream(file);
        assertEquals(0, stream.bytesRead);
    }

    @After
    public void tearDown() throws Exception {
        stream.close();
        FileUtils.deleteQuietly(file);
    }

    @Test
    public void testRead() throws Exception {
        stream.read();
        assertEquals(1, stream.bytesRead);

        for(int value = 0; value >= 0; ) { // Read the rest of the bytes
            value = stream.read();
        }

        assertEquals(file.length(), stream.bytesRead);
    }

    @Test
    public void testReadToArray() throws Exception {
        stream.read(new byte[5]);
        assertEquals(5, stream.bytesRead);

        for(int bytesRead = 0; bytesRead >= 0; ) { // Read the rest of the bytes
            bytesRead = stream.read(new byte[5]);
        }

        assertEquals(file.length(), stream.bytesRead);
    }

    @Test
    public void testReadToArrayLen() throws Exception {
        stream.read(new byte[8], 0, 8);
        assertEquals(8, stream.bytesRead);

        for(int bytesRead = 0; bytesRead >= 0; ) { // Read the rest of the bytes
            bytesRead = stream.read(new byte[8]);
        }

        assertEquals(file.length(), stream.bytesRead);
    }

}
