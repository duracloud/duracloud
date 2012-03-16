/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.amazonmapreduce.postprocessing;

import org.duracloud.client.ContentStore;
import org.duracloud.domain.Content;
import org.duracloud.error.ContentStoreException;
import org.duracloud.services.amazonmapreduce.AmazonMapReduceJobWorker;
import org.easymock.IArgumentMatcher;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Oct 1, 2010
 */
public class HeaderPostJobWorkerTest {

    private HeaderPostJobWorker worker;

    private AmazonMapReduceJobWorker predecessor;
    private ContentStore contentStore;
    private File workDir = new File("target", "header-worker");

    private InputStream textStream;
    private String header = "space-id,content-id,hash";
    private String csvSpaceId = "csv-space-id";
    private String csvContentId = "dir0/dir1/report.csv";
    private String csvNewContentId = "dir0/dir1/report-date.csv";
    private long sleepMillis = 100;
    private static final int NUM_LINES = 4;

    @Before
    public void setUp() throws Exception {
        predecessor = createMockJobWorker();
        contentStore = createMockContentStore();
        textStream = createTextStream(null);
        String serviceWorkDir = workDir.getAbsolutePath();

        worker = new HeaderPostJobWorker(predecessor,
                                         contentStore,
                                         serviceWorkDir,
                                         csvSpaceId,
                                         csvContentId,
                                         csvNewContentId,
                                         header,
                                         sleepMillis);
    }

    private AmazonMapReduceJobWorker createMockJobWorker() {
        AmazonMapReduceJobWorker worker = EasyMock.createMock(
            "PredecessorWorker",
            AmazonMapReduceJobWorker.class);

        EasyMock.expect(worker.getJobStatus()).andReturn(
            AmazonMapReduceJobWorker.JobStatus.COMPLETE);

        EasyMock.replay(worker);
        return worker;
    }

    private ContentStore createMockContentStore()
        throws ContentStoreException, FileNotFoundException {
        ContentStore contentStore = EasyMock.createMock("ContentStore",
                                                        ContentStore.class);

        Content content = createContent(csvContentId);
        EasyMock.expect(contentStore.getContent(csvSpaceId, csvContentId))
            .andReturn(content);

        EasyMock.expect(contentStore.addContent(EasyMock.isA(String.class),
                                                EasyMock.isA(String.class),
                                                eqInputStream(createTextStream(
                                                    header)),
                                                EasyMock.anyLong(),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<String>isNull(),
                                                EasyMock.<Map<String, String>>isNull()))
            .andReturn(null);

        contentStore.deleteContent(EasyMock.isA(String.class),
                                   EasyMock.isA(String.class));
        EasyMock.expectLastCall();

        EasyMock.replay(contentStore);
        return contentStore;
    }

    private Content createContent(String contentId) {
        Content content = new Content();
        content.setId(contentId);
        content.setStream(createTextStream(null));
        return content;
    }

    private InputStream createTextStream(String header) {
        String newline = System.getProperty("line.separator");

        StringBuilder sb = new StringBuilder();
        if (header != null) {
            sb.append(header);
            sb.append(newline);
        }

        for (int i = 0; i < NUM_LINES; ++i) {
            sb.append(i + "," + i + "," + i);
            sb.append(newline);
        }
        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(predecessor);
        EasyMock.verify(contentStore);

        textStream.close();
    }

    @Test
    public void testRun() throws Exception {
        worker.run();
    }


    /**
     * This nested class matches true if the InputStream contains the expected
     * header.
     * <p/>
     * It is used by including the eqResult(...) method below in the EasyMock
     * expectation.
     */
    public static class InputStreamEquals implements IArgumentMatcher {
        private List<String> expectedLines;

        public InputStreamEquals(InputStream expectedStream) {
            expectedLines = readStream(expectedStream);
        }

        private List<String> readStream(InputStream stream) {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            List<String> lines = new ArrayList<String>();
            String line = null;
            while ((line = readline(br)) != null) {
                lines.add(line);
            }
            close(br);

            return lines;
        }

        public boolean matches(Object actual) {
            if (!(actual instanceof InputStream)) {
                return false;
            }

            List<String> actualLines = readStream((InputStream) actual);
            Assert.assertEquals(expectedLines.size(), actualLines.size());
            for (int i = 0; i < expectedLines.size(); ++i) {
                Assert.assertEquals(expectedLines.get(i), actualLines.get(i));
            }
            return true;
        }

        private String readline(BufferedReader br) {
            String line = null;
            try {
                line = br.readLine();
            } catch (IOException e) {
                // do nothing
            }
            return line;
        }

        private void close(BufferedReader br) {
            try {
                br.close();
            } catch (IOException e) {
                // do nothing
            }
        }

        public void appendTo(StringBuffer buffer) {
            buffer.append("eqInputStream(");
            buffer.append(InputStreamEquals.class.getName());
            buffer.append(")");
        }
    }

    public static InputStream eqInputStream(InputStream inputStream) {
        EasyMock.reportMatcher(new InputStreamEquals(inputStream));
        return null;
    }

}
