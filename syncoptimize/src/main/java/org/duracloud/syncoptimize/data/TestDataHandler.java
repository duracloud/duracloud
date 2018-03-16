/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.syncoptimize.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the creation and deletion of local test content. This is the content
 * which is transferred to DuraCloud as each sync test is run.
 *
 * @author Bill Branan
 * Date: 5/16/14
 */
public class TestDataHandler {

    private final Logger log = LoggerFactory.getLogger(TestDataHandler.class);

    public void createDirectories(File... dirs) throws IOException {
        for (File dir : dirs) {
            if (dir.exists()) {
                try {
                    FileUtils.cleanDirectory(dir);
                } catch (IOException e) {
                    log.warn("Unable to clean directory {} due to  {}",
                             dir.getAbsolutePath(), e.getMessage());
                }
            } else {
                FileUtils.forceMkdir(dir);
            }
        }
    }

    public void removeDirectories(File... dirs) throws IOException {
        for (File dir : dirs) {
            try {
                FileUtils.deleteDirectory(dir);
            } catch (IOException e) {
                try {
                    FileUtils.cleanDirectory(dir);
                } catch (IOException e2) {
                    log.warn("Unable to clean directory {} due to  {}",
                             dir.getAbsolutePath(), e2.getMessage());
                }
            }
        }
    }

    public void createTestData(File dataDir,
                               int numFiles,
                               int xMB) throws IOException {
        File file = new File(dataDir, "test-file.txt");
        FileWriter writer = new FileWriter(file);

        StringBuilder textBuilder = new StringBuilder();
        for (int x = 0; x < FileUtils.ONE_KB; ++x) {
            textBuilder.append("x");
        }
        String text = textBuilder.toString();
        for (int y = 0; y < FileUtils.ONE_KB * xMB; ++y) {
            writer.write(text);
        }
        IOUtils.closeQuietly(writer);

        for (int z = 1; z < numFiles; ++z) {
            String filename = "test-file-" + z + ".txt";
            FileUtils.copyFile(file, new File(dataDir, filename));
        }
    }

}
