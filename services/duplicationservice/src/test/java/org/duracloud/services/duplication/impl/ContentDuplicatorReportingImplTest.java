/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.services.duplication.ContentDuplicator;
import org.duracloud.services.duplication.error.DuplicationException;
import org.duracloud.services.duplication.result.DuplicationEvent;
import org.duracloud.services.duplication.result.ResultListener;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.duracloud.services.duplication.impl.ContentDuplicatorReportingImplTest.MODE.CREATE_SUCCESS;
import static org.duracloud.services.duplication.impl.ContentDuplicatorReportingImplTest.MODE.UPDATE_SUCCESS;
import static org.duracloud.services.duplication.impl.ContentDuplicatorReportingImplTest.MODE.DELETE_SUCCESS;
import static org.duracloud.services.duplication.impl.ContentDuplicatorReportingImplTest.MODE.CREATE_ERROR;
import static org.duracloud.services.duplication.impl.ContentDuplicatorReportingImplTest.MODE.UPDATE_ERROR;
import static org.duracloud.services.duplication.impl.ContentDuplicatorReportingImplTest.MODE.DELETE_ERROR;

/**
 * @author Andrew Woods
 *         Date: 9/18/11
 */
public class ContentDuplicatorReportingImplTest {
    private ContentDuplicatorReportingImpl contentDuplicatorReporting;

    private ContentDuplicator contentDuplicator;
    private ResultListener listener;

    private static final String fromStoreId = "from-store-id";
    private static final String toStoreId = "to-store-id";
    private final String spaceId = "space-id";
    private final String contentId = "content-id";
    private final long waitMillis = 1;

