/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.retrieval.mgmt;

import static junit.framework.Assert.assertTrue;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.duracloud.common.model.ContentItem;
import org.duracloud.retrieval.RetrievalTestBase;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Oct 14, 2010
 */
public class CSVFileOutputWriterTest extends RetrievalTestBase {

    @Test
    public void testWriteSuccess() throws Exception {
        OutputWriter writer = new CSVFileOutputWriter(tempDir);
        File outputFile = getOutputFile();

        String spaceId = "successSpaceId";
        String contentId = "successContentId";
        String localPath = "/success/path";
        writer.writeSuccess(new ContentItem(spaceId, contentId), localPath, 1);

        String outputFileContent = FileUtils.readFileToString(outputFile);
        assertTrue(outputFileContent.contains(CSVFileOutputWriter.SUCCESS));
        assertTrue(outputFileContent.contains(spaceId));
        assertTrue(outputFileContent.contains(contentId));
        assertTrue(outputFileContent.contains(localPath));

        writer.close();
    }

    @Test
    public void testWriteFailure() throws Exception {
        OutputWriter writer = new CSVFileOutputWriter(tempDir);
        File outputFile = getOutputFile();

        String spaceId = "failureSpaceId";
        String contentId = "failureContentId";
        String errorText = "error text";
        writer.writeFailure(new ContentItem(spaceId, contentId), errorText, 1);

        String outputFileContent = FileUtils.readFileToString(outputFile);
        assertTrue(outputFileContent.contains(CSVFileOutputWriter.FAILURE));
        assertTrue(outputFileContent.contains(spaceId));
        assertTrue(outputFileContent.contains(contentId));
        assertTrue(outputFileContent.contains(errorText));

        writer.close();
    }

    private File getOutputFile() throws Exception {
        File outputFile = tempDir.listFiles()[0];
        assertTrue(outputFile.exists());
        assertTrue(outputFile.getName().
            startsWith(CSVFileOutputWriter.OUTPUT_PREFIX));
        return outputFile;
    }
}
