/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.result;

import org.duracloud.client.ContentStore;
import org.duracloud.error.ContentStoreException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.util.Map;

import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.CONTENT_CREATE;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.SPACE_CREATE;
import static org.duracloud.services.duplication.result.DuplicationResultListenerTest.MODE.INVALID;
import static org.duracloud.services.duplication.result.DuplicationResultListenerTest.MODE.VALID;
import static org.duracloud.services.duplication.result.DuplicationResultListenerTest.MODE.VALID_REPORT;

/**
 * @author Andrew Woods
 *         Date: 9/16/11
 */
public class DuplicationResultListenerTest {

    private DuplicationResultListener listener;

    private ContentStore contentStore;
    private final String spaceId = "space-id";
    private final String reportId = "report-content-id";
    private String workDir;

    private final String mime = "text/tab-separated-values";

    @Before
    public void setUp() throws Exception {
        contentStore = EasyMock.createMock("ContentStore", ContentStore.class);

        File target = new File("target");
        Assert.assertTrue("target directory must exist!", target.exists());

        File work = new File(target, "test-dup-listener");
        if (!work.exists()) {
            Assert.assertTrue("Error: " + work.getAbsolutePath(), work.mkdir());
        }

        workDir = work.getAbsolutePath();
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentStore);
    }

    private void replayMocks() {
        EasyMock.replay(contentStore);
    }

    @Test
    public void testProcessResult() throws Exception {
        createProcessResultMocks(VALID);
        replayMocks();

        listener = new DuplicationResultListener(contentStore,
                                                 spaceId,
                                                 reportId,
                                                 workDir);

        DuplicationEvent event = new DuplicationEvent(spaceId, SPACE_CREATE);
        listener.processResult(event);
    }

    @Test
    public void testProcessResultReportId() throws Exception {
        createProcessResultMocks(VALID_REPORT);
        replayMocks();

        listener = new DuplicationResultListener(contentStore,
                                                 spaceId,
                                                 reportId,
                                                 workDir);

        DuplicationEvent event = new DuplicationEvent(spaceId,
                                                      reportId,
                                                      CONTENT_CREATE);
        listener.processResult(event);
    }

    @Test
    public void testProcessResultError() throws Exception {
        createProcessResultMocks(INVALID);
        replayMocks();

        listener = new DuplicationResultListener(contentStore,
                                                 spaceId,
                                                 reportId,
                                                 workDir);

        DuplicationEvent event = new DuplicationEvent(spaceId, SPACE_CREATE);

        // catches exception and writes a log.
        listener.processResult(event);
    }

    private void createProcessResultMocks(MODE mode)
        throws ContentStoreException {
        switch (mode) {
            case VALID:
                EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                        EasyMock.eq(reportId),
                                                        EasyMock.<InputStream>anyObject(),
                                                        EasyMock.anyInt(),
                                                        EasyMock.eq(mime),
                                                        EasyMock.<String>isNull(),
                                                        EasyMock.<Map<String, String>>isNull()))
                    .andReturn("md5");
                break;

            case VALID_REPORT:
                break;

            case INVALID:
                EasyMock.expect(contentStore.addContent(EasyMock.eq(spaceId),
                                                        EasyMock.eq(reportId),
                                                        EasyMock.<InputStream>anyObject(),
                                                        EasyMock.anyInt(),
                                                        EasyMock.eq(mime),
                                                        EasyMock.<String>isNull(),
                                                        EasyMock.<Map<String, String>>isNull()))
                    .andThrow(new ContentStoreException("canned-exception"));
                break;

            default:
                Assert.fail("unexpected mode: " + mode);
        }

    }

    protected enum MODE {
        VALID, VALID_REPORT, INVALID;
    }
}
