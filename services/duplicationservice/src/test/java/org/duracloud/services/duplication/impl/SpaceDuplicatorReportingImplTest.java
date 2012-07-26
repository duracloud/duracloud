/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.services.duplication.impl;

import org.duracloud.services.duplication.SpaceDuplicator;
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

import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.CREATE_ERROR;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.CREATE_SUCCESS;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.DELETE_ERROR;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.DELETE_SUCCESS;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.UPDATE_ACL_ERROR;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.UPDATE_ACL_SUCCESS;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.UPDATE_ERROR;
import static org.duracloud.services.duplication.impl.SpaceDuplicatorReportingImplTest.MODE.UPDATE_SUCCESS;
import static org.duracloud.services.duplication.result.DuplicationEvent.TYPE.SPACE_CREATE;

/**
 * @author Andrew Woods
 *         Date: 9/16/11
 */
public class SpaceDuplicatorReportingImplTest {

    private SpaceDuplicatorReportingImpl spaceDuplicatorReporting;

    private SpaceDuplicator spaceDuplicator;
    private ResultListener listener;

    private static final String fromStoreId = "from-store-id";
    private static final String toStoreId = "to-store-id";
    private final String spaceId = "space-id";
    private final long waitMillis = 1;

    @Before
    public void setUp() throws Exception {
        spaceDuplicator = EasyMock.createMock("SpaceDuplicator",
                                              SpaceDuplicator.class);
        EasyMock.expect(spaceDuplicator.getFromStoreId())
                .andReturn(fromStoreId).anyTimes();
        EasyMock.expect(spaceDuplicator.getToStoreId())
                .andReturn(toStoreId).anyTimes();

        listener = EasyMock.createMock("ResultListener", ResultListener.class);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(spaceDuplicator, listener);
        spaceDuplicatorReporting.stop();
    }

    private void replayMocks() {
        EasyMock.replay(spaceDuplicator, listener);
    }

    @Test
    public void testStop() throws Exception {
        // set up mocks
        List<String> spaces = new ArrayList<String>();
        List<Capture<DuplicationEvent>> captures =
            new ArrayList<Capture<DuplicationEvent>>();
        createStopMocks(spaces, captures);
        replayMocks();

        // create object under test
        spaceDuplicatorReporting = new SpaceDuplicatorReportingImpl(
            spaceDuplicator,
            listener);

        // exercise the test scenario
        for (String space : spaces) {
            spaceDuplicatorReporting.createSpace(space);
        }
        spaceDuplicatorReporting.stop();

        // verify
        Assert.assertEquals(spaces.size(), captures.size());
        for (Capture<DuplicationEvent> capture : captures) {
            DuplicationEvent event = capture.getValue();
            Assert.assertNotNull(event);
            Assert.assertFalse(event.isSuccess());
        }

        Assert.assertFalse(spaceDuplicatorReporting.retriesExist());
    }

    private void createStopMocks(List<String> spaces,
                                 List<Capture<DuplicationEvent>> captures) {
        for (int i = 0; i < 5; ++i) {
            String space = spaceId + "-" + i;
            spaces.add(space);

            spaceDuplicator.createSpace(space);
            EasyMock.expectLastCall().andThrow(new DuplicationException(
                "canned-exception"));

            Capture<DuplicationEvent> capture = new Capture<DuplicationEvent>();
            captures.add(capture);

            listener.processResult(EasyMock.capture(capture));
            EasyMock.expectLastCall();
        }
        EasyMock.makeThreadSafe(spaceDuplicator, true);
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
    public void testUpdateSpaceAcl() throws Exception {
        doTestSpace(UPDATE_ACL_SUCCESS);
    }

    @Test
    public void testUpdateSpaceAclError() throws Exception {
        doTestSpace(UPDATE_ACL_ERROR);
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

        spaceDuplicatorReporting = new SpaceDuplicatorReportingImpl(
            spaceDuplicator,
            listener,
            waitMillis);

        switch (mode) {
            case CREATE_SUCCESS:
            case CREATE_ERROR:
                spaceDuplicatorReporting.createSpace(spaceId);
                break;

            case UPDATE_SUCCESS:
            case UPDATE_ERROR:
                spaceDuplicatorReporting.updateSpace(spaceId);
                break;

            case UPDATE_ACL_SUCCESS:
            case UPDATE_ACL_ERROR:
                spaceDuplicatorReporting.updateSpaceAcl(spaceId);
                break;

            case DELETE_SUCCESS:
            case DELETE_ERROR:
                spaceDuplicatorReporting.deleteSpace(spaceId);
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
                spaceDuplicator.createSpace(spaceId);
                EasyMock.expectLastCall();
                break;

            case UPDATE_SUCCESS:
                spaceDuplicator.updateSpace(spaceId);
                EasyMock.expectLastCall();
                break;

            case UPDATE_ACL_SUCCESS:
                spaceDuplicator.updateSpaceAcl(spaceId);
                EasyMock.expectLastCall();
                break;

            case DELETE_SUCCESS:
                spaceDuplicator.deleteSpace(spaceId);
                EasyMock.expectLastCall();
                break;

            case CREATE_ERROR:
                spaceDuplicator.createSpace(spaceId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(spaceDuplicator, true);
                break;

            case UPDATE_ERROR:
                spaceDuplicator.updateSpace(spaceId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(spaceDuplicator, true);
                break;

            case UPDATE_ACL_ERROR:
                spaceDuplicator.updateSpaceAcl(spaceId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(spaceDuplicator, true);
                break;

            case DELETE_ERROR:
                spaceDuplicator.deleteSpace(spaceId);
                EasyMock.expectLastCall().andThrow(new DuplicationException(
                    "canned-exception")).times(
                    SpaceDuplicatorReportingImpl.MAX_RETRIES + 1);
                EasyMock.makeThreadSafe(spaceDuplicator, true);
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
        final long maxWait = 5000;
        long waited = 0;
        long wait = 400;
        while (spaceDuplicatorReporting.retriesExist()) {
            Thread.sleep(wait);
            waited += wait;
            if (waited > maxWait) {
                Assert.fail("test exceeded time limit: " + maxWait);
            }
        }
    }

    protected enum MODE {
        CREATE_SUCCESS(true, SPACE_CREATE),
        UPDATE_SUCCESS(true, DuplicationEvent.TYPE.SPACE_UPDATE),
        UPDATE_ACL_SUCCESS(true, DuplicationEvent.TYPE.SPACE_UPDATE_ACL),
        DELETE_SUCCESS(true, DuplicationEvent.TYPE.SPACE_DELETE),
        CREATE_ERROR(false, SPACE_CREATE),
        UPDATE_ERROR(false, DuplicationEvent.TYPE.SPACE_UPDATE),
        UPDATE_ACL_ERROR(false, DuplicationEvent.TYPE.SPACE_UPDATE_ACL),
        DELETE_ERROR(false, DuplicationEvent.TYPE.SPACE_DELETE);

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