    @Before
    public void setUp() throws Exception {
        contentDuplicator = EasyMock.createMock("ContentDuplicator",
                                                ContentDuplicator.class);
        EasyMock.expect(contentDuplicator.getFromStoreId())
                .andReturn(fromStoreId).anyTimes();
        EasyMock.expect(contentDuplicator.getToStoreId())
                .andReturn(toStoreId).anyTimes();

        listener = EasyMock.createMock("ResultListener", ResultListener.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(contentDuplicator, listener);
        contentDuplicatorReporting.stop();
    }

    private void replayMocks() {
        EasyMock.replay(contentDuplicator, listener);
    }

    @Test
    public void testStop() throws Exception {
        // set up mocks
        List<String> contents = new ArrayList<String>();
        List<Capture<DuplicationEvent>> captures =
            new ArrayList<Capture<DuplicationEvent>>();
        createStopMocks(contents, captures);
        replayMocks();

        // create object under test
        contentDuplicatorReporting = new ContentDuplicatorReportingImpl(
            contentDuplicator,
            listener);

        // exercise the test scenario
        for (String content : contents) {
            contentDuplicatorReporting.createContent(spaceId, content);
        }
        contentDuplicatorReporting.stop();

        // verify
        Assert.assertEquals(contents.size(), captures.size());
        for (Capture<DuplicationEvent> capture : captures) {
            DuplicationEvent event = capture.getValue();
            Assert.assertNotNull(event);
            Assert.assertFalse(event.isSuccess());
        }

        Assert.assertFalse(contentDuplicatorReporting.eventsExist());
    }

    private void createStopMocks(List<String> contents,
                                 List<Capture<DuplicationEvent>> captures) {
        for (int i = 0; i < 5; ++i) {
            String content = contentId + "-" + i;
            contents.add(content);

            Capture<DuplicationEvent> capture = new Capture<DuplicationEvent>();
            captures.add(capture);

            listener.processResult(EasyMock.capture(capture));
            EasyMock.expectLastCall();
        }
        EasyMock.makeThreadSafe(contentDuplicator, true);
    }

    @Test
    public void testCreateSpace() throws Exception {
        doTestSpace(CREATE_SUCCESS);
    }

    @Test
    public void testCreateSpaceError() throws Exception {
        doTestSpace(CREATE_ERROR);
    }

    @Test
    public void testUpdateSpace() throws Exception {
        doTestSpace(UPDATE_SUCCESS);
    }

    @Test
    public void testUpdateSpaceError() throws Exception {
        doTestSpace(UPDATE_ERROR);
    }

    @Test
    public void testDeleteSpace() throws Exception {
        doTestSpace(DELETE_SUCCESS);
    }

    @Test
    public void testDeleteSpaceError() throws Exception {
        doTestSpace(DELETE_ERROR);
    }

    private void doTestSpace(MODE mode) throws Exception {
        Capture<DuplicationEvent> capture = createMocks(mode);
        replayMocks();

        contentDuplicatorReporting = new ContentDuplicatorReportingImpl(
            contentDuplicator,
            listener,
            waitMillis);

        switch (mode) {
            case CREATE_SUCCESS:
            case CREATE_ERROR:
                contentDuplicatorReporting.createContent(spaceId, contentId);
                break;

            case UPDATE_SUCCESS:
            case UPDATE_ERROR:
                contentDuplicatorReporting.updateContent(spaceId, contentId);
                break;

            case DELETE_SUCCESS:
            case DELETE_ERROR:
                contentDuplicatorReporting.deleteContent(spaceId, contentId);
                break;
        }
        waitForCompletion();

        DuplicationEvent event = capture.getValue();
        Assert.assertNotNull(event);

        Assert.assertEquals(mode.isValid(), event.isSuccess());
        Assert.assertEquals(mode.getType(), event.getType());
    }

    private Capture<DuplicationEvent> createMocks(MODE mode) {
        switch (mode) {
            case CREATE_SUCCESS:
                EasyMock.expect(contentDuplicator.createContent(spaceId,
                                                                contentId))
                    .andReturn("md5");
                break;

            case UPDATE_SUCCESS:
                contentDuplicator.updateContent(spaceId, contentId);
                EasyMock.expectLastCall();
                break;

            case DELETE_SUCCESS:
                contentDuplicator.deleteContent(spaceId, contentId);
                EasyMock.expectLastCall();
                break;

            case CREATE_ERROR:
                contentDuplicator.createContent(spaceId, contentId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(contentDuplicator, true);
                break;

            case UPDATE_ERROR:
                contentDuplicator.updateContent(spaceId, contentId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(contentDuplicator, true);
                break;

            case DELETE_ERROR:
                contentDuplicator.deleteContent(spaceId, contentId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(contentDuplicator, true);
                break;

            default:
                Assert.fail("Unexpected mode: " + mode);
        }

        Capture<DuplicationEvent> capturedResult =
            new Capture<DuplicationEvent>();
        listener.processResult(EasyMock.capture(capturedResult));

        return capturedResult;
    }

    private void waitForCompletion() throws InterruptedException {
        final long maxWait = 8000;
        long waited = 0;
        long wait = 400;
        while (contentDuplicatorReporting.eventsExist()) {
            Thread.sleep(wait);
            waited += wait;
            if (waited > maxWait) {
                Assert.fail("test exceeded time limit: " + maxWait);
            }
        }
    }

    protected enum MODE {
        CREATE_SUCCESS(true, DuplicationEvent.TYPE.CONTENT_CREATE),
        UPDATE_SUCCESS(true, DuplicationEvent.TYPE.CONTENT_UPDATE),
        DELETE_SUCCESS(true, DuplicationEvent.TYPE.CONTENT_DELETE),
        CREATE_ERROR(false, DuplicationEvent.TYPE.CONTENT_CREATE),
        UPDATE_ERROR(false, DuplicationEvent.TYPE.CONTENT_UPDATE),
        DELETE_ERROR(false, DuplicationEvent.TYPE.CONTENT_DELETE);

        private boolean valid;
        private DuplicationEvent.TYPE type;

        private MODE(boolean valid, DuplicationEvent.TYPE type) {
            this.valid = valid;
            this.type = type;
        }

        public boolean isValid() {
            return valid;
        }

        public DuplicationEvent.TYPE getType() {
            return type;
        }
    }
}
